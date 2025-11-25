import Foundation
import shared
import Combine

class ProfileViewModel: ObservableObject {
    @Published var isLoggedIn = false
    @Published var user: User? = nil
    @Published var isSyncing = false
    
    init() {
        checkAuthState()
    }
    
    func checkAuthState() {
        // TODO: Check auth state from shared module
        isLoggedIn = false
        user = nil
    }
    
    func sync() {
        guard isLoggedIn else { return }
        isSyncing = true
        // TODO: Trigger sync
        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
            self.isSyncing = false
        }
    }
    
    func logout() {
        // TODO: Call logout from shared module
        isLoggedIn = false
        user = nil
    }
}
