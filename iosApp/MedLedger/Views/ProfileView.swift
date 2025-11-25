import SwiftUI
import shared

struct ProfileView: View {
    @StateObject private var viewModel = ProfileViewModel()
    @State private var showingLogin = false
    @State private var showingLogoutAlert = false
    
    var body: some View {
        NavigationStack {
            List {
                // 用户信息
                Section {
                    if viewModel.isLoggedIn {
                        LoggedInHeader(user: viewModel.user)
                    } else {
                        NotLoggedInHeader(onLogin: { showingLogin = true })
                    }
                }
                
                // 同步状态
                if viewModel.isLoggedIn {
                    Section {
                        HStack {
                            Image(systemName: "cloud.fill")
                                .foregroundColor(.green)
                            VStack(alignment: .leading) {
                                Text("数据已同步")
                                    .font(.subheadline)
                                Text("最后同步: 刚刚")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                            Spacer()
                            Button("立即同步") {
                                viewModel.sync()
                            }
                            .font(.subheadline)
                        }
                    }
                }
                
                // 数据管理
                Section("数据管理") {
                    NavigationLink(destination: FamilyMembersView()) {
                        Label("家庭成员", systemImage: "person.3")
                    }
                    
                    NavigationLink(destination: BackupView()) {
                        Label("数据备份", systemImage: "externaldrive")
                    }
                    
                    NavigationLink(destination: Text("云同步设置")) {
                        Label("云同步设置", systemImage: "cloud")
                    }
                    .disabled(!viewModel.isLoggedIn)
                }
                
                // 更多
                Section("更多") {
                    NavigationLink(destination: SettingsView()) {
                        Label("设置", systemImage: "gearshape")
                    }
                    
                    NavigationLink(destination: Text("帮助与反馈")) {
                        Label("帮助与反馈", systemImage: "questionmark.circle")
                    }
                    
                    NavigationLink(destination: AboutView()) {
                        Label("关于", systemImage: "info.circle")
                    }
                }
                
                // 退出登录
                if viewModel.isLoggedIn {
                    Section {
                        Button(role: .destructive) {
                            showingLogoutAlert = true
                        } label: {
                            HStack {
                                Spacer()
                                Text("退出登录")
                                Spacer()
                            }
                        }
                    }
                }
            }
            .navigationTitle("我的")
            .sheet(isPresented: $showingLogin) {
                LoginView()
            }
            .alert("确认退出登录？", isPresented: $showingLogoutAlert) {
                Button("取消", role: .cancel) {}
                Button("退出", role: .destructive) {
                    viewModel.logout()
                }
            } message: {
                Text("退出后，云同步将暂停，本地数据不会丢失。")
            }
        }
    }
}

struct LoggedInHeader: View {
    let user: User?
    
    var body: some View {
        HStack(spacing: 16) {
            Image(systemName: "person.circle.fill")
                .font(.system(size: 50))
                .foregroundColor(.green)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(user?.nickname ?? "用户")
                    .font(.title2)
                    .fontWeight(.semibold)
                
                if let phone = user?.phone {
                    Text(maskPhone(phone))
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
            }
            
            Spacer()
        }
        .padding(.vertical, 8)
    }
    
    private func maskPhone(_ phone: String) -> String {
        guard phone.count >= 7 else { return phone }
        let start = phone.prefix(3)
        let end = phone.suffix(4)
        return "\(start)****\(end)"
    }
}

struct NotLoggedInHeader: View {
    let onLogin: () -> Void
    
    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "person.circle")
                .font(.system(size: 60))
                .foregroundColor(.secondary)
            
            Text("登录后同步数据到云端")
                .font(.subheadline)
                .foregroundColor(.secondary)
            
            Button(action: onLogin) {
                Label("立即登录", systemImage: "arrow.right.circle.fill")
            }
            .buttonStyle(.borderedProminent)
            .tint(.green)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 20)
    }
}

struct FamilyMembersView: View {
    var body: some View {
        List {
            Section {
                HStack {
                    Image(systemName: "info.circle")
                        .foregroundColor(.blue)
                    Text("添加家庭成员后，可以分别管理每个人的健康档案")
                        .font(.subheadline)
                }
                .padding(.vertical, 4)
            }
            
            Section {
                FamilyMemberRow(name: "本人", relationship: "本人", isDefault: true)
            }
            
            Section {
                Button {
                    // TODO: Add family member
                } label: {
                    Label("添加家庭成员", systemImage: "plus.circle")
                }
            }
        }
        .navigationTitle("家庭成员")
    }
}

struct FamilyMemberRow: View {
    let name: String
    let relationship: String
    let isDefault: Bool
    
    var body: some View {
        HStack {
            Image(systemName: "person.fill")
                .font(.title2)
                .foregroundColor(.green)
                .frame(width: 40, height: 40)
                .background(Color.green.opacity(0.1))
                .cornerRadius(20)
            
            VStack(alignment: .leading) {
                HStack {
                    Text(name)
                        .font(.headline)
                    if isDefault {
                        Text("默认")
                            .font(.caption)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(Color.green)
                            .foregroundColor(.white)
                            .cornerRadius(4)
                    }
                }
                Text(relationship)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
            
            Spacer()
            
            Image(systemName: "chevron.right")
                .foregroundColor(.secondary)
        }
    }
}

struct BackupView: View {
    var body: some View {
        List {
            Section("导出数据") {
                Button {
                    // TODO: Export
                } label: {
                    Label("导出为 ZIP", systemImage: "square.and.arrow.up")
                }
            }
            
            Section("导入数据") {
                Button {
                    // TODO: Import
                } label: {
                    Label("从 ZIP 导入", systemImage: "square.and.arrow.down")
                }
            }
        }
        .navigationTitle("数据备份")
    }
}

struct SettingsView: View {
    @AppStorage("enableNotifications") private var enableNotifications = true
    
    var body: some View {
        List {
            Section("通知") {
                Toggle("启用提醒通知", isOn: $enableNotifications)
            }
            
            Section("存储") {
                HStack {
                    Text("缓存大小")
                    Spacer()
                    Text("12.5 MB")
                        .foregroundColor(.secondary)
                }
                
                Button("清除缓存") {
                    // TODO: Clear cache
                }
            }
        }
        .navigationTitle("设置")
    }
}

struct AboutView: View {
    var body: some View {
        List {
            Section {
                VStack(spacing: 12) {
                    Image(systemName: "cross.case.fill")
                        .font(.system(size: 60))
                        .foregroundColor(.green)
                    
                    Text("病历本")
                        .font(.title)
                        .fontWeight(.bold)
                    
                    Text("版本 0.1.0")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 20)
            }
            
            Section {
                Link(destination: URL(string: "https://medledger.lessup.com/privacy")!) {
                    Label("隐私政策", systemImage: "hand.raised")
                }
                
                Link(destination: URL(string: "https://medledger.lessup.com/terms")!) {
                    Label("用户协议", systemImage: "doc.text")
                }
            }
        }
        .navigationTitle("关于")
    }
}

#Preview {
    ProfileView()
}
