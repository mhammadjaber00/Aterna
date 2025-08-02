package io.yavero.pocketadhd.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.yavero.pocketadhd.core.ui.theme.AdhdTypography
import io.yavero.pocketadhd.feature.home.component.HomeComponent
import io.yavero.pocketadhd.feature.home.ui.ErrorState
import io.yavero.pocketadhd.feature.home.ui.HomeContent
import io.yavero.pocketadhd.feature.home.ui.LoadingState

/**
 * Home screen showing today's overview
 *
 * ADHD-friendly features:
 * - Card-based layout for easy scanning
 * - Quick actions prominently displayed
 * - Today's focus with minimal cognitive load
 * - Large, clear action buttons
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    component: HomeComponent,
    modifier: Modifier = Modifier
) {
    val uiState by component.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Today",
                        style = AdhdTypography.Default.headlineMedium
                    )
                },
                actions = {
                    IconButton(onClick = { component.onRefresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingState(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.error != null -> {
                    ErrorState(
                        error = uiState.error!!,
                        onRetry = { component.onRefresh() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    HomeContent(
                        uiState = uiState,
                        onStartFocus = { component.onStartFocus() },
                        onQuickMoodCheck = { component.onQuickMoodCheck() },
                        onTaskClick = { taskId -> component.onTaskClick(taskId) },
                        onRoutineClick = { routineId -> component.onRoutineClick(routineId) },
                        onCreateTask = { component.onCreateTask() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}