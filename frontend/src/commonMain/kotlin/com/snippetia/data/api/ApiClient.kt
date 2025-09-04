package com.snippetia.data.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

class ApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String = "http://localhost:8080/api"
) {
    
    suspend inline fun <reified T> get(
        endpoint: String,
        parameters: Map<String, Any> = emptyMap()
    ): ApiResponse<T> {
        return try {
            val response = httpClient.get("$baseUrl$endpoint") {
                parameters.forEach { (key, value) ->
                    parameter(key, value.toString())
                }
            }
            
            if (response.status.isSuccess()) {
                ApiResponse.Success(response.body<T>())
            } else {
                ApiResponse.Error(response.status.value, response.bodyAsText())
            }
        } catch (e: Exception) {
            ApiResponse.Error(0, e.message ?: "Unknown error")
        }
    }
    
    suspend inline fun <reified T> post(
        endpoint: String,
        body: Any? = null
    ): ApiResponse<T> {
        return try {
            val response = httpClient.post("$baseUrl$endpoint") {
                contentType(ContentType.Application.Json)
                if (body != null) {
                    setBody(body)
                }
            }
            
            if (response.status.isSuccess()) {
                ApiResponse.Success(response.body<T>())
            } else {
                ApiResponse.Error(response.status.value, response.bodyAsText())
            }
        } catch (e: Exception) {
            ApiResponse.Error(0, e.message ?: "Unknown error")
        }
    }
    
    suspend inline fun <reified T> put(
        endpoint: String,
        body: Any? = null
    ): ApiResponse<T> {
        return try {
            val response = httpClient.put("$baseUrl$endpoint") {
                contentType(ContentType.Application.Json)
                if (body != null) {
                    setBody(body)
                }
            }
            
            if (response.status.isSuccess()) {
                ApiResponse.Success(response.body<T>())
            } else {
                ApiResponse.Error(response.status.value, response.bodyAsText())
            }
        } catch (e: Exception) {
            ApiResponse.Error(0, e.message ?: "Unknown error")
        }
    }
    
    suspend fun delete(endpoint: String): ApiResponse<Unit> {
        return try {
            val response = httpClient.delete("$baseUrl$endpoint")
            
            if (response.status.isSuccess()) {
                ApiResponse.Success(Unit)
            } else {
                ApiResponse.Error(response.status.value, response.bodyAsText())
            }
        } catch (e: Exception) {
            ApiResponse.Error(0, e.message ?: "Unknown error")
        }
    }
}

sealed class ApiResponse<out T> {
    data class Success<T>(val data: T) : ApiResponse<T>()
    data class Error(val code: Int, val message: String) : ApiResponse<Nothing>()
    
    inline fun <R> map(transform: (T) -> R): ApiResponse<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
        }
    }
    
    inline fun onSuccess(action: (T) -> Unit): ApiResponse<T> {
        if (this is Success) action(data)
        return this
    }
    
    inline fun onError(action: (Int, String) -> Unit): ApiResponse<T> {
        if (this is Error) action(code, message)
        return this
    }
}