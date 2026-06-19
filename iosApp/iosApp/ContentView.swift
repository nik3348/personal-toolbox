import SwiftUI
import SharedLogic
import UserNotifications

// Helper to convert hex colors in SwiftUI
extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (1, 1, 1, 0)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}

// Brand Palettes matching Kotlin
struct SwiftBrandPalette {
    let primary: Color
    let deep: Color
    let hover: Color
    let tint: Color
    let border: Color
    let label: String
}

let SwiftIndigo = SwiftBrandPalette(primary: Color(hex: "#4f46e5"), deep: Color(hex: "#312e81"), hover: Color(hex: "#4338ca"), tint: Color(hex: "#eef2ff"), border: Color(hex: "#c7d2fe"), label: "Indigo")
let SwiftForest = SwiftBrandPalette(primary: Color(hex: "#15803d"), deep: Color(hex: "#14532d"), hover: Color(hex: "#166534"), tint: Color(hex: "#dcfce7"), border: Color(hex: "#86efac"), label: "Forest")
let SwiftCyber = SwiftBrandPalette(primary: Color(hex: "#0e7490"), deep: Color(hex: "#164e63"), hover: Color(hex: "#155e75"), tint: Color(hex: "#cffafe"), border: Color(hex: "#67e8f9"), label: "Cyber")
let SwiftSunset = SwiftBrandPalette(primary: Color(hex: "#ea580c"), deep: Color(hex: "#7c2d12"), hover: Color(hex: "#c2410c"), tint: Color(hex: "#ffedd5"), border: Color(hex: "#fdba74"), label: "Sunset")

let SwiftBrandPalettes: [String: SwiftBrandPalette] = [
    "indigo": SwiftIndigo,
    "forest": SwiftForest,
    "cyber": SwiftCyber,
    "sunset": SwiftSunset
]

// Dark-aware theme tokens, mirroring Android's ToolboxTheme.
struct SwiftTheme {
    let isDark: Bool
    let ink: Color
    let inkSoft: Color
    let inkMute: Color
    let bg: Color
    let bgSubtle: Color
    let surface: Color
    let line: Color
    let control: Color
    let fridgeAccent: Color
    let fridgeTint: Color
    let shoppingAccent: Color
    let shoppingTint: Color

    // Fixed status colors (same in both modes)
    static let danger = Color(hex: "#ef4444")
    static let warn = Color(hex: "#f59e0b")
    static let ok = Color(hex: "#22c55e")
    static let cyan = Color(hex: "#00bfff")

    static func make(dark: Bool) -> SwiftTheme {
        if dark {
            return SwiftTheme(
                isDark: true,
                ink: Color(hex: "#f8fafc"),
                inkSoft: Color(hex: "#e2e8f0"),
                inkMute: Color(hex: "#94a3b8"),
                bg: Color(hex: "#020617"),
                bgSubtle: Color(hex: "#0f172a"),
                surface: Color(hex: "#1e293b"),
                line: Color(hex: "#94a3b8").opacity(0.24),
                control: Color(hex: "#334155"),
                fridgeAccent: Color(hex: "#22d3ee"),
                fridgeTint: Color(hex: "#0891b2").opacity(0.15),
                shoppingAccent: Color(hex: "#4ade80"),
                shoppingTint: Color(hex: "#16a34a").opacity(0.15)
            )
        } else {
            return SwiftTheme(
                isDark: false,
                ink: Color(hex: "#0f172a"),
                inkSoft: Color(hex: "#334155"),
                inkMute: Color(hex: "#64748b"),
                bg: Color(hex: "#f8fafc"),
                bgSubtle: Color(hex: "#f1f5f9"),
                surface: Color.white,
                line: Color(hex: "#0f172a").opacity(0.08),
                control: Color(hex: "#e2e8f0"),
                fridgeAccent: Color(hex: "#0891b2"),
                fridgeTint: Color(hex: "#ecfeff"),
                shoppingAccent: Color(hex: "#16a34a"),
                shoppingTint: Color(hex: "#dcfce7")
            )
        }
    }

    // Accent-derived tints that adapt to dark mode (matches Android activePaletteTint/Border).
    func paletteTint(_ p: SwiftBrandPalette) -> Color { isDark ? p.primary.opacity(0.15) : p.tint }
    func paletteBorder(_ p: SwiftBrandPalette) -> Color { isDark ? p.primary.opacity(0.5) : p.border }
}

extension View {
    // Card chrome shared across screens.
    func toolboxCard(_ theme: SwiftTheme, radius: CGFloat = 16) -> some View {
        self
            .background(theme.surface)
            .cornerRadius(radius)
            .overlay(RoundedRectangle(cornerRadius: radius).stroke(theme.line, lineWidth: 1))
    }
}

// MARK: Formatting helpers (mirror Android HomeScreen helpers)

func format12hTime(_ timeStr: String) -> String {
    let parts = timeStr.split(separator: ":")
    guard parts.count >= 2, let h = Int(parts[0]), let m = Int(parts[1]) else { return timeStr }
    let am = h < 12
    let h12 = h == 0 ? 12 : (h > 12 ? h - 12 : h)
    return String(format: "%d:%02d %@", h12, m, am ? "AM" : "PM")
}

func formatDaysLabel(_ days: Int) -> String {
    if days < 0 { return "\(-days)d ago" }
    if days == 0 { return "TODAY" }
    if days == 1 { return "TOMORROW" }
    return "\(days) days"
}

func daysUntil(_ expiry: String) -> Int {
    return Int(DateUtils.shared.daysUntil(expiry: expiry))
}

func urgencyColor(_ days: Int) -> Color {
    if days <= 1 { return SwiftTheme.danger }
    if days <= 3 { return SwiftTheme.warn }
    return SwiftTheme.ok
}

// Static option metadata shared by reminder UI
let alertModes: [(key: String, label: String, sub: String, emoji: String)] = [
    ("banner", "Banner Only", "Slides in, doesn't ring", "🔔"),
    ("badge", "Badge Only", "Just a red dot on the app", "✨"),
    ("buzz", "One Buzz", "Single haptic, no sound", "📳"),
    ("silent", "Fully Silent", "In the list only", "🔕")
]
let repeatOptions: [(value: String, label: String)] = [
    ("", "once"), ("daily", "daily"), ("weekdays", "weekdays"), ("weekly", "weekly"), ("monthly", "monthly")
]
let fridgeLocations = ["fridge", "freezer", "pantry"]
let mealSlots: [(key: String, label: String)] = [
    ("breakfast", "Breakfast"), ("lunch", "Lunch"), ("dinner", "Dinner")
]

// Swift mirrors of the shared helpers (logic identical to Models.kt's
// ingredientInFridge / weekdayShort) to avoid cross-language top-level calls.
func fridgeHasIngredient(_ name: String, _ fridge: [FridgeItem]) -> Bool {
    let key = name.trimmingCharacters(in: .whitespaces).lowercased()
    return !key.isEmpty && fridge.contains { $0.name.trimmingCharacters(in: .whitespaces).lowercased() == key }
}

func weekdayShort(_ date: String) -> String {
    let p = date.split(separator: "-").map { Int($0) ?? -1 }
    guard p.count == 3, !p.contains(-1) else { return "" }
    var (y, m, d) = (p[0], p[1], p[2])
    if m < 3 { m += 12; y -= 1 }
    let k = y % 100, j = y / 100
    let h = ((d + (13 * (m + 1)) / 5 + k + k / 4 + j / 4 + 5 * j) % 7 + 7) % 7
    return ["Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri"][h]
}

extension Recipe: Identifiable {}

// MARK: State Observer matching Kotlin Repository State

class ToolboxObservableState: ObservableObject, ToolboxRepositoryListener {
    @Published var state: ToolboxState
    private let repo: ToolboxRepository

    init(repo: ToolboxRepository) {
        self.repo = repo
        self.state = repo.state
        repo.addListener(listener: self)
        // Reconcile per-day reminder state on launch (clears yesterday's checkmarks, etc.)
        repo.rolloverIfNeeded()
    }

    func onStateChanged(state: ToolboxState) {
        DispatchQueue.main.async {
            self.state = state
        }
        // Keep scheduled notifications aligned with state (mirrors Android's listener).
        ReminderScheduler.sync(state: state)
    }

