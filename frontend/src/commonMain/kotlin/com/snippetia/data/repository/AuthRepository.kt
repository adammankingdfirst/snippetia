package com.snippetia.data.repository

import com.snippetia.data.api.ApiResponse
import com.snippetia.data.api.AuthApiService
import com.snippetia.data.dto.*
import com.snippetia.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface AuthRepository {
    suspend fun login(username: String, password: String): Flow<Result<AuthResult>>
    suspend fun register(username: String, email: String, password: String, displayName: String): Flow<Result<AuthResult>>
    suspend fun refreshToken(): Flow<Result<AuthResult>>
    suspend fun logout(): Flow<Result<Unit>>
    suspend fun getCurrentUser(): Flow<Result<User>>
    suspend fun isLoggedIn(): Boolean
    suspend fun getStoredToken(): String?
    suspend fun storeToken(token: String)
    suspend fun clearToken()
}

class AuthRepositoryImpl(
    private val apiService: AuthApiService,
    private val tokenStorage: TokenStorage
) : AuthRepository {
    
    override suspend fun login(username: String, password: String): Flow<Result<AuthResult>> = flow {
        val request = LoginRequest(username, password)
        when (val response = apiService.login(request)) {
            is ApiResponse.Success -> {
                tokenStorage.storeToken(response.data.token)
                val result = AuthResult(
                    token = response.data.token,
                    user = response.data.user.toDomainModel()
                )
                emit(Result.success(result))
            }
            is ApiResponse.Error -> {
                emit(Result.failure(Exception("${response.code}: ${response.message}")))
            }
        }
    }
    
    override suspend fun register(
        username: String,
        email: String,
        password: String,
        displayName: String
    ): Flow<Result<AuthResult>> = flow {
        val request = RegisterRequest(username, email, password, displayName)
        when (val response = apiService.register(request)) {
            is ApiResponse.Success -> {
                tokenStorage.storeToken(response.data.token)
                val result = AuthResult(
                    token = response.data.token,
                    user = response.data.user.toDomainModel()
                )
                emit(Result.success(result))
            }
            is ApiResponse.Error -> {
                emit(Result.failure(Exception("${response.code}: ${response.message}")))
            }
        }
    }
    
    override suspend fun refreshToken(): Flow<Result<AuthResult>> = flow {
        when (val response = apiService.refreshToken()) {
            is ApiResponse.Success -> {
                tokenStorage.storeToken(response.data.token)
                val result = AuthResult(
                    token = response.data.token,
                    user = response.data.user.toDomainModel()
                )
                emit(Result.success(result))
            }
            is ApiResponse.Error -> {
                emit(Result.failure(Exception("${response.code}: ${response.message}")))
            }
        }
    }
    
    override suspend fun logout(): Flow<Result<Unit>> = flow {
        when (val response = apiService.logout()) {
            is ApiResponse.Success -> {
                tokenStorage.clearToken()
                emit(Result.success(Unit))
            }
            is ApiResponse.Error -> {
                // Even if API call fails, clear local token
                tokenStorage.clearToken()
                emit(Result.success(Unit))
            }
        }
    }
    
    override suspend fun getCurrentUser(): Flow<Result<User>> = flow {
        when (val response = apiService.getCurrentUser()) {
            is ApiResponse.Success -> {
                emit(Result.success(response.data.toDomainModel()))
            }
            is ApiResponse.Error -> {
                emit(Result.failure(Exception("${response.code}: ${response.message}")))
            }
        }
    }
    
    override suspend fun isLoggedIn(): Boolean {
        return tokenStorage.getToken() != null
    }
    
    override suspend fun getStoredToken(): String? {
        return tokenStorage.getToken()
    }
    
    override suspend fun storeToken(token: String) {
        tokenStorage.storeToken(token)
    }
    
    override suspend fun clearToken() {
        tokenStorage.clearToken()
    }
}

// Extension function to convert API model to domain model
private fun UserResponse.toDomainModel(): User {
    return User(
        id = this.id,
        username = this.username,
        displayName = this.displayName,
        avatarUrl = this.avatarUrl,
        bio = this.bio,
        githubUsername = this.githubUsername,
        twitterUsername = this.twitterUsername,
        websiteUrl = this.websiteUrl,
        isEmailVerified = this.isEmailVerified,
        isTwoFactorEnabled = this.isTwoFactorEnabled,
        accountStatus = this.accountStatus,
        createdAt = LocalDateTime.parse(this.createdAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    )
}

data class AuthResult(
    val token: String,
    val user: User
)

interface TokenStorage {
    suspend fun storeToken(token: String)
    suspend fun getToken(): String?
    suspend fun clearToken()
}

// Platform-specific implementation would be provided
expect class TokenStorageImpl() : TokenStorage