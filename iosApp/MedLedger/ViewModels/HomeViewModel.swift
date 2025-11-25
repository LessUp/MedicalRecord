import Foundation
import shared
import Combine

class HomeViewModel: ObservableObject {
    @Published var visits: [Visit] = []
    @Published var monthlyVisitCount: Int = 0
    @Published var yearlyVisitCount: Int = 0
    @Published var isLoading = false
    
    private var cancellables = Set<AnyCancellable>()
    
    init() {
        loadVisits()
    }
    
    func loadVisits() {
        // TODO: Load from shared repository
        // For now, use mock data
        visits = []
        monthlyVisitCount = 0
        yearlyVisitCount = 0
    }
    
    func filteredVisits(_ searchText: String) -> [Visit] {
        guard !searchText.isEmpty else { return visits }
        return visits.filter { visit in
            visit.hospital.localizedCaseInsensitiveContains(searchText) ||
            (visit.department?.localizedCaseInsensitiveContains(searchText) ?? false) ||
            (visit.doctor?.localizedCaseInsensitiveContains(searchText) ?? false)
        }
    }
    
    func deleteVisits(at offsets: IndexSet) {
        // TODO: Delete from repository
        visits.remove(atOffsets: offsets)
    }
    
    func refresh() async {
        // TODO: Refresh data
    }
}
