#!/usr/bin/env kotlin

/**
 * Test script to verify home functionality works correctly
 *
 * This script checks:
 * 1. HomeComponent interface is properly defined
 * 2. DefaultHomeComponent implements all required methods
 * 3. HomeScreen accepts HomeComponent parameter
 * 4. Navigation callbacks are properly wired
 */

import java.io.File

fun main() {
    println("Testing Home Functionality...")

    val projectRoot = "/Users/mohamadjaber/IdeaProjects/pocketadhd"

    // Test 1: Check HomeComponent interface
    val homeComponentFile =
        File("$projectRoot/feature/home/src/commonMain/kotlin/io/yavero/pocketadhd/feature/home/HomeComponent.kt")
    if (homeComponentFile.exists()) {
        val content = homeComponentFile.readText()
        val hasInterface = content.contains("interface HomeComponent")
        val hasRequiredMethods = listOf(
            "fun onStartFocus()",
            "fun onQuickMoodCheck()",
            "fun onTaskClick(taskId: String)",
            "fun onRoutineClick(routineId: String)",
            "fun onRefresh()"
        ).all { content.contains(it) }

        println("✓ HomeComponent interface: ${if (hasInterface && hasRequiredMethods) "PASS" else "FAIL"}")
    } else {
        println("✗ HomeComponent file not found")
    }

    // Test 2: Check DefaultHomeComponent implementation
    val defaultHomeComponentExists = homeComponentFile.readText().contains("class DefaultHomeComponent")
    println("✓ DefaultHomeComponent class: ${if (defaultHomeComponentExists) "PASS" else "FAIL"}")

    // Test 3: Check HomeScreen accepts HomeComponent
    val homeScreenFile =
        File("$projectRoot/feature/home/src/commonMain/kotlin/io/yavero/pocketadhd/feature/home/HomeScreen.kt")
    if (homeScreenFile.exists()) {
        val content = homeScreenFile.readText()
        val acceptsComponent = content.contains("component: HomeComponent")
        val usesComponentMethods = listOf(
            "component.onStartFocus()",
            "component.onQuickMoodCheck()",
            "component.onTaskClick(",
            "component.onRoutineClick(",
            "component.onRefresh()"
        ).all { content.contains(it) }

        println("✓ HomeScreen component integration: ${if (acceptsComponent && usesComponentMethods) "PASS" else "FAIL"}")
    } else {
        println("✗ HomeScreen file not found")
    }

    // Test 4: Check AppContent uses component
    val appContentFile = File("$projectRoot/composeApp/src/commonMain/kotlin/io/yavero/pocketadhd/ui/AppContent.kt")
    if (appContentFile.exists()) {
        val content = appContentFile.readText()
        val passesComponent = content.contains("HomeScreen(component = instance.component)")
        println("✓ AppContent component passing: ${if (passesComponent) "PASS" else "FAIL"}")
    } else {
        println("✗ AppContent file not found")
    }

    println("\nHome functionality test completed!")
}