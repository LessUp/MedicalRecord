import SwiftUI

struct LoginView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var phone = ""
    @State private var code = ""
    @State private var isLoading = false
    @State private var countdown = 0
    @State private var errorMessage: String?
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 24) {
                // Logo
                VStack(spacing: 12) {
                    Image(systemName: "cross.case.fill")
                        .font(.system(size: 60))
                        .foregroundColor(.green)
                    
                    Text("病历本")
                        .font(.largeTitle)
                        .fontWeight(.bold)
                        .foregroundColor(.green)
                    
                    Text("您的私人健康档案管家")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                .padding(.top, 40)
                
                Spacer()
                
                // 登录表单
                VStack(spacing: 16) {
                    // 手机号
                    HStack {
                        Image(systemName: "phone")
                            .foregroundColor(.secondary)
                        TextField("手机号", text: $phone)
                            .keyboardType(.phonePad)
                    }
                    .padding()
                    .background(Color(.systemGray6))
                    .cornerRadius(12)
                    
                    // 验证码
                    HStack {
                        Image(systemName: "number")
                            .foregroundColor(.secondary)
                        TextField("验证码", text: $code)
                            .keyboardType(.numberPad)
                        
                        Button(action: sendCode) {
                            if countdown > 0 {
                                Text("\(countdown)s")
                                    .foregroundColor(.secondary)
                            } else {
                                Text("获取验证码")
                            }
                        }
                        .disabled(phone.count != 11 || countdown > 0)
                    }
                    .padding()
                    .background(Color(.systemGray6))
                    .cornerRadius(12)
                    
                    // 错误提示
                    if let error = errorMessage {
                        Text(error)
                            .font(.caption)
                            .foregroundColor(.red)
                    }
                    
                    // 登录按钮
                    Button(action: login) {
                        if isLoading {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        } else {
                            Text("登录")
                                .fontWeight(.semibold)
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 50)
                    .background(phone.count == 11 && code.count == 6 ? Color.green : Color.gray)
                    .foregroundColor(.white)
                    .cornerRadius(12)
                    .disabled(phone.count != 11 || code.count != 6 || isLoading)
                }
                .padding(.horizontal)
                
                // 分隔线
                HStack {
                    Rectangle()
                        .fill(Color.secondary.opacity(0.3))
                        .frame(height: 1)
                    Text("其他登录方式")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    Rectangle()
                        .fill(Color.secondary.opacity(0.3))
                        .frame(height: 1)
                }
                .padding(.horizontal)
                
                // 微信登录
                Button(action: loginWithWeChat) {
                    HStack {
                        Image(systemName: "message.fill")
                        Text("微信登录")
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 50)
                    .background(Color(red: 0.03, green: 0.76, blue: 0.38).opacity(0.1))
                    .foregroundColor(Color(red: 0.03, green: 0.76, blue: 0.38))
                    .cornerRadius(12)
                }
                .padding(.horizontal)
                
                Spacer()
                
                // 跳过登录
                Button("暂不登录，本地使用") {
                    dismiss()
                }
                .font(.subheadline)
                .foregroundColor(.secondary)
                
                // 协议
                Text("登录即表示同意《用户协议》和《隐私政策》")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .padding(.bottom)
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("关闭") { dismiss() }
                }
            }
        }
    }
    
    private func sendCode() {
        // TODO: Send verification code
        countdown = 60
        startCountdown()
    }
    
    private func startCountdown() {
        Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { timer in
            if countdown > 0 {
                countdown -= 1
            } else {
                timer.invalidate()
            }
        }
    }
    
    private func login() {
        isLoading = true
        errorMessage = nil
        // TODO: Call login API
        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
            isLoading = false
            dismiss()
        }
    }
    
    private func loginWithWeChat() {
        // TODO: WeChat login
    }
}

#Preview {
    LoginView()
}
