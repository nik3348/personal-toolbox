import SwiftUI
import SharedLogic

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

// Theme tokens
struct SwiftTheme {
    static let ink = Color(hex: "#0f172a")
    static let inkSoft = Color(hex: "#334155")
    static let inkMute = Color(hex: "#64748b")
    static let bg = Color(hex: "#f8fafc")
    static let bgSubtle = Color(hex: "#f1f5f9")
    static let surface = Color.white
    static let line = Color(red: 15/255, green: 23/255, blue: 42/255).opacity(0.08)
    static let danger = Color(hex: "#ef4444")
    static let warn = Color(hex: "#f59e0b")
    static let ok = Color(hex: "#22c55e")
    static let pink = Color(hex: "#ff00ff")
    static let cyan = Color(hex: "#00bfff")
}

// State Observer matching Kotlin Repository State
class ToolboxObservableState: ObservableObject, ToolboxRepositoryListener {
    @Published var state: ToolboxState
    private let repo: ToolboxRepository

    init(repo: ToolboxRepository) {
        self.repo = repo
        self.state = repo.state
        repo.addListener(listener: self)
    }

    func onStateChanged(state: ToolboxState) {
        DispatchQueue.main.async {
            self.state = state
        }
    }

    func toggleDone(id: String) { repo.toggleDone(id: id) }
    func setOn(id: String, on: Bool) { repo.setOn(id: id, on: on) }
    func setMode(id: String, mode: String) { repo.setMode(id: id, mode: mode) }
    func deleteReminder(id: String) { repo.deleteReminder(id: id) }
    func updateReminder(id: String, title: String, time: String, repeat: String, mode: String) {
        repo.updateReminder(id: id, title: title, time: time, repeat: repeat, mode: mode)
    }
    func addReminder(title: String, time: String, repeat: String, mode: String) {
        repo.addReminder(title: title, time: time, repeat: repeat, mode: mode)
    }
    func consumeFridge(id: String) { repo.consumeFridge(id: id) }
    func updateFridge(id: String, name: String, qty: String, expiry: String, location: String) {
        repo.updateFridge(id: id, name: name, qty: qty, expiry: expiry, location: location)
    }
    func addFridge(name: String, qty: String, expiry: String, location: String) {
        repo.addFridge(name: name, qty: qty, expiry: expiry, location: location)
    }
    func nudgeFromFridge(name: String) { repo.nudgeFromFridge(itemName: name) }
    func reset() { repo.reset() }
    func setQuiet(on: Bool) { repo.setQuiet(on: on) }
}

struct ContentView: View {
    @StateObject var store = ToolboxObservableState(repo: ToolboxRepository(storage: KeyValueStorage()))
    @State private var activeTab = "home"
    @State private var accent = "indigo"
    @State private var showFlourishes = true
    @State private var backgroundPattern = "grid"

    var currentPalette: SwiftBrandPalette {
        SwiftBrandPalettes[accent] ?? SwiftIndigo
    }

    var body: some View {
        ZStack {
            // Background pattern
            BackgroundView(pattern: backgroundPattern)
                .edgesIgnoringSafeArea(.all)

            VStack(spacing: 0) {
                // Content area
                ZStack {
                    if activeTab == "home" {
                        SwiftHomeScreen(store: store, activeTab: $activeTab, palette: currentPalette, showFlourishes: showFlourishes)
                    } else if activeTab == "reminders" {
                        SwiftRemindersScreen(store: store, palette: currentPalette)
                    } else if activeTab == "fridge" {
                        SwiftFridgeScreen(store: store, palette: currentPalette)
                    } else if activeTab == "me" {
                        SwiftMeScreen(
                            store: store,
                            accent: $accent,
                            showFlourishes: $showFlourishes,
                            backgroundPattern: $backgroundPattern,
                            palette: currentPalette
                        )
                    }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)

                // Custom Tab Bar
                SwiftTabBar(activeTab: $activeTab, palette: currentPalette)
            }
        }
    }
}

// Background design drawer
struct BackgroundView: View {
    let pattern: String
    var body: some View {
        GeometryReader { geo in
            Path { path in
                if pattern == "grid" {
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
            }
            .stroke(SwiftTheme.line, lineWidth: 1)
            .background(SwiftTheme.bgSubtle)
        }
    }
}

// Signature buttons & elements
struct SwiftChunkyButton: View {
    let text: String
    let palette: SwiftBrandPalette
    var variant: String = "primary" // "primary", "outline", "ghost"
    var size: String = "md"
    var fullWidth: Bool = false
    var action: () -> Void

    @State private var isPressed = false

    var body: some View {
        Button(action: action) {
            Text(text.uppercased())
                .font(.system(.body, design: .monospaced))
                .fontWeight(.bold)
                .tracking(1.2)
                .padding(size == "sm" ? 8 : 12)
                .frame(maxWidth: fullWidth ? .infinity : nil)
                .background(variant == "primary" ? palette.primary : Color.white)
                .foregroundColor(variant == "primary" ? .white : SwiftTheme.ink)
                .cornerRadius(12)
                .overlay(
                    RoundedCornerShape(radius: 12)
                        .stroke(variant == "outline" ? SwiftTheme.ink : palette.primary, lineWidth: 1.5)
                )
                .shadow(color: palette.deep, radius: 0, x: isPressed ? 0 : 4, y: isPressed ? 0 : 4)
                .offset(x: isPressed ? 4 : 0, y: isPressed ? 4 : 0)
        }
        .buttonStyle(PlainButtonStyle())
    }
}

struct RoundedCornerShape: Shape {
    let radius: CGFloat
    func path(in rect: CGRect) -> Path {
        let path = UIBezierPath(roundedRect: rect, cornerRadius: radius)
        return Path(path.cgPath)
    }
}

struct SwiftTabBar: View {
    @Binding var activeTab: String
    let palette: SwiftBrandPalette