    func toggleDone(id: String) { repo.toggleDone(id: id) }
    func setOn(id: String, on: Bool) { repo.setOn(id: id, on: on) }
    func setMode(id: String, mode: String) { repo.setMode(id: id, mode: mode) }
    func deleteReminder(id: String) { repo.deleteReminder(id: id) }
    func updateReminder(id: String, title: String, time: String, repeat: String, mode: String) {
        repo.updateReminder(id: id, title: title, time: time, repeat: `repeat`, mode: mode)
    }
    func addReminder(title: String, time: String, repeat: String, mode: String) {
        repo.addReminder(title: title, time: time, repeat: `repeat`, mode: mode)
    }
    func consumeFridge(id: String) { repo.consumeFridge(id: id) }
    func updateFridge(id: String, name: String, qty: String, expiry: String, location: String) {
        repo.updateFridge(id: id, name: name, qty: qty, expiry: expiry, location: location)
    }
    func addFridge(name: String, qty: String, expiry: String, location: String) {
        repo.addFridge(name: name, qty: qty, expiry: expiry, location: location)
    }
    func nudgeFromFridge(name: String) { repo.nudgeFromFridge(itemName: name) }
    func restockFridgeItem(id: String) { repo.restockFridgeItem(id: id) }
    func toggleShoppingItem(id: String) { repo.toggleShoppingItem(id: id) }
    func deleteShoppingItem(id: String) { repo.deleteShoppingItem(id: id) }
    func updateShoppingItem(id: String, name: String, qty: String) {
        repo.updateShoppingItem(id: id, name: name, qty: qty)
    }
    func addShoppingItem(name: String, qty: String) { repo.addShoppingItem(name: name, qty: qty) }
    func clearCheckedShoppingItems() { repo.clearCheckedShoppingItems() }
    func purchaseShoppingItem(id: String, location: String, expiry: String) {
        repo.purchaseShoppingItem(itemId: id, location: location, expiry: expiry)
    }
    func purchaseCheckedShoppingItems(location: String, expiry: String) {
        repo.purchaseCheckedShoppingItems(location: location, expiry: expiry)
    }
    func addRecipe(name: String, ingredients: [RecipeIngredient], steps: [String]) {
        repo.addRecipe(name: name, ingredients: ingredients, steps: steps)
    }
    func updateRecipe(id: String, name: String, ingredients: [RecipeIngredient], steps: [String]) {
        repo.updateRecipe(id: id, name: name, ingredients: ingredients, steps: steps)
    }
    func deleteRecipe(id: String) { repo.deleteRecipe(id: id) }
    func sendRecipeToShoppingList(id: String) { repo.sendRecipeToShoppingList(id: id) }
    func setMealSlot(date: String, slot: String, recipeId: String) {
        repo.setMealSlot(date: date, slot: slot, recipeId: recipeId)
    }
    func clearMealSlot(date: String, slot: String) {
        repo.clearMealSlot(date: date, slot: slot)
    }
    func sendPlannedMealsToShoppingList() {
        repo.sendPlannedMealsToShoppingList(days: 7)
    }
    func reset() { repo.reset() }
    func setQuiet(on: Bool) { repo.setQuiet(on: on) }
    func setAccent(accent: String) { repo.setAccent(accent: accent) }
    func setDarkMode(on: Bool) { repo.setDarkMode(on: on) }
    func setShowFlourishes(on: Bool) { repo.setShowFlourishes(on: on) }
    func setBackgroundPattern(pattern: String) { repo.setBackgroundPattern(pattern: pattern) }
}

struct ContentView: View {
    @StateObject var store = ToolboxObservableState(repo: ToolboxRepository(storage: KeyValueStorage()))
    @State private var activeTab = "home"

    var accentBinding: Binding<String> {
        Binding(get: { store.state.settings.accent }, set: { store.setAccent(accent: $0) })
    }
    var darkModeBinding: Binding<Bool> {
        Binding(get: { store.state.settings.darkMode }, set: { store.setDarkMode(on: $0) })
    }
    var showFlourishesBinding: Binding<Bool> {
        Binding(get: { store.state.settings.showFlourishes }, set: { store.setShowFlourishes(on: $0) })
    }
    var backgroundPatternBinding: Binding<String> {
        Binding(get: { store.state.settings.backgroundPattern }, set: { store.setBackgroundPattern(pattern: $0) })
    }

    var currentPalette: SwiftBrandPalette {
        SwiftBrandPalettes[store.state.settings.accent] ?? SwiftIndigo
    }
    var theme: SwiftTheme {
        SwiftTheme.make(dark: store.state.settings.darkMode)
    }

    var body: some View {
        ZStack {
            BackgroundView(pattern: store.state.settings.backgroundPattern, theme: theme)
                .edgesIgnoringSafeArea(.all)

            VStack(spacing: 0) {
                ZStack {
                    if activeTab == "home" {
                        SwiftHomeScreen(store: store, activeTab: $activeTab, palette: currentPalette, theme: theme, showFlourishes: store.state.settings.showFlourishes)
                    } else if activeTab == "reminders" {
                        SwiftRemindersScreen(store: store, palette: currentPalette, theme: theme)
                    } else if activeTab == "fridge" {
                        SwiftFridgeScreen(store: store, palette: currentPalette, theme: theme)
                    } else if activeTab == "shopping" {
                        SwiftShoppingScreen(store: store, palette: currentPalette, theme: theme)
                    } else if activeTab == "recipes" {
                        SwiftRecipesScreen(store: store, palette: currentPalette, theme: theme)
                    } else if activeTab == "mealplanner" {
                        SwiftMealPlannerScreen(store: store, palette: currentPalette, theme: theme)
                    } else if activeTab == "me" {
                        SwiftMeScreen(
                            store: store,
                            activeTab: $activeTab,
                            accent: accentBinding,
                            darkMode: darkModeBinding,
                            showFlourishes: showFlourishesBinding,
                            backgroundPattern: backgroundPatternBinding,
                            palette: currentPalette,
                            theme: theme
                        )
                    }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)

                // Settings is reached via the gear on Home, so it is not a tab.
                if activeTab != "me" {
                    SwiftTabBar(activeTab: $activeTab, palette: currentPalette, theme: theme)
                }
            }
        }
        .preferredColorScheme(theme.isDark ? .dark : .light)
        .onReceive(AppRouter.shared.$pendingTab.compactMap { $0 }) { tab in
            activeTab = tab
            AppRouter.shared.pendingTab = nil
        }
    }
}

// Background design drawer (grid / dots / plain), dark-aware.
struct BackgroundView: View {
    let pattern: String
    let theme: SwiftTheme

    var body: some View {
        GeometryReader { geo in
            ZStack {
                theme.bgSubtle
                if pattern == "grid" {
                    Path { path in
                        let step: CGFloat = 32
                        var x: CGFloat = 0
                        while x < geo.size.width {
                            path.move(to: CGPoint(x: x, y: 0))
                            path.addLine(to: CGPoint(x: x, y: geo.size.height))
                            x += step
                        }
                        var y: CGFloat = 0
                        while y < geo.size.height {
                            path.move(to: CGPoint(x: 0, y: y))
                            path.addLine(to: CGPoint(x: geo.size.width, y: y))
                            y += step
                        }
                    }
                    .stroke(theme.isDark ? Color.white.opacity(0.04) : Color(hex: "#0f172a").opacity(0.03), lineWidth: 1)
                } else if pattern == "dots" {
                    Canvas { ctx, size in
                        let step: CGFloat = 24
                        let dot = theme.isDark ? Color.white.opacity(0.08) : Color(hex: "#0f172a").opacity(0.07)
                        var x: CGFloat = 0
                        while x < size.width {
                            var y: CGFloat = 0
                            while y < size.height {
                                let rect = CGRect(x: x - 1.5, y: y - 1.5, width: 3, height: 3)
                                ctx.fill(Path(ellipseIn: rect), with: .color(dot))
                                y += step
                            }
                            x += step
                        }
                    }
                }
            }
        }
    }
}

// MARK: Reusable bits

struct SwiftKicker: View {
    let text: String
    let color: Color
    var body: some View {
        Text(text.uppercased())
            .font(.system(size: 10, design: .monospaced))
            .fontWeight(.bold)
            .tracking(1)
            .foregroundColor(color)
    }
}

struct SwiftTag: View {
    let text: String
    var emoji: String? = nil
    var bg: Color
    var fg: Color
    var body: some View {
        HStack(spacing: 4) {
            if let e = emoji { Text(e).font(.system(size: 10)) }
            Text(text)
                .font(.system(size: 11, design: .monospaced))
                .foregroundColor(fg)
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 4)
        .background(bg)
        .cornerRadius(999)
    }
}

struct SwiftChip: View {
    let label: String
    var count: Int? = nil
    let active: Bool
    let palette: SwiftBrandPalette
    let theme: SwiftTheme
    let action: () -> Void
    var body: some View {
        Button(action: action) {
            HStack(spacing: 5) {
                Text(label)
                    .font(.system(size: 12, weight: .semibold))
                if let c = count {
                    Text("\(c)")
                        .font(.system(size: 10, design: .monospaced))
                        .opacity(0.7)
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 7)
            .background(active ? theme.paletteTint(palette) : theme.surface)
            .foregroundColor(active ? palette.primary : theme.inkMute)
            .overlay(
                RoundedRectangle(cornerRadius: 999)
                    .stroke(active ? theme.paletteBorder(palette) : theme.line, lineWidth: 1)
            )
            .cornerRadius(999)
        }
        .buttonStyle(PlainButtonStyle())
    }
}

// Signature chunky button
struct SwiftChunkyButton: View {
    let text: String
    let palette: SwiftBrandPalette
    var variant: String = "primary" // "primary", "outline", "ghost"
    var size: String = "md"
    var fullWidth: Bool = false
    var shadowColor: Color? = nil
    var action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(text.uppercased())
                .font(.system(size: size == "sm" ? 12 : 14, design: .monospaced))
                .fontWeight(.bold)
                .tracking(1.2)
                .padding(size == "sm" ? 8 : 12)
                .frame(maxWidth: fullWidth ? .infinity : nil)
                .background(variant == "primary" ? palette.primary : Color.clear)
                .foregroundColor(variant == "primary" ? .white : (variant == "ghost" ? SwiftTheme.danger : palette.primary))
                .cornerRadius(12)
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(variant == "ghost" ? Color.clear : (variant == "outline" ? palette.primary : palette.primary), lineWidth: 1.5)
                )
        }
        .buttonStyle(PlainButtonStyle())
    }
}

