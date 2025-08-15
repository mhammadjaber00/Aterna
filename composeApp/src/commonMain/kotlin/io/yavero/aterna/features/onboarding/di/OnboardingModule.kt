package io.yavero.aterna.features.onboarding.di

import io.yavero.aterna.features.onboarding.presentation.OnboardingStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val onboardingModule = module {


    single<OnboardingStore> {
        OnboardingStore(
            settingsRepository = get(), 
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        )
    }
}