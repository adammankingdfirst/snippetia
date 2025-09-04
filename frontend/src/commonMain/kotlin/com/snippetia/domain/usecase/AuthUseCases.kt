package com.snippetia.domain.usecase

import com.snippetia.data.repository.AuthRepository
import com.snippetia.domain.model.User
import kotlinx.coroutines.flow.Flow

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(
        email: String,
        password: String,
        rememberMe: Boolean = false
    ): Flow<Result<User>> {
        return authRepository.login(email, password, rememberMe)
    }
}

class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        username: String
    ): Flow<Result<User>> {
        return authRepository.register(
            email = email,
            password = password,
            firstName = firstName,
            lastName = lastName,
            username = username
        )
    }
}

class ForgotPasswordUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(email: String): Flow<Result<Unit>> {
        return authRepository.forgotPassword(email)
    }
}

class LogoutUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<Result<Unit>> {
        return authRepository.logout()
    }
}

class GetCurrentUserUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<Result<User?>> {
        return authRepository.getCurrentUser()
    }
}

class RefreshTokenUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<Result<String>> {
        return authRepository.refreshToken()
    }
}

class VerifyEmailUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(token: String): Flow<Result<Unit>> {
        return authRepository.verifyEmail(token)
    }
}

class ResetPasswordUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(
        token: String,
        newPassword: String
    ): Flow<Result<Unit>> {
        return authRepository.resetPassword(token, newPassword)
    }
}

class ChangePasswordUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(
        currentPassword: String,
        newPassword: String
    ): Flow<Result<Unit>> {
        return authRepository.changePassword(currentPassword, newPassword)
    }
}

class UpdateProfileUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(
        firstName: String?,
        lastName: String?,
        bio: String?,
        avatarUrl: String?
    ): Flow<Result<User>> {
        return authRepository.updateProfile(firstName, lastName, bio, avatarUrl)
    }
}