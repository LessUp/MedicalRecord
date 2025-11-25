import SwiftUI
import shared

@main
struct MedLedgerApp: App {
    init() {
        // 初始化 Koin
        KoinHelperKt.doInitKoin()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
