import SwiftUI
import shared

struct HomeView: View {
    @StateObject private var viewModel = HomeViewModel()
    @State private var searchText = ""
    @State private var showingAddVisit = false
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // 统计卡片
                StatsCard(
                    monthlyCount: viewModel.monthlyVisitCount,
                    yearlyCount: viewModel.yearlyVisitCount
                )
                .padding()
                
                // 就诊记录列表
                if viewModel.visits.isEmpty {
                    EmptyStateView(
                        icon: "doc.text",
                        title: "暂无就诊记录",
                        message: "点击右上角添加您的第一条就诊记录"
                    )
                } else {
                    List {
                        ForEach(viewModel.filteredVisits(searchText), id: \.localId) { visit in
                            NavigationLink(destination: VisitDetailView(visit: visit)) {
                                VisitRow(visit: visit)
                            }
                        }
                        .onDelete(perform: viewModel.deleteVisits)
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("病历本")
            .searchable(text: $searchText, prompt: "搜索医院、科室、医生")
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button(action: { showingAddVisit = true }) {
                        Image(systemName: "plus")
                    }
                }
                
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: { /* TODO: Calendar view */ }) {
                        Image(systemName: "calendar")
                    }
                }
            }
            .sheet(isPresented: $showingAddVisit) {
                VisitEditView(visit: nil)
            }
        }
    }
}

struct StatsCard: View {
    let monthlyCount: Int
    let yearlyCount: Int
    
    var body: some View {
        HStack(spacing: 16) {
            StatItem(title: "本月就诊", value: "\(monthlyCount)", icon: "calendar", color: .blue)
            StatItem(title: "本年就诊", value: "\(yearlyCount)", icon: "chart.bar", color: .green)
        }
    }
}

struct StatItem: View {
    let title: String
    let value: String
    let icon: String
    let color: Color
    
    var body: some View {
        HStack {
            Image(systemName: icon)
                .foregroundColor(color)
                .font(.title2)
            
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.caption)
                    .foregroundColor(.secondary)
                Text(value)
                    .font(.title2)
                    .fontWeight(.semibold)
            }
            
            Spacer()
        }
        .padding()
        .background(color.opacity(0.1))
        .cornerRadius(12)
    }
}

struct VisitRow: View {
    let visit: Visit
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(visit.hospital)
                    .font(.headline)
                Spacer()
                Text(formatDate(visit.date))
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            HStack(spacing: 12) {
                if let department = visit.department {
                    Label(department, systemImage: "building.2")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                
                if let doctor = visit.doctor {
                    Label(doctor, systemImage: "person")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
            }
            
            if let cost = visit.cost {
                Text("¥\(String(format: "%.2f", cost))")
                    .font(.subheadline)
                    .foregroundColor(.orange)
            }
        }
        .padding(.vertical, 4)
    }
    
    private func formatDate(_ timestamp: Int64) -> String {
        let date = Date(timeIntervalSince1970: Double(timestamp) / 1000)
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter.string(from: date)
    }
}

struct EmptyStateView: View {
    let icon: String
    let title: String
    let message: String
    
    var body: some View {
        VStack(spacing: 16) {
            Spacer()
            Image(systemName: icon)
                .font(.system(size: 60))
                .foregroundColor(.secondary.opacity(0.5))
            Text(title)
                .font(.headline)
                .foregroundColor(.secondary)
            Text(message)
                .font(.subheadline)
                .foregroundColor(.secondary.opacity(0.8))
                .multilineTextAlignment(.center)
            Spacer()
        }
        .padding()
    }
}

#Preview {
    HomeView()
}