    var body: some View {
        HStack {
            tabButton(id: "home", label: "Home", icon: "house.fill")
            tabButton(id: "reminders", label: "Quiet", icon: "bell.slash.fill")
            tabButton(id: "fridge", label: "Fridge", icon: "thermometer.snowflake")
            tabButton(id: "me", label: "Me", icon: "person.fill")
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 8)
        .background(Color.white.opacity(0.85))
        .cornerRadius(999)
        .overlay(
            RoundedRectangle(cornerRadius: 999)
                .stroke(SwiftTheme.line, lineWidth: 1)
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
            .background(active ? palette.primary : Color.transparent)
            .foregroundColor(active ? .white : SwiftTheme.inkMute)
            .cornerRadius(999)
        }
    }
}

extension Color {
    static let transparent = Color.clear
}

// MARK: Screens

struct SwiftHomeScreen: View {
    @ObservedObject var store: ToolboxObservableState
    @Binding var activeTab: String
    let palette: SwiftBrandPalette
    let showFlourishes: Bool

    var todayReminders: [Reminder] {
        store.state.reminders.filter { $0.on && $0.dueToday }
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                // Header
                HStack {
                    Text("🕹")
                    Text(showFlourishes ? "toolbox" : "8bittoolbox")
                        .font(showFlourishes ? .system(.headline, design: .monospaced) : .headline)
                    Spacer()
                    Text("ONLINE")
                        .font(.system(.caption, design: .monospaced))
                        .foregroundColor(SwiftTheme.ok)
                }
                .padding(.top, 12)

                Text("Dashboard")
                    .font(.title)
                    .fontWeight(.bold)

                // Today's Nudges
                VStack(alignment: .leading) {
                    Text("TODAY'S QUIET NUDGES")
                        .font(.system(.caption, design: .monospaced))
                        .foregroundColor(palette.primary)
                    
                    ForEach(todayReminders.prefix(4), id: \.id) { r in
                        HStack {
                            Button(action: { store.toggleDone(id: r.id) }) {
                                Image(systemName: store.state.doneIds.contains(r.id) ? "checkmark.square.fill" : "square")
                                    .foregroundColor(palette.primary)
                            }
                            VStack(alignment: .leading) {
                                Text(r.title)
                                    .fontWeight(.semibold)
                                Text("\(r.time) · 🔕 \(r.mode)")
                                    .font(.caption)
                                    .foregroundColor(SwiftTheme.inkMute)
                            }
                        }
                        .padding(.vertical, 4)
                    }
                }
                .padding()
                .background(Color.white)
                .cornerRadius(16)
                .overlay(RoundedRectangle(cornerRadius: 16).stroke(SwiftTheme.line, lineWidth: 1))
            }
            .padding(.horizontal)
        }
    }
}

struct SwiftRemindersScreen: View {
    @ObservedObject var store: ToolboxObservableState
    let palette: SwiftBrandPalette

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text("Quiet Nudges")
                    .font(.title)
                    .fontWeight(.bold)

                ForEach(store.state.reminders, id: \.id) { r in
                    HStack {
                        VStack(alignment: .leading) {
                            Text(r.title)
                                .font(.headline)
                            Text("\(r.time) · \(r.mode)")
                                .font(.subheadline)
                                .foregroundColor(SwiftTheme.inkMute)
                        }
                        Spacer()
                        Toggle("", isOn: Binding(
                            get: { r.on },
                            set: { store.setOn(id: r.id, on: $0) }
                        ))
                        .labelsHidden()
                    }
                    .padding()
                    .background(Color.white)
                    .cornerRadius(14)
                }
            }
            .padding(.horizontal)
        }
    }
}

struct SwiftFridgeScreen: View {
    @ObservedObject var store: ToolboxObservableState
    let palette: SwiftBrandPalette

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text("Fridge")
                    .font(.title)
                    .fontWeight(.bold)

                ForEach(store.state.fridge, id: \.id) { item in
                    HStack {
                        VStack(alignment: .leading) {
                            Text(item.name)
                                .font(.headline)
                            Text("\(item.qty) · \(item.location)")
                                .font(.subheadline)
                                .foregroundColor(SwiftTheme.inkMute)
                        }
                        Spacer()
                        Text(item.expiry)
                            .font(.system(.body, design: .monospaced))
                    }
                    .padding()
                    .background(Color.white)
                    .cornerRadius(14)
                }
            }
            .padding(.horizontal)
        }
    }
}

struct SwiftMeScreen: View {
    @ObservedObject var store: ToolboxObservableState
    @Binding var accent: String
    @Binding var showFlourishes: Bool
    @Binding var backgroundPattern: String
    let palette: SwiftBrandPalette

    var body: some View {
        Form {
            Section(header: Text("BRAND")) {
                Picker("Accent Color", selection: $accent) {
                    Text("Indigo").tag("indigo")
                    Text("Forest").tag("forest")
                    Text("Cyber").tag("cyber")
                    Text("Sunset").tag("sunset")
                }
                Toggle("8-bit flourishes", isOn: $showFlourishes)
            }

            Section(header: Text("BACKDROP")) {
                Picker("Pattern Style", selection: $backgroundPattern) {
                    Text("Grid").tag("grid")
                    Text("Plain").tag("plain")
                    Text("Dots").tag("dots")
                }
            }

            Section {
                Button("Reset Demo Data", role: .destructive) {
                    store.reset()
                }
            }
        }
    }
}