struct SwiftTabBar: View {
    @Binding var activeTab: String
    let palette: SwiftBrandPalette
    let theme: SwiftTheme

    var body: some View {
        HStack {
            tabButton(id: "home", label: "Home", icon: "house.fill")
            tabButton(id: "reminders", label: "Quiet", icon: "bell.slash.fill")
            tabButton(id: "fridge", label: "Fridge", icon: "thermometer.snowflake")
            tabButton(id: "shopping", label: "List", icon: "cart.fill")
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 8)
        .background(theme.surface)
        .cornerRadius(999)
        .overlay(
            RoundedRectangle(cornerRadius: 999)
                .stroke(theme.line, lineWidth: 1)
        )
        .padding(.horizontal, 20)
        .padding(.bottom, 16)
    }

    func tabButton(id: String, label: String, icon: String) -> some View {
        let active = activeTab == id
        return Button(action: { activeTab = id }) {
            VStack(spacing: 2) {
                Image(systemName: icon)
                    .font(.system(size: 18))
                Text(label.uppercased())
                    .font(.system(size: 9, design: .monospaced))
                    .fontWeight(.bold)
                    .tracking(0.8)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 8)
            .background(active ? palette.primary : Color.clear)
            .foregroundColor(active ? .white : theme.inkMute)
            .cornerRadius(999)
        }
        .buttonStyle(PlainButtonStyle())
    }
}

// MARK: Home

struct SwiftHomeScreen: View {
    @ObservedObject var store: ToolboxObservableState
    @Binding var activeTab: String
    let palette: SwiftBrandPalette
    let theme: SwiftTheme
    let showFlourishes: Bool

    var todayReminders: [Reminder] {
        store.state.reminders.filter { $0.on && $0.dueToday }.sorted { $0.time < $1.time }
    }
    var expiringSoon: [(item: FridgeItem, days: Int)] {
        store.state.fridge
            .map { (item: $0, days: daysUntil($0.expiry)) }
            .filter { $0.days <= 3 }
            .sorted { $0.days < $1.days }
    }
    var greeting: String {
        let h = Int(DateUtils.shared.getCurrentHour())
        switch h {
        case ..<5: return "Up late?"
        case ..<12: return "Good morning"
        case ..<18: return "Afternoon"
        default: return "Evening"
        }
    }
    var activeReminderCount: Int { store.state.reminders.filter { $0.on }.count }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                // Header
                HStack {
                    Text("🕹")
                    Text(showFlourishes ? "toolbox" : "8bit toolbox")
                        .font(showFlourishes ? .system(.subheadline, design: .monospaced) : .headline)
                        .foregroundColor(theme.ink)
                    Spacer()
                    HStack(spacing: 4) {
                        Circle().fill(SwiftTheme.ok).frame(width: 6, height: 6)
                        SwiftKicker(text: "Online", color: SwiftTheme.ok)
                    }
                    // Settings entry point (no longer a tab)
                    Button(action: { activeTab = "me" }) {
                        Image(systemName: "gearshape.fill")
                            .foregroundColor(theme.inkMute)
                            .frame(width: 34, height: 34)
                            .background(theme.surface)
                            .cornerRadius(10)
                            .overlay(RoundedRectangle(cornerRadius: 10).stroke(theme.line, lineWidth: 1))
                    }
                    .buttonStyle(PlainButtonStyle())
                }
                .padding(.top, 12)

                VStack(alignment: .leading, spacing: 2) {
                    Text(greeting)
                        .font(.system(.largeTitle, design: .serif))
                        .foregroundColor(theme.ink)
                    Text(DateUtils.shared.getTodayFormatted())
                        .font(.subheadline)
                        .foregroundColor(theme.inkMute)
                }

                // Today's nudges
                dashCard(kicker: "Today's quiet nudges", kickerColor: palette.primary,
                         count: "\(todayReminders.count) of \(activeReminderCount)",
                         onOpen: { activeTab = "reminders" }) {
                    if todayReminders.isEmpty {
                        Text("Nothing today. Quiet day.")
                            .font(.subheadline).foregroundColor(theme.inkMute)
                    } else {
                        ForEach(todayReminders.prefix(4), id: \.id) { r in
                            HStack {
                                Button(action: { store.toggleDone(id: r.id) }) {
                                    Image(systemName: store.state.doneIds.contains(r.id) ? "checkmark.square.fill" : "square")
                                        .foregroundColor(store.state.doneIds.contains(r.id) ? palette.primary : theme.inkMute)
                                }.buttonStyle(PlainButtonStyle())
                                VStack(alignment: .leading) {
                                    Text(r.title)
                                        .fontWeight(.semibold)
                                        .foregroundColor(store.state.doneIds.contains(r.id) ? theme.inkMute : theme.ink)
                                    Text("\(format12hTime(r.time)) · 🔕 \(r.mode)")
                                        .font(.caption).foregroundColor(theme.inkMute)
                                }
                                Spacer()
                            }
                            .padding(.vertical, 4)
                        }
                    }
                }

