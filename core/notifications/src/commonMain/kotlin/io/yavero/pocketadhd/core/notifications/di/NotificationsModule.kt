package io.yavero.pocketadhd.core.notifications.di

import io.yavero.pocketadhd.core.notifications.LocalNotifier
import org.koin.dsl.module

/**
 * Koin DI module for notifications
 * 
 * Provides LocalNotifier instance for cross-platform notification scheduling.
 * Platform-specific implementations are provided through expect/actual pattern.
 */
val notificationsModule = module {
    // LocalNotifier is provided by platform-specific modules
    // This module can include any common notification-related dependencies
}