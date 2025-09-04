package com.snippetia.data.api

import com.snippetia.data.dto.*

interface AuthApiService {
    suspend fun login(request: LoginRequest): ApiResponse<AuthResponse>
    suspend fun register(request: RegisterRequest): ApiResponse<AuthResponse>
    suspend fun refreshToken(): ApiResponse<AuthResponse>
    suspend fun logout(): ApiResponse<Unit>
    suspend fun getCurrentUser(): ApiResponse<UserResponse>
}

class AuthApiServiceImpl(
    private val apiClient: ApiClient
) : AuthApiService {
    
    override suspend fun login(request: LoginRequest): ApiResponse<AuthResponse> {
        return apiClient.post("/auth/login", request)
    }
    
    override suspend fun register(request: RegisterRequest): ApiResponse<AuthResponse> {
        return apiClient.post("/auth/register", request)
    }
    
    override suspend fun refreshToken(): ApiResponse<AuthResponse> {
        return apiClient.post("/auth/refresh")
    }
    
    override suspend fun logout(): ApiResponse<Unit> {
        return apiClient.post("/auth/logout")
    }
    
    override suspend fun getCurrentUser(): ApiResponse<UserResponse> {
        return apiClient.get("/auth/me")
    }
}