                // Use soon
                dashCard(kicker: "Use these soon", kickerColor: SwiftTheme.warn,
                         count: "\(expiringSoon.count) item\(expiringSoon.count == 1 ? "" : "s")",
                         onOpen: { activeTab = "fridge" }) {
                    if expiringSoon.isEmpty {
                        Text("Nothing about to expire. Nice fridge.")
                            .font(.subheadline).foregroundColor(theme.inkMute)
                    } else {
                        HStack(spacing: 8) {
                            ForEach(expiringSoon.prefix(4), id: \.item.id) { entry in
                                HStack(spacing: 0) {
                                    Rectangle().fill(urgencyColor(entry.days)).frame(width: 5)
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(entry.item.name)
                                            .font(.system(.subheadline, design: .serif))
                                            .fontWeight(.semibold)
                                            .foregroundColor(theme.ink)
                                            .lineLimit(1)
                                        Text(formatDaysLabel(entry.days))
                                            .font(.system(size: 9, design: .monospaced))
                                            .fontWeight(.bold)
                                            .foregroundColor(urgencyColor(entry.days))
                                    }
                                    .padding(.horizontal, 8).padding(.vertical, 6)
                                    Spacer(minLength: 0)
                                }
                                .frame(maxWidth: .infinity)
                                .background(theme.bgSubtle)
                                .cornerRadius(10)
                                .overlay(RoundedRectangle(cornerRadius: 10).stroke(theme.line, lineWidth: 1))
                            }
                        }
                    }
                }

                // Today's meals
                let today = DateUtils.shared.getTodayPlusDays(days: 0)
                let todaysMeals: [(String, String)] = mealSlots.compactMap { s in
                    guard let entry = store.state.mealPlan.first(where: { $0.date == today && $0.slot == s.key }),
                          let recipe = store.state.recipes.first(where: { $0.id == entry.recipeId }) else { return nil }
                    return (s.label, recipe.name)
                }
                dashCard(kicker: "Today's meals", kickerColor: theme.fridgeAccent,
                         count: "\(todaysMeals.count) planned",
                         onOpen: { activeTab = "mealplanner" }) {
                    if todaysMeals.isEmpty {
                        Text("No meals planned today. Tap to plan.")
                            .font(.subheadline).foregroundColor(theme.inkMute)
                    } else {
                        ForEach(Array(todaysMeals.enumerated()), id: \.offset) { _, m in
                            HStack {
                                Text(m.0.uppercased()).font(.system(size: 9, weight: .bold, design: .monospaced))
                                    .foregroundColor(theme.inkMute).frame(width: 72, alignment: .leading)
                                Text(m.1).fontWeight(.semibold).foregroundColor(theme.ink)
                                Spacer()
                            }.padding(.vertical, 6)
                        }
                    }
                }

                // Shopping
                let uncheckedItems = store.state.shoppingList.filter { !$0.checked }
                dashCard(kicker: "Shopping list", kickerColor: theme.shoppingAccent,
                         count: "\(uncheckedItems.count) left",
                         onOpen: { activeTab = "shopping" }) {
                    if store.state.shoppingList.isEmpty {
                        Text("List is empty. Tap to add items.")
                            .font(.subheadline).foregroundColor(theme.inkMute)
                    } else {
                        ForEach(uncheckedItems.prefix(3), id: \.id) { item in
                            HStack {
                                Circle().stroke(theme.inkMute, lineWidth: 1.5).frame(width: 8, height: 8)
                                Text(item.name).fontWeight(.semibold).foregroundColor(theme.ink)
                                Spacer()
                                if !item.qty.isEmpty {
                                    Text(item.qty).font(.system(size: 10, design: .monospaced)).foregroundColor(theme.inkMute)
                                }
                            }.padding(.vertical, 6)
                        }
                    }
                }

                // Tools grid launcher
                SwiftKicker(text: "Your tools", color: palette.primary)
                VStack(spacing: 12) {
                    HStack(spacing: 12) {
                        toolTile(label: "Quiet Reminders", sub: "\(activeReminderCount) active", emoji: "🔕", color: palette.primary, tint: theme.paletteTint(palette), tab: "reminders")
                        toolTile(label: "Fridge", sub: "\(store.state.fridge.count) items", emoji: "❄", color: theme.fridgeAccent, tint: theme.fridgeTint, tab: "fridge")
                    }
                    HStack(spacing: 12) {
                        toolTile(label: "Shopping", sub: "\(uncheckedItems.count) items left", emoji: "🛒", color: theme.shoppingAccent, tint: theme.shoppingTint, tab: "shopping")
                        toolTile(label: "Recipes", sub: "\(store.state.recipes.count) saved", emoji: "🍳", color: palette.primary, tint: theme.paletteTint(palette), tab: "recipes")
                    }
                    HStack(spacing: 12) {
                        toolTile(label: "Meal Planner", sub: "Plan the week", emoji: "📅", color: theme.fridgeAccent, tint: theme.fridgeTint, tab: "mealplanner")
                        Color.clear.frame(maxWidth: .infinity)
                    }
                }
            }
            .padding(.horizontal)
            .padding(.bottom, 24)
        }
    }

    @ViewBuilder
    func dashCard<Content: View>(kicker: String, kickerColor: Color, count: String, onOpen: @escaping () -> Void, @ViewBuilder content: () -> Content) -> some View {
        Button(action: onOpen) {
            VStack(alignment: .leading, spacing: 6) {
                HStack {
                    SwiftKicker(text: kicker, color: kickerColor)
                    Spacer()
                    Text("\(count) →")
                        .font(.system(size: 11, design: .monospaced))
                        .foregroundColor(theme.inkMute)
                }
                content()
            }
            .padding(16)
            .frame(maxWidth: .infinity, alignment: .leading)
            .toolboxCard(theme)
        }
        .buttonStyle(PlainButtonStyle())
    }

    func toolTile(label: String, sub: String, emoji: String, color: Color, tint: Color, tab: String?) -> some View {
        Button(action: { if let t = tab { activeTab = t } }) {
            VStack(alignment: .leading, spacing: 10) {
                Text(emoji)
                    .font(.system(size: 20))
                    .frame(width: 40, height: 40)
                    .background(tint)
                    .cornerRadius(10)
                VStack(alignment: .leading, spacing: 3) {
                    Text(label).font(.system(size: 14, weight: .bold)).foregroundColor(tab == nil ? theme.inkMute : theme.ink)
                    Text(sub).font(.system(size: 10, design: .monospaced)).foregroundColor(theme.inkMute)
                }
            }
            .frame(maxWidth: .infinity, minHeight: 96, alignment: .leading)
            .padding(14)
            .toolboxCard(theme)
        }
        .buttonStyle(PlainButtonStyle())
        .disabled(tab == nil)
    }
}

// MARK: Reminders

struct SwiftRemindersScreen: View {
    @ObservedObject var store: ToolboxObservableState
    let palette: SwiftBrandPalette
    let theme: SwiftTheme

    @State private var filter = "all"
    @State private var expandedId: String? = nil
    @State private var sheetOpen = false
    @State private var editing: Reminder? = nil
    @State private var notifDenied = false

    private func refreshNotifStatus() {
        UNUserNotificationCenter.current().getNotificationSettings { settings in
            DispatchQueue.main.async {
                notifDenied = settings.authorizationStatus == .denied
            }
        }
    }

    var all: [Reminder] { store.state.reminders }
    func count(_ key: String) -> Int {
        switch key {
        case "today": return all.filter { $0.dueToday && $0.on }.count
        case "repeat": return all.filter { !$0.`repeat`.isEmpty }.count
        case "off": return all.filter { !$0.on }.count
        default: return all.count
        }
    }
    var visible: [Reminder] {
        all.filter { r in
            switch filter {
            case "today": return r.on && r.dueToday
            case "repeat": return !r.`repeat`.isEmpty
            case "off": return !r.on
            default: return true
            }
        }
    }

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    VStack(alignment: .leading, spacing: 4) {
                        SwiftKicker(text: "Silent · No ring", color: palette.primary)
                        Text("Quiet nudges")
                            .font(.system(.largeTitle, design: .serif))
                            .foregroundColor(theme.ink)
                    }
                    .padding(.top, 8)

                    // Notifications-off warning — reminders won't actually alert without it
                    if notifDenied {
                        HStack(spacing: 10) {
                            Text("🔔").font(.system(size: 16))
                            VStack(alignment: .leading, spacing: 2) {
                                Text("Notifications are off").font(.system(size: 13, weight: .bold)).foregroundColor(theme.ink)
                                Text("Reminders won't alert you until you turn them on.").font(.system(size: 11)).foregroundColor(theme.inkSoft)
                            }
                            Spacer(minLength: 6)
                            Button("Enable") {
                                if let url = URL(string: UIApplication.openSettingsURLString) {
                                    UIApplication.shared.open(url)
                                }
                            }
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(palette.primary)
                        }
                        .padding(.horizontal, 14).padding(.vertical, 10)
                        .background(SwiftTheme.danger.opacity(0.12))
                        .cornerRadius(14)
                        .overlay(RoundedRectangle(cornerRadius: 14).stroke(SwiftTheme.danger.opacity(0.4), lineWidth: 1))
                    }

                    // Quiet hours banner
                    HStack {
                        Text("🌙").font(.system(size: 16))
                            .frame(width: 32, height: 32)
                            .background(theme.surface)
                            .cornerRadius(8)
                            .overlay(RoundedRectangle(cornerRadius: 8).stroke(theme.paletteBorder(palette), lineWidth: 1))
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Quiet hours · 10 PM – 7 AM").font(.system(size: 13, weight: .bold)).foregroundColor(theme.ink)
                            Text("Reminders held silently until morning").font(.system(size: 11)).foregroundColor(theme.inkSoft)
                        }
                        Spacer()
                        Toggle("", isOn: Binding(get: { store.state.quietHoursOn }, set: { store.setQuiet(on: $0) }))
                            .labelsHidden().tint(palette.primary)
                    }
                    .padding(.horizontal, 14).padding(.vertical, 10)
                    .background(theme.paletteTint(palette))
                    .cornerRadius(14)
                    .overlay(RoundedRectangle(cornerRadius: 14).stroke(theme.paletteBorder(palette), lineWidth: 1))

                    // Filters
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 6) {
                            ForEach([("all", "All"), ("today", "Today"), ("repeat", "Repeating"), ("off", "Off")], id: \.0) { key, label in
                                SwiftChip(label: label, count: count(key), active: filter == key, palette: palette, theme: theme) { filter = key }
                            }
                        }
                    }

                    if visible.isEmpty {
                        Text("Nothing in this filter.")
                            .font(.subheadline).foregroundColor(theme.inkMute)
                            .frame(maxWidth: .infinity).padding(.vertical, 40)
                            .overlay(RoundedRectangle(cornerRadius: 14).stroke(theme.line, lineWidth: 1.5))
                    } else {
                        ForEach(visible, id: \.id) { r in
                            reminderCard(r)
                        }
                    }
                }
                .padding(.horizontal)
                .padding(.bottom, 110)
            }

            // FAB
            SwiftChunkyButton(text: "+ Nudge", palette: palette, size: "sm") {
                editing = nil
                sheetOpen = true
            }
            .padding(.trailing, 16).padding(.bottom, 96)
        }
        .sheet(isPresented: $sheetOpen) {
            ReminderFormView(palette: palette, theme: theme, initial: editing) { title, time, repeatVal, mode in
                if let e = editing {
                    store.updateReminder(id: e.id, title: title, time: time, repeat: repeatVal, mode: mode)
                } else {
                    store.addReminder(title: title, time: time, repeat: repeatVal, mode: mode)
                }
                sheetOpen = false
            } onCancel: { sheetOpen = false }
        }
        .onAppear { refreshNotifStatus() }
    }

    func reminderCard(_ r: Reminder) -> some View {
        let expanded = expandedId == r.id
        let scheduleStr = r.`repeat`.isEmpty
            ? "\(format12hTime(r.time)) · \(r.dueToday ? "today" : "once")"
            : "\(format12hTime(r.time)) · \(r.`repeat`)"
        return VStack(spacing: 0) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 6) {
                    Text(r.title)
                        .font(.system(.title3, design: .serif))
                        .foregroundColor(theme.ink)
                    HStack(spacing: 6) {
                        SwiftTag(text: scheduleStr, emoji: "🕒", bg: theme.bgSubtle, fg: theme.inkMute)
                        SwiftTag(text: r.mode, emoji: "🔕", bg: theme.paletteTint(palette), fg: palette.primary)
                    }
                }
                Spacer()
                Toggle("", isOn: Binding(get: { r.on }, set: { store.setOn(id: r.id, on: $0) }))
                    .labelsHidden().tint(palette.primary)
            }
            .padding(14)

            if expanded {
                Divider().background(theme.line)
                VStack(alignment: .leading, spacing: 8) {
                    SwiftKicker(text: "How it alerts", color: theme.inkMute)
                    ForEach(alertModes, id: \.key) { m in
                        Button(action: { store.setMode(id: r.id, mode: m.key) }) {
                            HStack {
                                Text(m.emoji)
                                VStack(alignment: .leading) {
                                    Text(m.label).font(.system(size: 12, weight: .bold)).foregroundColor(theme.ink)
                                    Text(m.sub).font(.system(size: 10)).foregroundColor(theme.inkMute)
                                }
                                Spacer()
                                if r.mode == m.key {
                                    Image(systemName: "checkmark").foregroundColor(palette.primary)
                                }
                            }
                            .padding(10)
                            .background(r.mode == m.key ? theme.paletteTint(palette) : theme.surface)
                            .cornerRadius(10)
                            .overlay(RoundedRectangle(cornerRadius: 10).stroke(r.mode == m.key ? theme.paletteBorder(palette) : theme.line, lineWidth: 1.5))
                        }
                        .buttonStyle(PlainButtonStyle())
                    }
                    HStack {
                        SwiftChunkyButton(text: "Delete", palette: palette, variant: "ghost", size: "sm") {
                            expandedId = nil
                            store.deleteReminder(id: r.id)
                        }
                        Spacer()
                        SwiftChunkyButton(text: "Edit details", palette: palette, size: "sm") {
                            editing = r
                            expandedId = nil
                            sheetOpen = true
                        }
                    }
                    .padding(.top, 4)
                }
                .padding(14)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(theme.bg)
            }
        }
        .toolboxCard(theme)
        .onTapGesture { expandedId = expanded ? nil : r.id }
    }
}

