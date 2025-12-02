package com.lessup.medledger.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lessup.medledger.model.User
import com.lessup.medledger.network.ApiClient
import com.lessup.medledger.network.TokenProvider
import com.lessup.medledger.sync.SyncEngine
import com.lessup.medledger.sync.SyncState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import javax.inject.Inject

/**
 * 认证状态
 */
sealed class AuthState {
    object Unknown : AuthState()
    object NotLoggedIn : AuthState()
    data class LoggedIn(val user: User) : AuthState()
}

/**
 * 登录UI状态
 */
data class LoginUiState(
    val phone: String = "",
    val code: String = "",
    val isLoading: Boolean = false,
    val isSendingCode: Boolean = false,
    val codeSent: Boolean = false,
    val countdown: Int = 0,
    val error: String? = null
)

/**
 * 认证 ViewModel
 */
@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel(), KoinComponent {
    
    private val apiClient: ApiClient by inject()
    private val tokenProvider: TokenProvider by inject()
    private val syncEngine: SyncEngine by inject()

    val syncState: StateFlow<SyncState> = syncEngine.syncState
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unknown)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()
    
    init {
        checkAuthState()
    }
    
    /**
     * 检查认证状态
     */
    private fun checkAuthState() {
        viewModelScope.launch {
            val token = tokenProvider.getAccessToken()
            if (token != null) {
                try {
                    val userResponse = apiClient.getCurrentUser()
                    _authState.value = AuthState.LoggedIn(
                        User(
                            id = userResponse.id,
                            phone = userResponse.phone,
                            email = userResponse.email,
                            nickname = userResponse.nickname,
                            avatar = userResponse.avatar,
                            createdAt = userResponse.createdAt
                        )
                    )
                } catch (e: Exception) {
                    _authState.value = AuthState.NotLoggedIn
                }
            } else {
                _authState.value = AuthState.NotLoggedIn
            }
        }
    }
    
    /**
     * 更新手机号
     */
    fun updatePhone(phone: String) {
        _loginUiState.value = _loginUiState.value.copy(
            phone = phone.take(11),
            error = null
        )
    }
    
    /**
     * 更新验证码
     */
    fun updateCode(code: String) {
        _loginUiState.value = _loginUiState.value.copy(
            code = code.take(6),
            error = null
        )
    }
    
    /**
     * 发送验证码
     */
    fun sendVerificationCode() {
        val phone = _loginUiState.value.phone
        if (phone.length != 11) {
            _loginUiState.value = _loginUiState.value.copy(error = "请输入正确的手机号")
            return
        }
        
        viewModelScope.launch {
            _loginUiState.value = _loginUiState.value.copy(isSendingCode = true, error = null)
            try {
                apiClient.sendVerificationCode(phone)
                _loginUiState.value = _loginUiState.value.copy(
                    isSendingCode = false,
                    codeSent = true,
                    countdown = 60
                )
                // 开始倒计时
                startCountdown()
            } catch (e: Exception) {
                _loginUiState.value = _loginUiState.value.copy(
                    isSendingCode = false,
                    error = e.message ?: "发送失败，请重试"
                )
            }
        }
    }
    
    private fun startCountdown() {
        viewModelScope.launch {
            while (_loginUiState.value.countdown > 0) {
                kotlinx.coroutines.delay(1000)
                _loginUiState.value = _loginUiState.value.copy(
                    countdown = _loginUiState.value.countdown - 1
                )
            }
        }
    }
    
    /**
     * 手机号登录
     */
    fun loginWithPhone() {
        val state = _loginUiState.value
        if (state.phone.length != 11) {
            _loginUiState.value = state.copy(error = "请输入正确的手机号")
            return
        }
        if (state.code.length != 6) {
            _loginUiState.value = state.copy(error = "请输入6位验证码")
            return
        }
        
        viewModelScope.launch {
            _loginUiState.value = state.copy(isLoading = true, error = null)
            try {
                val response = apiClient.loginWithPhone(state.phone, state.code)
                tokenProvider.saveTokens(response.accessToken, response.refreshToken)
                
                _authState.value = AuthState.LoggedIn(
                    User(
                        id = response.user.id,
                        phone = response.user.phone,
                        email = response.user.email,
                        nickname = response.user.nickname,
                        avatar = response.user.avatar,
                        createdAt = response.user.createdAt
                    )
                )
                
                // 登录成功后触发同步
                syncEngine.sync()
                
                _loginUiState.value = LoginUiState() // 重置状态
            } catch (e: Exception) {
                _loginUiState.value = state.copy(
                    isLoading = false,
                    error = e.message ?: "登录失败，请重试"
                )
            }
        }
    }
    
    /**
     * 微信登录
     */
    fun loginWithWeChat(code: String) {
        viewModelScope.launch {
            _loginUiState.value = _loginUiState.value.copy(isLoading = true, error = null)
            try {
                val response = apiClient.loginWithWeChat(code)
                tokenProvider.saveTokens(response.accessToken, response.refreshToken)
                
                _authState.value = AuthState.LoggedIn(
                    User(
                        id = response.user.id,
                        phone = response.user.phone,
                        email = response.user.email,
                        nickname = response.user.nickname,
                        avatar = response.user.avatar,
                        createdAt = response.user.createdAt
                    )
                )
                
                syncEngine.sync()
                _loginUiState.value = LoginUiState()
            } catch (e: Exception) {
                _loginUiState.value = _loginUiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "微信登录失败"
                )
            }
        }
    }

    /**
     * 立即同步
     */
    fun syncNow() {
        viewModelScope.launch {
            syncEngine.sync()
        }
    }
    
    /**
     * 登出
     */
    fun logout() {
        viewModelScope.launch {
            try {
                apiClient.logout()
            } catch (e: Exception) {
                // 忽略登出错误
            }
            tokenProvider.clearTokens()
            _authState.value = AuthState.NotLoggedIn
        }
    }
    
    /**
     * 跳过登录（本地模式）
     */
    fun skipLogin() {
        _authState.value = AuthState.NotLoggedIn
    }
}
