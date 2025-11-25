import Foundation
import shared
import Combine

class ChronicViewModel: ObservableObject {
    @Published var conditions: [ConditionOverview] = []
    @Published var isLoading = false
    
    init() {
        loadConditions()
    }
    
    func loadConditions() {
        // TODO: Load from shared repository
        conditions = []
    }
    
    func deleteConditions(at offsets: IndexSet) {
        // TODO: Delete from repository
        conditions.remove(atOffsets: offsets)
    }
}
