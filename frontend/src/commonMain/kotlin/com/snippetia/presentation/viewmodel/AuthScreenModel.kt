package com.snippetia.presentation.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.snippetia.domain.usecase.LoginUseCase
import com.snippetia.domain.usecase.RegisterUseCase
import com.snippetia.domain.usecase.ForgotPasswordUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val rememberMe: Boolean = false,
    val acceptTerms: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val usernameError: String? = null,
    val isSignUpMode: Boolean = false,
    val showBiometricPrompt: Boolean = false,
    val isBiometricEnabled: Boolean = false
)

class AuthScreenModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase
) : ScreenModel {
    
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()
    
    fun updateEmail(email: String) {
        _state.value = _state.value.copy(
            email = email,
            emailError = null,
            error = null
        )
    }
    
    fun updatePassword(password: String) {
        _state.value = _state.value.copy(
            password = password,
            passwordError = null,
            error = null
        )
    }
    
    fun updateConfirmPassword(confirmPassword: String) {
        _state.value = _state.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = null,
            error = null
        )
    }
    
    fun updateFirstName(firstName: String) {
        _state.value = _state.value.copy(firstName = firstName, error = null)
    }
    
    fun updateLastName(lastName: String) {
        _state.value = _state.value.copy(lastName = lastName, error = null)
    }
    
    fun updateUsername(username: String) {
        _state.value = _state.value.copy(
            username = username,
            usernameError = null,
            error = null
        )
    }
    
    fun toggleRememberMe() {
        _state.value = _state.value.copy(rememberMe = !_state.value.rememberMe)
    }
    
    fun toggleAcceptTerms() {
        _state.value = _state.value.copy(acceptTerms = !_state.value.acceptTerms)
    }
    
    fun toggleSignUpMode() {
        _state.value = _state.value.copy(
            isSignUpMode = !_state.value.isSignUpMode,
            error = null,
            emailError = null,
            passwordError = null,
            confirmPasswordError = null,
            usernameError = null
        )
    }
    
    fun login() {
        if (!validateLoginForm()) return
        
        _state.value = _state.value.copy(isLoading = true, error = null)
        
        screenModelScope.launch {
            loginUseCase(
                email = _state.value.email,
                password = _state.value.password,
                rememberMe = _state.value.rememberMe
            ).collect { result ->
                result.onSuccess { user ->
                    _state.value = _state.value.copy(isLoading = false)
                    // Navigate to home screen
                }.onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Login failed"
                    )
                }
            }
        }
    }
    
    fun register() {
        if (!validateSignUpForm()) return
        
        _state.value = _state.value.copy(isLoading = true, error = null)
        
        screenModelScope.launch {
            registerUseCase(
                email = _state.value.email,
                password = _state.value.password,
                firstName = _state.value.firstName,
                lastName = _state.value.lastName,
                username = _state.value.username
            ).collect { result ->
                result.onSuccess { user ->
                    _state.value = _state.value.copy(isLoading = false)
                    // Navigate to verification screen or home
                }.onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Registration failed"
                    )
                }
            }
        }
    }
    
    fun forgotPassword() {
        if (_state.value.email.isBlank()) {
            _state.value = _state.value.copy(emailError = "Please enter your email first")
            return
        }
        
        screenModelScope.launch {
            forgotPasswordUseCase(_state.value.email).collect { result ->
                result.onSuccess {
                    _state.value = _state.value.copy(
                        error = "Password reset link sent to your email"
                    )
                }.onFailure { error ->
                    _state.value = _state.value.copy(
                        error = error.message ?: "Failed to send reset link"
                    )
                }
            }
        }
    }
    
    fun loginWithGitHub() {
        // Implement OAuth login with GitHub
        screenModelScope.launch {
            // OAuth flow implementation
        }
    }
    
    fun loginWithGoogle() {
        // Implement OAuth login with Google
        screenModelScope.launch {
            // OAuth flow implementation
        }
    }
    
    fun enableBiometric() {
        _state.value = _state.value.copy(showBiometricPrompt = true)
    }
    
    fun loginWithBiometric() {
        // Implement biometric authentication
        screenModelScope.launch {
            // Biometric authentication flow
        }
    }
    
    fun navigateToSignUp() {
        _state.value = _state.value.copy(isSignUpMode = true)
    }
    
    fun navigateToSignIn() {
        _state.value = _state.value.copy(isSignUpMode = false)
    }
    
    private fun validateLoginForm(): Boolean {
        var isValid = true
        
        if (_state.value.email.isBlank()) {
            _state.value = _state.value.copy(emailError = "Email is required")
            isValid = false
        } else if (!isValidEmail(_state.value.email)) {
            _state.value = _state.value.copy(emailError = "Invalid email format")
            isValid = false
        }
        
        if (_state.value.password.isBlank()) {
            _state.value = _state.value.copy(passwordError = "Password is required")
            isValid = false
        }
        
        return isValid
    }
    
    private fun validateSignUpForm(): Boolean {
        var isValid = true
        
        if (_state.value.email.isBlank()) {
            _state.value = _state.value.copy(emailError = "Email is required")
            isValid = false
        } else if (!isValidEmail(_state.value.email)) {
            _state.value = _state.value.copy(emailError = "Invalid email format")
            isValid = false
        }
        
        if (_state.value.username.isBlank()) {
            _state.value = _state.value.copy(usernameError = "Username is required")
            isValid = false
        } else if (_state.value.username.length < 3) {
            _state.value = _state.value.copy(usernameError = "Username must be at least 3 characters")
            isValid = false
        }
        
        if (_state.value.password.isBlank()) {
            _state.value = _state.value.copy(passwordError = "Password is required")
            isValid = false
        } else if (_state.value.password.length < 8) {
            _state.value = _state.value.copy(passwordError = "Password must be at least 8 characters")
            isValid = false
        }
        
        if (_state.value.confirmPassword != _state.value.password) {
            _state.value = _state.value.copy(confirmPasswordError = "Passwords do not match")
            isValid = false
        }
        
        if (!_state.value.acceptTerms) {
            _state.value = _state.value.copy(error = "Please accept the terms and conditions")
            isValid = false
        }
        
        return isValid
    }
    
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$".toRegex()
        return emailRegex.matches(email)
    }
    
    fun canLogin(): Boolean {
        return _state.value.email.isNotBlank() && _state.value.password.isNotBlank()
    }
    
    fun canRegister(): Boolean {
        return _state.value.email.isNotBlank() && 
               _state.value.password.isNotBlank() && 
               _state.value.username.isNotBlank() &&
               _state.value.acceptTerms
    }
    
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}