struct ReminderFormView: View {
    let palette: SwiftBrandPalette
    let theme: SwiftTheme
    let initial: Reminder?
    let onSave: (String, String, String, String) -> Void
    let onCancel: () -> Void

    @State private var title = ""
    @State private var time = "09:00"
    @State private var repeatVal = ""
    @State private var mode = "banner"

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    field("What") {
                        TextField("Water plants", text: $title)
                            .textFieldStyle(RoundedBorderTextFieldStyle())
                    }
                    field("Time") {
                        TextField("09:00", text: Binding(
                            get: { time },
                            set: { time = String($0.prefix(5)) }
                        ))
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                    }
                    field("Repeat") {
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 6) {
                                ForEach(repeatOptions, id: \.value) { opt in
                                    SwiftChip(label: opt.label, active: repeatVal == opt.value, palette: palette, theme: theme) { repeatVal = opt.value }
                                }
                            }
                        }
                    }
                    field("Alert mode") {
                        VStack(spacing: 6) {
                            ForEach(alertModes, id: \.key) { m in
                                Button(action: { mode = m.key }) {
                                    HStack {
                                        Text(m.emoji)
                                        Text(m.label).font(.system(size: 12, weight: .bold)).foregroundColor(theme.ink)
                                        Spacer()
                                        if mode == m.key { Image(systemName: "checkmark").foregroundColor(palette.primary) }
                                    }
                                    .padding(10)
                                    .background(mode == m.key ? theme.paletteTint(palette) : theme.surface)
                                    .cornerRadius(10)
                                    .overlay(RoundedRectangle(cornerRadius: 10).stroke(mode == m.key ? theme.paletteBorder(palette) : theme.line, lineWidth: 1.5))
                                }
                                .buttonStyle(PlainButtonStyle())
                            }
                        }
                        Text("None of these will make sound.").font(.system(size: 11)).foregroundColor(theme.inkMute)
                    }
                }
                .padding()
            }
            .background(theme.bgSubtle.ignoresSafeArea())
            .navigationTitle(initial != nil ? "Edit nudge" : "New quiet nudge")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel", action: onCancel)
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") { onSave(title, time, repeatVal, mode) }
                        .disabled(title.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
        }
        .onAppear {
            if let i = initial {
                title = i.title; time = i.time; repeatVal = i.`repeat`; mode = i.mode
            }
        }
    }

    @ViewBuilder
    func field<Content: View>(_ label: String, @ViewBuilder content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label).font(.system(size: 12, weight: .bold)).foregroundColor(theme.inkMute)
            content()
        }
    }
}

// MARK: Fridge

struct SwiftFridgeScreen: View {
    @ObservedObject var store: ToolboxObservableState
    let palette: SwiftBrandPalette
    let theme: SwiftTheme

    @State private var expandedId: String? = nil
    @State private var sheetOpen = false
    @State private var editing: FridgeItem? = nil

    var sorted: [FridgeItem] {
        store.state.fridge.sorted { daysUntil($0.expiry) < daysUntil($1.expiry) }
    }

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    VStack(alignment: .leading, spacing: 4) {
                        SwiftKicker(text: "Cold storage", color: theme.fridgeAccent)
                        Text("Fridge")
                            .font(.system(.largeTitle, design: .serif))
                            .foregroundColor(theme.ink)
                    }
                    .padding(.top, 8)

                    if sorted.isEmpty {
                        Text("Fridge is empty. Tap + to add what you have.")
                            .font(.subheadline).foregroundColor(theme.inkMute)
                            .frame(maxWidth: .infinity).padding(.vertical, 40)
                            .overlay(RoundedRectangle(cornerRadius: 14).stroke(theme.line, lineWidth: 1.5))
                    } else {
                        ForEach(sorted, id: \.id) { item in
                            fridgeCard(item)
                        }
                    }
                }
                .padding(.horizontal)
                .padding(.bottom, 110)
            }

            SwiftChunkyButton(text: "+ Item", palette: palette, size: "sm") {
                editing = nil
                sheetOpen = true
            }
            .padding(.trailing, 16).padding(.bottom, 96)
        }
        .sheet(isPresented: $sheetOpen) {
            FridgeFormView(palette: palette, theme: theme, initial: editing) { name, qty, expiry, location in
                if let e = editing {
                    store.updateFridge(id: e.id, name: name, qty: qty, expiry: expiry, location: location)
                } else {
                    store.addFridge(name: name, qty: qty, expiry: expiry, location: location)
                }
                sheetOpen = false
            } onCancel: { sheetOpen = false }
        }
    }

    func fridgeCard(_ item: FridgeItem) -> some View {
        let expanded = expandedId == item.id
        let days = daysUntil(item.expiry)
        return VStack(spacing: 0) {
            HStack(spacing: 0) {
                Rectangle().fill(urgencyColor(days)).frame(width: 5)
                VStack(alignment: .leading, spacing: 4) {
                    Text(item.name).font(.system(.headline, design: .serif)).foregroundColor(theme.ink)
                    Text("\(item.qty) · \(item.location)").font(.subheadline).foregroundColor(theme.inkMute)
                }
                .padding(.leading, 12).padding(.vertical, 12)
                Spacer()
                VStack(alignment: .trailing, spacing: 2) {
                    Text(formatDaysLabel(days))
                        .font(.system(size: 10, design: .monospaced)).fontWeight(.bold)
                        .foregroundColor(urgencyColor(days))
                    Text(item.expiry).font(.system(size: 10, design: .monospaced)).foregroundColor(theme.inkMute)
                }
                .padding(.trailing, 12)
            }

            if expanded {
                Divider().background(theme.line)
                HStack {
                    SwiftChunkyButton(text: "Used it", palette: palette, variant: "ghost", size: "sm") {
                        expandedId = nil
                        store.consumeFridge(id: item.id)
                    }
                    SwiftChunkyButton(text: "Restock", palette: palette, variant: "outline", size: "sm") {
                        store.restockFridgeItem(id: item.id)
                    }
                    SwiftChunkyButton(text: "Nudge", palette: palette, variant: "outline", size: "sm") {
                        store.nudgeFromFridge(name: item.name)
                    }
                    SwiftChunkyButton(text: "Edit", palette: palette, size: "sm") {
                        editing = item
                        expandedId = nil
                        sheetOpen = true
                    }
                }
                .padding(12)
            }
        }
        .toolboxCard(theme)
        .onTapGesture { expandedId = expanded ? nil : item.id }
    }
}

