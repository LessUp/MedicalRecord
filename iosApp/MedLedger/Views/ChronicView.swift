import SwiftUI
import shared

struct ChronicView: View {
    @StateObject private var viewModel = ChronicViewModel()
    @State private var showingAddCondition = false
    
    var body: some View {
        NavigationStack {
            Group {
                if viewModel.conditions.isEmpty {
                    EmptyStateView(
                        icon: "heart.text.square",
                        title: "暂无慢病档案",
                        message: "添加慢病记录，设置复查提醒"
                    )
                } else {
                    List {
                        ForEach(viewModel.conditions, id: \.condition.localId) { overview in
                            ConditionCard(overview: overview)
                        }
                        .onDelete(perform: viewModel.deleteConditions)
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("慢病管理")
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button(action: { showingAddCondition = true }) {
                        Image(systemName: "plus")
                    }
                }
            }
            .sheet(isPresented: $showingAddCondition) {
                AddConditionView()
            }
        }
    }
}

struct ConditionCard: View {
    let overview: ConditionOverview
    @State private var isExpanded = false
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // 标题行
            HStack {
                Image(systemName: "heart.fill")
                    .foregroundColor(.red)
                
                Text(overview.condition.name)
                    .font(.headline)
                
                Spacer()
                
                Button(action: { withAnimation { isExpanded.toggle() } }) {
                    Image(systemName: isExpanded ? "chevron.up" : "chevron.down")
                        .foregroundColor(.secondary)
                }
            }
            
            // 复查计划
            if !overview.plans.isEmpty {
                let nextPlan = overview.plans.first
                if let nextDate = nextPlan?.nextCheckDate {
                    HStack {
                        Image(systemName: "calendar.badge.clock")
                            .foregroundColor(.orange)
                        Text("下次复查: \(formatDate(nextDate))")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                }
            }
            
            // 展开的详情
            if isExpanded {
                Divider()
                
                if let note = overview.condition.note, !note.isEmpty {
                    Text(note)
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                
                ForEach(overview.plans, id: \.plan.localId) { planOverview in
                    PlanRow(planOverview: planOverview)
                }
            }
        }
        .padding()
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.05), radius: 5, x: 0, y: 2)
    }
    
    private func formatDate(_ timestamp: Int64) -> String {
        let date = Date(timeIntervalSince1970: Double(timestamp) / 1000)
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy年M月d日"
        return formatter.string(from: date)
    }
}

struct PlanRow: View {
    let planOverview: PlanOverview
    
    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                if let items = planOverview.plan.items {
                    Text(items)
                        .font(.subheadline)
                }
                Text("每\(planOverview.plan.intervalMonths)个月")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            Spacer()
            
            if let remind = planOverview.plan.remindDaysBefore {
                Text("提前\(remind)天提醒")
                    .font(.caption)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color.blue.opacity(0.1))
                    .cornerRadius(4)
            }
        }
        .padding(.vertical, 4)
    }
}

struct AddConditionView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var name = ""
    @State private var note = ""
    @State private var intervalMonths = 3
    @State private var remindDays = 7
    
    var body: some View {
        NavigationStack {
            Form {
                Section("慢病信息") {
                    TextField("疾病名称", text: $name)
                    TextField("备注", text: $note, axis: .vertical)
                        .lineLimit(3...6)
                }
                
                Section("复查计划") {
                    Picker("复查周期", selection: $intervalMonths) {
                        Text("1个月").tag(1)
                        Text("3个月").tag(3)
                        Text("6个月").tag(6)
                        Text("12个月").tag(12)
                    }
                    
                    Picker("提前提醒", selection: $remindDays) {
                        Text("提前1天").tag(1)
                        Text("提前3天").tag(3)
                        Text("提前7天").tag(7)
                        Text("提前14天").tag(14)
                    }
                }
            }
            .navigationTitle("添加慢病")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("取消") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("保存") {
                        // TODO: Save condition
                        dismiss()
                    }
                    .disabled(name.isEmpty)
                }
            }
        }
    }
}

#Preview {
    ChronicView()
}
