package com.snippetia.di

import com.snippetia.data.api.*
import com.snippetia.data.network.createHttpClient
import com.snippetia.data.repository.*
import com.snippetia.domain.usecase.*
import com.snippetia.presentation.viewmodel.*
import kotlinx.coroutines.runBlocking
import org.koin.dsl.module

val appModule = module {
    // Local Storage
    single<TokenStorage> { TokenStorageImpl() }
    
    // Network
    single { 
        createHttpClient { 
            get<TokenStorage>().getToken() 
        } 
    }
    single { ApiClient(get()) }
    
    // API Services
    single<SnippetApiService> { SnippetApiServiceImpl(get()) }
    single<AuthApiService> { AuthApiServiceImpl(get()) }
    
    // Repositories
    single<SnippetRepository> { SnippetRepositoryImpl(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    
    // Use Cases - Snippets
    factory { GetSnippetsUseCase(get()) }
    factory { GetFeaturedSnippetsUseCase(get()) }
    factory { GetTrendingSnippetsUseCase(get()) }
    factory { SearchSnippetsUseCase(get()) }
    factory { GetSnippetUseCase(get()) }
    factory { LikeSnippetUseCase(get()) }
    factory { ForkSnippetUseCase(get()) }
    factory { CreateSnippetUseCase(get()) }
    factory { UpdateSnippetUseCase(get()) }
    factory { DeleteSnippetUseCase(get()) }
    
    // Use Cases - Auth
    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }
    factory { ForgotPasswordUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { GetCurrentUserUseCase(get()) }
    factory { RefreshTokenUseCase(get()) }
    factory { VerifyEmailUseCase(get()) }
    factory { ResetPasswordUseCase(get()) }
    factory { ChangePasswordUseCase(get()) }
    factory { UpdateProfileUseCase(get()) }
    
    // ViewModels / Screen Models
    factory { HomeScreenModel(get(), get(), get(), get(), get(), get()) }
    factory { CreateSnippetScreenModel(get(), get()) }
    factory { SnippetDetailScreenModel(get(), get(), get()) }
    factory { AuthScreenModel(get(), get(), get()) }
}

fun initKoin() {
    org.koin.core.context.startKoin {
        modules(appModule)
    }
}