struct FridgeFormView: View {
    let palette: SwiftBrandPalette
    let theme: SwiftTheme
    let initial: FridgeItem?
    let onSave: (String, String, String, String) -> Void
    let onCancel: () -> Void

    @State private var name = ""
    @State private var qty = ""
    @State private var location = "fridge"
    @State private var expiry = ""

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    field("Item") {
                        TextField("Milk", text: $name).textFieldStyle(RoundedBorderTextFieldStyle())
                    }
                    field("Quantity") {
                        TextField("1 carton", text: $qty).textFieldStyle(RoundedBorderTextFieldStyle())
                    }
                    field("Location") {
                        HStack(spacing: 6) {
                            ForEach(fridgeLocations, id: \.self) { loc in
                                SwiftChip(label: loc, active: location == loc, palette: palette, theme: theme) { location = loc }
                            }
                        }
                    }
                    field("Expiry (YYYY-MM-DD)") {
                        TextField("2026-01-01", text: $expiry).textFieldStyle(RoundedBorderTextFieldStyle())
                    }
                }
                .padding()
            }
            .background(theme.bgSubtle.ignoresSafeArea())
            .navigationTitle(initial != nil ? "Edit item" : "New item")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button("Cancel", action: onCancel) }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        onSave(name, qty.isEmpty ? "1" : qty, expiry, location)
                    }
                    .disabled(name.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
        }
        .onAppear {
            if let i = initial {
                name = i.name; qty = i.qty; location = i.location; expiry = i.expiry
            } else {
                expiry = DateUtils.shared.getTodayPlusDays(days: 7)
            }
        }
    }

    @ViewBuilder
    func field<Content: View>(_ label: String, @ViewBuilder content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label).font(.system(size: 12, weight: .bold)).foregroundColor(theme.inkMute)
            content()
        }
    }
}

// MARK: Shopping

struct SwiftShoppingScreen: View {
    @ObservedObject var store: ToolboxObservableState
    let palette: SwiftBrandPalette
    let theme: SwiftTheme

    @State private var newName = ""
    @State private var newQty = ""
    @State private var fridgeMove: FridgeMove? = nil

    var uncheckedItems: [ShoppingListItem] { store.state.shoppingList.filter { !$0.checked } }
    var checkedItems: [ShoppingListItem] { store.state.shoppingList.filter { $0.checked } }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                VStack(alignment: .leading, spacing: 4) {
                    SwiftKicker(text: "Need to buy", color: theme.shoppingAccent)
                    Text("Shopping list")
                        .font(.system(.largeTitle, design: .serif))
                        .foregroundColor(theme.ink)
                }
                .padding(.top, 8)

                // Add row
                HStack {
                    TextField("Item", text: $newName).textFieldStyle(RoundedBorderTextFieldStyle())
                    TextField("Qty", text: $newQty).textFieldStyle(RoundedBorderTextFieldStyle()).frame(width: 70)
                    Button(action: {
                        let name = newName.trimmingCharacters(in: .whitespaces)
                        guard !name.isEmpty else { return }
                        store.addShoppingItem(name: name, qty: newQty.isEmpty ? "1" : newQty)
                        newName = ""; newQty = ""
                    }) {
                        Image(systemName: "plus.circle.fill").font(.title2).foregroundColor(palette.primary)
                    }
                }

                if store.state.shoppingList.isEmpty {
                    Text("List is empty. Add your first item above.")
                        .font(.subheadline).foregroundColor(theme.inkMute)
                        .frame(maxWidth: .infinity).padding(.vertical, 40)
                        .overlay(RoundedRectangle(cornerRadius: 14).stroke(theme.line, lineWidth: 1.5))
                }

                ForEach(uncheckedItems, id: \.id) { item in shoppingRow(item) }

                if !checkedItems.isEmpty {
                    HStack {
                        SwiftKicker(text: "In cart", color: theme.inkMute)
                        Spacer()
                        Button("To Fridge") {
                            fridgeMove = .bulk(checkedItems.count)
                        }
                        .font(.caption).foregroundColor(palette.primary)
                        Button("Clear") { store.clearCheckedShoppingItems() }
                            .font(.caption).foregroundColor(SwiftTheme.danger)
                    }
                    ForEach(checkedItems, id: \.id) { item in shoppingRow(item) }
                }
            }
            .padding(.horizontal)
            .padding(.bottom, 110)
        }
        .sheet(item: $fridgeMove) { move in
            let message: String = {
                switch move {
                case .single(let item): return "Moving \(item.name) from your list into the fridge tracker."
                case .bulk(let n): return "Moving \(n) bought item\(n == 1 ? "" : "s") from your list into the fridge tracker."
                }
            }()
            SendToFridgeView(palette: palette, theme: theme, message: message) { location, expiry in
                switch move {
                case .single(let item):
                    store.purchaseShoppingItem(id: item.id, location: location, expiry: expiry)
                case .bulk:
                    store.purchaseCheckedShoppingItems(location: location, expiry: expiry)
                }
                fridgeMove = nil
            } onCancel: { fridgeMove = nil }
        }
    }

    func shoppingRow(_ item: ShoppingListItem) -> some View {
        HStack {
            Button(action: { store.toggleShoppingItem(id: item.id) }) {
                Image(systemName: item.checked ? "checkmark.circle.fill" : "circle")
                    .foregroundColor(item.checked ? SwiftTheme.ok : theme.inkMute)
            }.buttonStyle(PlainButtonStyle())
            VStack(alignment: .leading) {
                Text(item.name)
                    .fontWeight(.semibold)
                    .strikethrough(item.checked)
                    .foregroundColor(item.checked ? theme.inkMute : theme.ink)
                if !item.qty.isEmpty {
                    Text(item.qty).font(.caption).foregroundColor(theme.inkMute)
                }
            }
            Spacer()
            if item.checked {
                Button(action: {
                    fridgeMove = .single(item)
                }) {
                    Image(systemName: "snowflake").foregroundColor(SwiftTheme.cyan)
                }.buttonStyle(PlainButtonStyle())
            }
            Button(action: { store.deleteShoppingItem(id: item.id) }) {
                Image(systemName: "trash").foregroundColor(SwiftTheme.danger)
            }.buttonStyle(PlainButtonStyle())
        }
        .padding()
        .toolboxCard(theme, radius: 14)
    }
}

// Describes a pending shopping→fridge move (one item or all checked items).
enum FridgeMove: Identifiable {
    case single(ShoppingListItem)
    case bulk(Int)
    var id: String {
        switch self {
        case .single(let item): return "single-\(item.id)"
        case .bulk: return "bulk"
        }
    }
}

// Picks the destination + best-before date when moving items into the fridge,
// mirroring Android's SendToFridgeForm.
struct SendToFridgeView: View {
    let palette: SwiftBrandPalette
    let theme: SwiftTheme
    let message: String
    let onSave: (String, String) -> Void
    let onCancel: () -> Void

    @State private var location = "fridge"
    @State private var expiry = ""

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    HStack(alignment: .top, spacing: 10) {
                        Text("❄").font(.system(size: 16))
                        Text(message).font(.system(size: 13)).foregroundColor(theme.inkSoft)
                        Spacer(minLength: 0)
                    }
                    .padding(.horizontal, 14).padding(.vertical, 10)
                    .background(theme.paletteTint(palette))
                    .cornerRadius(12)
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(theme.paletteBorder(palette), lineWidth: 1))

                    field("Best before (YYYY-MM-DD)") {
                        TextField("2026-01-01", text: $expiry).textFieldStyle(RoundedBorderTextFieldStyle())
                    }
                    field("Where") {
                        HStack(spacing: 6) {
                            ForEach(fridgeLocations, id: \.self) { loc in
                                SwiftChip(label: loc, active: location == loc, palette: palette, theme: theme) { location = loc }
                            }
                        }
                    }
                }
                .padding()
            }
            .background(theme.bgSubtle.ignoresSafeArea())
            .navigationTitle("Add to fridge")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button("Cancel", action: onCancel) }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Add") { onSave(location, expiry) }
                }
            }
        }
        .onAppear {
            if expiry.isEmpty { expiry = DateUtils.shared.getTodayPlusDays(days: 7) }
        }
    }

    @ViewBuilder
    func field<Content: View>(_ label: String, @ViewBuilder content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label).font(.system(size: 12, weight: .bold)).foregroundColor(theme.inkMute)
            content()
        }
    }
}

// MARK: Recipes

struct SwiftRecipesScreen: View {
    @ObservedObject var store: ToolboxObservableState
    let palette: SwiftBrandPalette
    let theme: SwiftTheme

    @State private var detail: Recipe? = nil
    @State private var editing: Recipe? = nil
    @State private var showForm = false

