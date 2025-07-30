rootProject.name = "pocketadhd"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

includeBuild("build-logic")

include(":composeApp")

// Core modules
include(":core:ui")
include(":core:designsystem")
include(":core:domain")
include(":core:data")
include(":core:notifications")
include(":core:export")

// Feature modules
include(":feature:home")
include(":feature:planner")
include(":feature:focus")
include(":feature:routines")
include(":feature:mood")
include(":feature:settings")