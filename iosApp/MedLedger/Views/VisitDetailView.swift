import SwiftUI
import shared

struct VisitDetailView: View {
    let visit: Visit
    @State private var showingEdit = false
    
    var body: some View {
        List {
            // 基本信息
            Section("就诊信息") {
                InfoRow(icon: "building.2", title: "医院", value: visit.hospital)
                
                if let department = visit.department {
                    InfoRow(icon: "stethoscope", title: "科室", value: department)
                }
                
                if let doctor = visit.doctor {
                    InfoRow(icon: "person", title: "医生", value: doctor)
                }
                
                InfoRow(icon: "calendar", title: "日期", value: formatDate(visit.date))
                
                if let cost = visit.cost {
                    InfoRow(icon: "yensign.circle", title: "费用", value: "¥\(String(format: "%.2f", cost))")
                }
            }
            
            // 检查项目
            if let items = visit.items, !items.isEmpty {
                Section("检查项目") {
                    Text(items)
                        .font(.body)
                }
            }
            
            // 备注
            if let note = visit.note, !note.isEmpty {
                Section("备注") {
                    Text(note)
                        .font(.body)
                }
            }
            
            // 相关文档
            Section("相关文档") {
                Button {
                    // TODO: Scan document
                } label: {
                    Label("添加文档", systemImage: "doc.badge.plus")
                }
            }
        }
        .navigationTitle("就诊详情")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button("编辑") {
                    showingEdit = true
                }
            }
        }
        .sheet(isPresented: $showingEdit) {
            VisitEditView(visit: visit)
        }
    }
    
    private func formatDate(_ timestamp: Int64) -> String {
        let date = Date(timeIntervalSince1970: Double(timestamp) / 1000)
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy年M月d日"
        return formatter.string(from: date)
    }
}

struct InfoRow: View {
    let icon: String
    let title: String
    let value: String
    
    var body: some View {
        HStack {
            Label(title, systemImage: icon)
                .foregroundColor(.secondary)
            Spacer()
            Text(value)
        }
    }
}

struct VisitEditView: View {
    @Environment(\.dismiss) private var dismiss
    let visit: Visit?
    
    @State private var hospital = ""
    @State private var department = ""
    @State private var doctor = ""
    @State private var date = Date()
    @State private var cost = ""
    @State private var items = ""
    @State private var note = ""
    @State private var isSaving = false
    
    var isNewVisit: Bool { visit == nil }
    
    var body: some View {
        NavigationStack {
            Form {
                Section("基本信息") {
                    TextField("医院名称 *", text: $hospital)
                    TextField("科室", text: $department)
                    TextField("医生", text: $doctor)
                    DatePicker("就诊日期", selection: $date, displayedComponents: .date)
                }
                
                Section("费用") {
                    TextField("费用（元）", text: $cost)
                        .keyboardType(.decimalPad)
                }
                
                Section("检查项目") {
                    TextField("检查项目（逗号分隔）", text: $items, axis: .vertical)
                        .lineLimit(2...4)
                }
                
                Section("备注") {
                    TextField("备注", text: $note, axis: .vertical)
                        .lineLimit(3...6)
                }
            }
            .navigationTitle(isNewVisit ? "新增就诊" : "编辑就诊")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("取消") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("保存") {
                        save()
                    }
                    .disabled(hospital.isEmpty || isSaving)
                }
            }
            .onAppear {
                if let visit = visit {
                    hospital = visit.hospital
                    department = visit.department ?? ""
                    doctor = visit.doctor ?? ""
                    date = Date(timeIntervalSince1970: Double(visit.date) / 1000)
                    cost = visit.cost.map { String(format: "%.2f", $0) } ?? ""
                    items = visit.items ?? ""
                    note = visit.note ?? ""
                }
            }
        }
    }
    
    private func save() {
        isSaving = true
        // TODO: Save to repository
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            isSaving = false
            dismiss()
        }
    }
}