    var recipes: [Recipe] { store.state.recipes.sorted { $0.name.lowercased() < $1.name.lowercased() } }

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            ScrollView {
                VStack(alignment: .leading, spacing: 10) {
                    VStack(alignment: .leading, spacing: 4) {
                        SwiftKicker(text: "\(recipes.count) saved", color: palette.primary)
                        Text("Recipes").font(.system(.largeTitle, design: .serif)).foregroundColor(theme.ink)
                    }.padding(.top, 8)

                    if recipes.isEmpty {
                        Text("No recipes yet. Tap + to save your first one.")
                            .font(.subheadline).foregroundColor(theme.inkMute)
                            .frame(maxWidth: .infinity).padding(.vertical, 40)
                            .overlay(RoundedRectangle(cornerRadius: 14).stroke(theme.line, lineWidth: 1.5))
                    }
                    ForEach(recipes, id: \.id) { r in
                        Button(action: { detail = r }) { recipeRow(r) }.buttonStyle(PlainButtonStyle())
                    }
                }
                .padding(.horizontal).padding(.bottom, 110)
            }
            SwiftChunkyButton(text: "+ Recipe", palette: palette, size: "sm") {
                editing = nil; showForm = true
            }.padding(.trailing, 16).padding(.bottom, 96)
        }
        .sheet(item: $detail) { r in
            RecipeDetailView(
                store: store, recipe: r, palette: palette, theme: theme,
                onEdit: { editing = r; detail = nil; showForm = true },
                onClose: { detail = nil }
            )
        }
        .sheet(isPresented: $showForm) {
            RecipeFormView(palette: palette, theme: theme, initial: editing) { name, ingredients, steps in
                if let e = editing {
                    store.updateRecipe(id: e.id, name: name, ingredients: ingredients, steps: steps)
                } else {
                    store.addRecipe(name: name, ingredients: ingredients, steps: steps)
                }
                showForm = false
            } onCancel: { showForm = false }
        }
    }

    func recipeRow(_ r: Recipe) -> some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(r.name).fontWeight(.semibold).foregroundColor(theme.ink)
                Text("\(r.ingredients.count) ingredients · \(r.steps.count) steps")
                    .font(.caption).foregroundColor(theme.inkMute)
            }
            Spacer()
            Image(systemName: "chevron.right").foregroundColor(theme.inkMute).font(.caption)
        }
        .padding().toolboxCard(theme, radius: 14)
    }
}

struct RecipeDetailView: View {
    @ObservedObject var store: ToolboxObservableState
    let recipe: Recipe
    let palette: SwiftBrandPalette
    let theme: SwiftTheme
    let onEdit: () -> Void
    let onClose: () -> Void

    @State private var planDate = ""
    @State private var planSlot = "dinner"

    var haveCount: Int {
        recipe.ingredients.filter { fridgeHasIngredient($0.name, store.state.fridge) }.count
    }

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    HStack {
                        SwiftKicker(text: "Ingredients", color: theme.inkMute)
                        Spacer()
                        Text("\(haveCount) of \(recipe.ingredients.count) in fridge")
                            .font(.system(size: 10, design: .monospaced))
                            .foregroundColor(haveCount == recipe.ingredients.count && !recipe.ingredients.isEmpty ? SwiftTheme.ok : theme.inkMute)
                    }
                    VStack(spacing: 0) {
                        ForEach(Array(recipe.ingredients.enumerated()), id: \.offset) { idx, ing in
                            let have = fridgeHasIngredient(ing.name, store.state.fridge)
                            HStack {
                                Text(have ? "✓" : "•").fontWeight(.bold)
                                    .foregroundColor(have ? SwiftTheme.ok : theme.inkMute).frame(width: 16)
                                Text(ing.name).foregroundColor(theme.ink)
                                Spacer()
                                Text(have ? "in fridge" : ing.qty)
                                    .font(.system(size: 11, design: .monospaced))
                                    .foregroundColor(have ? SwiftTheme.ok : theme.inkMute)
                            }
                            .padding(.vertical, 8)
                            if idx != recipe.ingredients.count - 1 { Divider() }
                        }
                    }
                    .padding(.horizontal, 14).background(theme.bgSubtle).cornerRadius(12)

                    SwiftKicker(text: "Add to meal plan", color: theme.inkMute)
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 6) {
                            ForEach(0..<7, id: \.self) { i in
                                let date = DateUtils.shared.getTodayPlusDays(days: Int32(i))
                                let label = i == 0 ? "Today" : (i == 1 ? "Tom" : weekdayShort(date))
                                SwiftChip(label: label, active: planDate == date, palette: palette, theme: theme) { planDate = date }
                            }
                        }
                    }
                    HStack(spacing: 6) {
                        ForEach(mealSlots, id: \.key) { s in
                            SwiftChip(label: s.label, active: planSlot == s.key, palette: palette, theme: theme) { planSlot = s.key }
                        }
                    }
                    SwiftChunkyButton(text: "📅 Add to plan", palette: palette, variant: "outline", size: "sm") {
                        store.setMealSlot(date: planDate, slot: planSlot, recipeId: recipe.id)
                        onClose()
                    }

                    if !recipe.steps.isEmpty {
                        SwiftKicker(text: "Steps", color: theme.inkMute)
                        ForEach(Array(recipe.steps.enumerated()), id: \.offset) { idx, step in
                            HStack(alignment: .top, spacing: 10) {
                                Text(String(format: "%02d", idx + 1))
                                    .font(.system(size: 12, weight: .bold, design: .monospaced))
                                    .foregroundColor(palette.primary)
                                Text(step).font(.system(size: 14)).foregroundColor(theme.inkSoft)
                            }
                        }
                    }

                    HStack(spacing: 8) {
                        SwiftChunkyButton(text: "Edit", palette: palette, variant: "outline", size: "sm", action: onEdit)
                        SwiftChunkyButton(text: "🛒 To list", palette: palette, size: "sm") {
                            store.sendRecipeToShoppingList(id: recipe.id); onClose()
                        }
                        Spacer()
                        SwiftChunkyButton(text: "Delete", palette: palette, variant: "ghost", size: "sm") {
                            store.deleteRecipe(id: recipe.id); onClose()
                        }
                    }
                }
                .padding()
            }
            .background(theme.bgSubtle.ignoresSafeArea())
            .navigationTitle(recipe.name)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar { ToolbarItem(placement: .confirmationAction) { Button("Done", action: onClose) } }
        }
        .onAppear { if planDate.isEmpty { planDate = DateUtils.shared.getTodayPlusDays(days: 0) } }
    }
}

struct EditableIngredient: Identifiable {
    let id = UUID()
    var name: String = ""
    var qty: String = ""
}
struct EditableStep: Identifiable {
    let id = UUID()
    var text: String = ""
}

struct RecipeFormView: View {
    let palette: SwiftBrandPalette
    let theme: SwiftTheme
    let initial: Recipe?
    let onSave: (String, [RecipeIngredient], [String]) -> Void
    let onCancel: () -> Void

    @State private var name = ""
    @State private var ingredients: [EditableIngredient] = [EditableIngredient()]
    @State private var steps: [EditableStep] = [EditableStep()]

    var canSave: Bool {
        !name.trimmingCharacters(in: .whitespaces).isEmpty &&
        ingredients.contains { !$0.name.trimmingCharacters(in: .whitespaces).isEmpty }
    }

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    field("Name") {
                        TextField("Garlic pasta", text: $name).textFieldStyle(RoundedBorderTextFieldStyle())
                    }
                    field("Ingredients") {
                        VStack(spacing: 6) {
                            ForEach(ingredients.indices, id: \.self) { idx in
                                HStack {
                                    TextField("Spaghetti", text: $ingredients[idx].name).textFieldStyle(RoundedBorderTextFieldStyle())
                                    TextField("200 g", text: $ingredients[idx].qty).textFieldStyle(RoundedBorderTextFieldStyle()).frame(width: 80)
                                    Button(action: {
                                        if ingredients.count > 1 { ingredients.remove(at: idx) } else { ingredients[0] = EditableIngredient() }
                                    }) { Image(systemName: "xmark.circle.fill").foregroundColor(theme.inkMute) }
                                    .buttonStyle(PlainButtonStyle())
                                }
                            }
                            Button("+ Ingredient") { ingredients.append(EditableIngredient()) }
                                .font(.caption).foregroundColor(palette.primary)
                        }
                    }
                    field("Steps") {
                        VStack(spacing: 6) {
                            ForEach(steps.indices, id: \.self) { idx in
                                HStack {
                                    Text(String(format: "%02d", idx + 1))
                                        .font(.system(size: 12, weight: .bold, design: .monospaced)).foregroundColor(palette.primary)
                                    TextField("Boil the pasta", text: $steps[idx].text).textFieldStyle(RoundedBorderTextFieldStyle())
                                    Button(action: {
                                        if steps.count > 1 { steps.remove(at: idx) } else { steps[0] = EditableStep() }
                                    }) { Image(systemName: "xmark.circle.fill").foregroundColor(theme.inkMute) }
                                    .buttonStyle(PlainButtonStyle())
                                }
                            }
                            Button("+ Step") { steps.append(EditableStep()) }
                                .font(.caption).foregroundColor(palette.primary)
                        }
                    }
                }
                .padding()
            }
            .background(theme.bgSubtle.ignoresSafeArea())
            .navigationTitle(initial != nil ? "Edit recipe" : "New recipe")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button("Cancel", action: onCancel) }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        let cleanIng = ingredients
                            .filter { !$0.name.trimmingCharacters(in: .whitespaces).isEmpty }
                            .map { RecipeIngredient(name: $0.name.trimmingCharacters(in: .whitespaces), qty: $0.qty.trimmingCharacters(in: .whitespaces)) }
                        let cleanSteps = steps.map { $0.text.trimmingCharacters(in: .whitespaces) }.filter { !$0.isEmpty }
                        onSave(name.trimmingCharacters(in: .whitespaces), cleanIng, cleanSteps)
                    }.disabled(!canSave)
                }
            }
        }
        .onAppear {
            if let r = initial {
                name = r.name
                ingredients = r.ingredients.isEmpty ? [EditableIngredient()] : r.ingredients.map { EditableIngredient(name: $0.name, qty: $0.qty) }
                steps = r.steps.isEmpty ? [EditableStep()] : r.steps.map { EditableStep(text: $0) }
            }
        }
    }

    @ViewBuilder
    func field<Content: View>(_ label: String, @ViewBuilder content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label).font(.system(size: 12, weight: .bold)).foregroundColor(theme.inkMute)
            content()
        }
    }
}

