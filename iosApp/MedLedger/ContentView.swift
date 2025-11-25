import SwiftUI

struct ContentView: View {
    @State private var selectedTab = 0
    
    var body: some View {
        TabView(selection: $selectedTab) {
            HomeView()
                .tabItem {
                    Label("首页", systemImage: "house")
                }
                .tag(0)
            
            ChronicView()
                .tabItem {
                    Label("慢病", systemImage: "heart")
                }
                .tag(1)
            
            ProfileView()
                .tabItem {
                    Label("我的", systemImage: "person")
                }
                .tag(2)
        }
        .tint(.green)
    }
}

#Preview {
    ContentView()
}
