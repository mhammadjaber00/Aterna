package io.yavero.pocketadhd.feature.onboarding.di

import io.yavero.pocketadhd.feature.onboarding.presentation.OnboardingStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

/**
 * Koin DI module for onboarding feature
 */
val onboardingModule = module {

    // Onboarding Store
    single<OnboardingStore> {
        OnboardingStore(
            settingsRepository = get(), // Provided by dataModule
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        )
    }
}