import Foundation
import UserNotifications
import SharedLogic

/// iOS counterpart to Android's `ReminderScheduler` + `ReminderNotifications`.
///
/// Unlike Android (exact alarms + a BroadcastReceiver that builds the notification
/// at fire time), iOS notifications are declarative: we register one or more
/// `UNNotificationRequest`s per reminder and the system delivers them even when the
/// app is closed and across reboots. `sync(state:)` reconciles the scheduled set with
/// the current state on every mutation, mirroring the Android listener.
enum ReminderScheduler {
    private static let prefix = "toolbox.reminder."
    private static let fridgePrefix = "toolbox.fridge."

    /// Ask once for permission to show alerts/sounds/badges.
    static func requestAuthorization() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { _, _ in }
    }

    /// Align scheduled notifications with the current state: (re)schedule every active
    /// reminder and cancel requests whose reminders were deleted, turned off, or
    /// (for one-shots) already completed.
    static func sync(state: ToolboxState) {
        let center = UNUserNotificationCenter.current()
        let active = state.reminders.filter { $0.on && shouldSchedule($0, doneIds: state.doneIds) }

        var desired: [UNNotificationRequest] = []
        for r in active {
            desired.append(contentsOf: requests(for: r, quietHoursOn: state.quietHoursOn))
        }

        // Add fridge notifications if enabled
        if state.settings.expiryNotificationsOn {
            for f in state.fridge {
                desired.append(contentsOf: fridgeRequests(for: f))
            }
        }

        let desiredIds = Set(desired.map { $0.identifier })

        center.getPendingNotificationRequests { pending in
            // Only ever touch requests we own.
            let ours = pending.map { $0.identifier }.filter { $0.hasPrefix(prefix) || $0.hasPrefix(fridgePrefix) }
            let stale = ours.filter { !desiredIds.contains($0) }
            if !stale.isEmpty {
                center.removePendingNotificationRequests(withIdentifiers: stale)
            }
            // Adding with an existing identifier replaces it, so this is idempotent.
            for req in desired {
                center.add(req)
            }
        }
    }

    // One-shot reminders already marked done stay quiet; repeating ones always fire.
    private static func shouldSchedule(_ r: Reminder, doneIds: [String]) -> Bool {
        return !r.`repeat`.isEmpty || !doneIds.contains(r.id)
    }

    /// Build the request(s) for a reminder. "weekdays" needs five weekly triggers
    /// (Mon–Fri) because a single calendar trigger can't express "weekdays only".
    private static func requests(for r: Reminder, quietHoursOn: Bool) -> [UNNotificationRequest] {
        let parts = r.time.split(separator: ":")
        guard parts.count >= 2,
              let hour = Int(parts[0]), let minute = Int(parts[1]),
              (0...23).contains(hour), (0...59).contains(minute) else { return [] }

        // Fire time is the reminder's time-of-day, so quiet hours (10 PM–7 AM) is
        // decided here at scheduling time — equivalent to Android deciding at fire time.
        let effectiveMode = (quietHoursOn && (hour >= 22 || hour < 7)) ? "silent" : r.mode
        let content = makeContent(title: r.title, time: r.time, mode: effectiveMode)

        let base = prefix + r.id
        let cal = Calendar.current
        let now = Date()

        func comps(weekday: Int? = nil, day: Int? = nil) -> DateComponents {
            var c = DateComponents()
            c.hour = hour
            c.minute = minute
            c.weekday = weekday
            c.day = day
            return c
        }

        switch r.`repeat` {
        case "daily":
            return [request(base, content, UNCalendarNotificationTrigger(dateMatching: comps(), repeats: true))]
        case "weekly":
            let weekday = cal.component(.weekday, from: now)
            return [request(base, content, UNCalendarNotificationTrigger(dateMatching: comps(weekday: weekday), repeats: true))]
        case "monthly":
            let day = cal.component(.day, from: now)
            return [request(base, content, UNCalendarNotificationTrigger(dateMatching: comps(day: day), repeats: true))]
        case "weekdays":
            // Gregorian weekday: 1 = Sunday … 7 = Saturday, so Mon–Fri = 2…6.
            return (2...6).map { wd in
                request("\(base).wd\(wd)", content, UNCalendarNotificationTrigger(dateMatching: comps(weekday: wd), repeats: true))
            }
        default:
            // One-shot: next occurrence of HH:MM, fires once (today if upcoming, else tomorrow).
            return [request(base, content, UNCalendarNotificationTrigger(dateMatching: comps(), repeats: false))]
        }
    }

    private static func makeContent(title: String, time: String, mode: String) -> UNMutableNotificationContent {
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = "Quiet nudge · \(time)"
        // Mirrors the Android per-mode notification channels. iOS has no pure
        // "vibrate-only" scheduled notification, so "buzz" uses the default alert.
        switch mode {
        case "silent", "badge":
            content.sound = nil
            content.interruptionLevel = .passive
        case "buzz":
            content.sound = .default
            content.interruptionLevel = .active
        default: // banner
            content.sound = .default
            content.interruptionLevel = .active
        }
        return content
    }

    private static func request(_ id: String, _ content: UNNotificationContent, _ trigger: UNNotificationTrigger) -> UNNotificationRequest {
        UNNotificationRequest(identifier: id, content: content, trigger: trigger)
    }

    private static func fridgeRequests(for f: FridgeItem) -> [UNNotificationRequest] {
        guard !f.expiry.isEmpty else { return [] }

        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd"
        dateFormatter.locale = Locale(identifier: "en_US_POSIX")

        guard let expiryDate = dateFormatter.date(from: f.expiry) else { return [] }

        let calendar = Calendar.current
        let today = calendar.startOfDay(for: Date())
        let targetDay = calendar.startOfDay(for: expiryDate)

        // Only schedule if today or in the future
        guard targetDay >= today else { return [] }

        var requests: [UNNotificationRequest] = []

        // 1. Day of expiry notification
        let todayComps = calendar.dateComponents([.year, .month, .day], from: targetDay)
        var triggerComps = DateComponents()
        triggerComps.year = todayComps.year
        triggerComps.month = todayComps.month
        triggerComps.day = todayComps.day
        triggerComps.hour = 9
        triggerComps.minute = 0

        let todayContent = UNMutableNotificationContent()
        todayContent.title = "Fridge Expiry Alert"
        todayContent.body = "\(f.name) expires today!"
        todayContent.sound = .default
        todayContent.interruptionLevel = .active

        let todayTrigger = UNCalendarNotificationTrigger(dateMatching: triggerComps, repeats: false)
        requests.append(request("\(fridgePrefix)\(f.id).today", todayContent, todayTrigger))

        // 2. Day before expiry notification
        if let yesterday = calendar.date(byAdding: .day, value: -1, to: targetDay), yesterday >= today {
            let yesterdayComps = calendar.dateComponents([.year, .month, .day], from: yesterday)
            var triggerCompsY = DateComponents()
            triggerCompsY.year = yesterdayComps.year
            triggerCompsY.month = yesterdayComps.month
            triggerCompsY.day = yesterdayComps.day
            triggerCompsY.hour = 9
            triggerCompsY.minute = 0

            let tomorrowContent = UNMutableNotificationContent()
            tomorrowContent.title = "Fridge Expiry Alert"
            tomorrowContent.body = "\(f.name) expires tomorrow!"
            tomorrowContent.sound = .default
            tomorrowContent.interruptionLevel = .active

            let tomorrowTrigger = UNCalendarNotificationTrigger(dateMatching: triggerCompsY, repeats: false)
            requests.append(request("\(fridgePrefix)\(f.id).tomorrow", tomorrowContent, tomorrowTrigger))
        }

        return requests
    }
}
