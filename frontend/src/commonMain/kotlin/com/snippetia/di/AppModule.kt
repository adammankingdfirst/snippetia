package com.snippetia.di

import com.snippetia.presentation.viewmodel.HomeScreenModel
import org.koin.dsl.module

val appModule = module {
    // ViewModels
    factory { HomeScreenModel() }
    
    // TODO: Add repositories, use cases, and other dependencies
}

fun initKoin() {
    org.koin.core.context.startKoin {
        modules(appModule)
    }
}