import SwiftUI
import UserNotifications

// Lets the notification delegate (which has no access to the SwiftUI store) ask
// the UI to navigate. ContentView observes this and switches tabs.
final class AppRouter: ObservableObject {
    static let shared = AppRouter()
    @Published var pendingTab: String?
}

// Hooks notifications into the app lifecycle: requests permission on launch,
// presents reminder notifications even while the app is foregrounded
// (Android shows them regardless of app state), and deep-links taps to Reminders.
class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        ReminderScheduler.requestAuthorization()
        return true
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        // `.sound` plays only if the notification actually carries one, so silent
        // and badge modes stay quiet even in the foreground.
        completionHandler([.banner, .list, .sound])
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        // Tapping a reminder notification opens the Reminders screen.
        if response.notification.request.identifier.hasPrefix("toolbox.reminder.") {
            AppRouter.shared.pendingTab = "reminders"
        }
        completionHandler()
    }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