// MARK: Meal planner

struct MealSlotRef: Identifiable {
    let date: String
    let slot: String
    var id: String { "\(date)-\(slot)" }
}

struct SwiftMealPlannerScreen: View {
    @ObservedObject var store: ToolboxObservableState
    let palette: SwiftBrandPalette
    let theme: SwiftTheme

    @State private var picking: MealSlotRef? = nil

    var days: [String] { (0..<7).map { DateUtils.shared.getTodayPlusDays(days: Int32($0)) } }

    func entryFor(_ date: String, _ slot: String) -> MealPlanEntry? {
        store.state.mealPlan.first { $0.date == date && $0.slot == slot }
    }
    func recipeName(_ id: String) -> String? { store.state.recipes.first { $0.id == id }?.name }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 10) {
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        SwiftKicker(text: "Next 7 days", color: palette.primary)
                        Text("Meal planner").font(.system(.largeTitle, design: .serif)).foregroundColor(theme.ink)
                    }
                    Spacer()
                    if !store.state.mealPlan.isEmpty {
                        Button("🛒 To list") { store.sendPlannedMealsToShoppingList() }
                            .font(.caption).foregroundColor(palette.primary)
                    }
                }.padding(.top, 8)

                if store.state.recipes.isEmpty {
                    Text("Save a recipe first, then plan it onto a day.")
                        .font(.subheadline).foregroundColor(theme.inkMute)
                        .frame(maxWidth: .infinity).padding(.vertical, 40)
                        .overlay(RoundedRectangle(cornerRadius: 14).stroke(theme.line, lineWidth: 1.5))
                } else {
                    ForEach(Array(days.enumerated()), id: \.offset) { idx, date in
                        let label = idx == 0 ? "Today" : (idx == 1 ? "Tomorrow" : weekdayShort(date))
                        dayCard(label: label, date: date)
                    }
                }
            }
            .padding(.horizontal).padding(.bottom, 110)
        }
        .sheet(item: $picking) { ref in mealPicker(ref) }
    }

    func dayCard(label: String, date: String) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(label).fontWeight(.bold).foregroundColor(theme.ink).padding(.bottom, 8)
            ForEach(Array(mealSlots.enumerated()), id: \.offset) { i, s in
                Button(action: { picking = MealSlotRef(date: date, slot: s.key) }) {
                    HStack {
                        Text(s.label.uppercased())
                            .font(.system(size: 10, weight: .bold, design: .monospaced))
                            .foregroundColor(theme.inkMute).frame(width: 78, alignment: .leading)
                        if let name = entryFor(date, s.key).flatMap({ recipeName($0.recipeId) }) {
                            Text(name).fontWeight(.semibold).foregroundColor(theme.ink)
                        } else {
                            Text("— tap to plan").foregroundColor(theme.inkMute)
                        }
                        Spacer()
                    }
                    .padding(.vertical, 9).contentShape(Rectangle())
                }.buttonStyle(PlainButtonStyle())
                if i != mealSlots.count - 1 { Divider() }
            }
        }
        .padding(14).toolboxCard(theme)
    }

    func mealPicker(_ ref: MealSlotRef) -> some View {
        let current = entryFor(ref.date, ref.slot)
        return NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 8) {
                    if current != nil {
                        SwiftChunkyButton(text: "Clear this slot", palette: palette, variant: "ghost", size: "sm") {
                            store.clearMealSlot(date: ref.date, slot: ref.slot); picking = nil
                        }
                    }
                    ForEach(store.state.recipes.sorted { $0.name.lowercased() < $1.name.lowercased() }, id: \.id) { r in
                        Button(action: { store.setMealSlot(date: ref.date, slot: ref.slot, recipeId: r.id); picking = nil }) {
                            HStack {
                                Text(r.name).fontWeight(.semibold).foregroundColor(theme.ink)
                                Spacer()
                                if current?.recipeId == r.id { Image(systemName: "checkmark").foregroundColor(palette.primary) }
                            }
                            .padding().contentShape(Rectangle())
                        }
                        .buttonStyle(PlainButtonStyle())
                        .background(theme.surface).cornerRadius(12)
                        .overlay(RoundedRectangle(cornerRadius: 12).stroke(theme.line, lineWidth: 1))
                    }
                }
                .padding()
            }
            .background(theme.bgSubtle.ignoresSafeArea())
            .navigationTitle("Plan \(mealSlots.first { $0.key == ref.slot }?.label ?? ref.slot)")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar { ToolbarItem(placement: .confirmationAction) { Button("Done") { picking = nil } } }
        }
    }
}

// MARK: Settings

struct SwiftMeScreen: View {
    @ObservedObject var store: ToolboxObservableState
    @Binding var activeTab: String
    @Binding var accent: String
    @Binding var darkMode: Bool
    @Binding var showFlourishes: Bool
    @Binding var backgroundPattern: String
    let palette: SwiftBrandPalette
    let theme: SwiftTheme

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                // Back affordance (settings is not a tab)
                Button(action: { activeTab = "home" }) {
                    HStack(spacing: 6) {
                        Text("‹").font(.system(size: 20, weight: .bold))
                        Text("BACK").font(.system(size: 11, design: .monospaced)).fontWeight(.bold).tracking(0.8)
                    }
                    .foregroundColor(theme.inkMute)
                }
                .buttonStyle(PlainButtonStyle())
                .padding(.top, 8)

                VStack(alignment: .leading, spacing: 4) {
                    SwiftKicker(text: "Settings · Profile", color: palette.primary)
                    Text("Me").font(.system(.largeTitle, design: .serif)).foregroundColor(theme.ink)
                }

                // Brand
                VStack(alignment: .leading, spacing: 12) {
                    SwiftKicker(text: "Brand", color: theme.inkMute)
                    VStack(alignment: .leading, spacing: 14) {
                        VStack(alignment: .leading, spacing: 6) {
                            Text("Accent Palette").font(.system(size: 13, weight: .bold)).foregroundColor(theme.ink)
                            HStack(spacing: 6) {
                                ForEach(["indigo", "forest", "cyber", "sunset"], id: \.self) { key in
                                    SwiftChip(label: key, active: accent == key, palette: palette, theme: theme) { accent = key }
                                }
                            }
                        }
                        Toggle(isOn: $showFlourishes) {
                            Text("8-bit flourishes").font(.system(size: 13, weight: .bold)).foregroundColor(theme.ink)
                        }.tint(palette.primary)
                        Toggle(isOn: $darkMode) {
                            Text("Dark mode").font(.system(size: 13, weight: .bold)).foregroundColor(theme.ink)
                        }.tint(palette.primary)
                    }
                    .padding(14)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .toolboxCard(theme)
                }

                // Backdrop
                VStack(alignment: .leading, spacing: 12) {
                    SwiftKicker(text: "Backdrop", color: theme.inkMute)
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Pattern Style").font(.system(size: 13, weight: .bold)).foregroundColor(theme.ink)
                        HStack(spacing: 6) {
                            ForEach([("grid", "Grid"), ("plain", "Plain"), ("dots", "Dots")], id: \.0) { key, label in
                                SwiftChip(label: label, active: backgroundPattern == key, palette: palette, theme: theme) { backgroundPattern = key }
                            }
                        }
                    }
                    .padding(14)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .toolboxCard(theme)
                }

                SwiftChunkyButton(text: "Reset app data", palette: palette, variant: "outline", size: "sm", fullWidth: true) {
                    store.reset()
                }
                .padding(.top, 6)
            }
            .padding(.horizontal)
            .padding(.bottom, 32)
        }
    }
}
