package com.gym.tracker.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoCameraFront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.gym.core.database.dao.CoachAndReviewDao
import com.gym.core.database.dao.UserProfileDao
import com.gym.core.ui.theme.GymAccentGreen
import com.gym.core.ui.theme.GymAccentGreenDim
import com.gym.core.ui.theme.GymDarkBackground
import com.gym.core.ui.theme.GymDarkSurface
import com.gym.core.ui.theme.GymSurfaceBorder
import com.gym.core.ui.theme.GymTextPrimary
import com.gym.core.ui.theme.GymTextSecondary
import com.gym.feature.exercises.ExercisesViewModel
import com.gym.feature.exercises.ui.ExercisesScreen
import com.gym.feature.templates.TemplateEditorIntent
import com.gym.feature.templates.TemplatesViewModel
import com.gym.feature.templates.onboarding.OnboardingScreen
import com.gym.feature.templates.onboarding.OnboardingViewModel
import com.gym.feature.templates.ui.EquipmentSettingsScreen
import com.gym.feature.templates.ui.TemplateEditorScreen
import com.gym.feature.templates.ui.TemplatesListScreen
import com.gym.feature.workout.WorkoutViewModel
import com.gym.feature.workout.analytics.AnalyticsScreen
import com.gym.feature.workout.analytics.AnalyticsViewModel
import com.gym.feature.workout.ui.CoachReviewScreen
import com.gym.feature.workout.ui.LiveWorkoutScreen
import com.gym.feature.workout.ui.WorkoutHistoryScreen
import com.gym.feature.workout.ui.WorkoutSummaryScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

object Destinations {
    const val ONBOARDING = "onboarding"
    const val TEMPLATES_LIST = "templates_list"
    const val WORKOUT_HISTORY = "workout_history"
    const val WORKOUT_SUMMARY = "workout_summary/{workoutId}"
    const val TEMPLATE_EDITOR = "template_editor/{templateId}"
    const val EXERCISE_PICKER = "exercise_picker?target={target}"
    const val LIVE_WORKOUT = "live_workout"
    const val EQUIPMENT_SETTINGS = "equipment_settings"
    const val COACH_REVIEWS = "coach_reviews"
    const val ANALYTICS = "analytics"

    fun templateEditorRoute(templateId: Long) = "template_editor/$templateId"
    fun exercisePickerRoute(target: String = "template") = "exercise_picker?target=$target"
    fun workoutSummaryRoute(workoutId: Long) = "workout_summary/$workoutId"
}

@Composable
fun GymNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val userProfileDao: UserProfileDao = koinInject()
    var hasCompletedProfile by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val count = userProfileDao.hasCompletedProfile()
            hasCompletedProfile = count > 0
        }
    }

    if (hasCompletedProfile == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(GymDarkBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = GymAccentGreen)
        }
        return
    }

    val templatesViewModel: TemplatesViewModel = koinViewModel()
    val exercisesViewModel: ExercisesViewModel = koinViewModel()
    val workoutViewModel: WorkoutViewModel = koinViewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentTargetArg = navBackStackEntry?.arguments?.getString("target")

    // Нижняя панель навигации видна на основных вкладках приложения, но скрыта в режиме активной тренировки и редактора
    val isTopLevelDestination = currentRoute in listOf(
        Destinations.TEMPLATES_LIST,
        Destinations.WORKOUT_HISTORY,
        Destinations.ANALYTICS,
        Destinations.COACH_REVIEWS
    ) || (currentRoute?.startsWith("exercise_picker") == true && currentTargetArg == "catalog")

    Scaffold(
        bottomBar = {
            if (isTopLevelDestination) {
                GymBottomNavigationBar(
                    currentRoute = currentRoute,
                    targetArg = currentTargetArg,
                    onNavigate = { destination ->
                        navController.navigate(destination) {
                            popUpTo(Destinations.TEMPLATES_LIST) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        containerColor = GymDarkBackground
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (hasCompletedProfile == true) Destinations.TEMPLATES_LIST else Destinations.ONBOARDING,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(Destinations.ONBOARDING) {
                val onboardingViewModel: OnboardingViewModel = koinViewModel()
                OnboardingScreen(
                    viewModel = onboardingViewModel,
                    onOnboardingComplete = {
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        } else {
                            navController.navigate(Destinations.TEMPLATES_LIST) {
                                popUpTo(Destinations.ONBOARDING) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Destinations.TEMPLATES_LIST) {
                val workoutState by workoutViewModel.uiState.collectAsState()
                val activeWorkout = (workoutState as? com.gym.feature.workout.WorkoutUiState.Active)?.workout
                TemplatesListScreen(
                    viewModel = templatesViewModel,
                    activeWorkoutTitle = activeWorkout?.title,
                    onResumeWorkoutClick = if (activeWorkout != null) {
                        { navController.navigate(Destinations.LIVE_WORKOUT) }
                    } else null,
                    onCreateTemplateClick = {
                        templatesViewModel.clearEditorSession()
                        navController.navigate(Destinations.templateEditorRoute(0L))
                    },
                    onTemplateClick = { templateId ->
                        templatesViewModel.clearEditorSession()
                        navController.navigate(Destinations.templateEditorRoute(templateId))
                    },
                    onStartWorkoutClick = { templateId ->
                        workoutViewModel.startWorkoutFromTemplate(templateId)
                        navController.navigate(Destinations.LIVE_WORKOUT)
                    },
                    onStartEmptyWorkoutClick = {
                        workoutViewModel.startEmptyWorkout()
                        navController.navigate(Destinations.LIVE_WORKOUT)
                    },
                    onSettingsClick = {
                        navController.navigate(Destinations.EQUIPMENT_SETTINGS)
                    },
                    onAnalyticsClick = {
                        navController.navigate(Destinations.ANALYTICS)
                    },
                    onCoachReviewsClick = {
                        navController.navigate(Destinations.COACH_REVIEWS)
                    },
                    onProfileClick = {
                        navController.navigate(Destinations.ONBOARDING)
                    }
                )
            }

            composable(Destinations.WORKOUT_HISTORY) {
                WorkoutHistoryScreen(
                    viewModel = workoutViewModel,
                    onWorkoutClick = { workoutId ->
                        navController.navigate(Destinations.workoutSummaryRoute(workoutId))
                    },
                    onRepeatWorkoutClick = { workout ->
                        workoutViewModel.repeatWorkout(workout)
                        navController.navigate(Destinations.LIVE_WORKOUT)
                    },
                    onStartEmptyWorkoutClick = {
                        workoutViewModel.startEmptyWorkout()
                        navController.navigate(Destinations.LIVE_WORKOUT)
                    }
                )
            }

            composable(
                route = Destinations.WORKOUT_SUMMARY,
                arguments = listOf(navArgument("workoutId") { type = NavType.LongType })
            ) { backStackEntry ->
                val workoutId = backStackEntry.arguments?.getLong("workoutId") ?: 0L
                WorkoutSummaryScreen(
                    workoutId = workoutId,
                    viewModel = workoutViewModel,
                    onDoneClick = {
                        navController.navigate(Destinations.TEMPLATES_LIST) {
                            popUpTo(Destinations.TEMPLATES_LIST) { inclusive = false }
                        }
                    }
                )
            }

            composable(Destinations.ANALYTICS) {
                val analyticsViewModel: AnalyticsViewModel = koinViewModel()
                AnalyticsScreen(
                    viewModel = analyticsViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Destinations.EQUIPMENT_SETTINGS) {
                EquipmentSettingsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Destinations.COACH_REVIEWS) {
                val coachDao: CoachAndReviewDao = koinInject()
                val pendingReviews by coachDao.getPendingReviewsForCoach().collectAsState(initial = emptyList())
                CoachReviewScreen(
                    reviews = pendingReviews,
                    onBackClick = { navController.popBackStack() },
                    onSubmitFeedback = { reviewId, setId, uri, comment, rating ->
                        workoutViewModel.submitCoachFeedback(reviewId, setId, uri, comment, rating, "Тренер")
                    }
                )
            }

            composable(
                route = Destinations.TEMPLATE_EDITOR,
                arguments = listOf(navArgument("templateId") { type = NavType.LongType })
            ) { backStackEntry ->
                val templateId = backStackEntry.arguments?.getLong("templateId") ?: 0L
                TemplateEditorScreen(
                    templateId = templateId,
                    viewModel = templatesViewModel,
                    onBackClick = { navController.popBackStack() },
                    onAddExerciseClick = { navController.navigate(Destinations.exercisePickerRoute("template")) },
                    onStartWorkout = { savedTemplateId ->
                        workoutViewModel.startWorkoutFromTemplate(savedTemplateId)
                        navController.navigate(Destinations.LIVE_WORKOUT) {
                            popUpTo(Destinations.TEMPLATES_LIST) { inclusive = false }
                        }
                    }
                )
            }

            composable(
                route = Destinations.EXERCISE_PICKER,
                arguments = listOf(
                    navArgument("target") {
                        type = NavType.StringType
                        defaultValue = "template"
                    }
                )
            ) { backStackEntry ->
                val target = backStackEntry.arguments?.getString("target") ?: "template"
                ExercisesScreen(
                    viewModel = exercisesViewModel,
                    onBackClick = { navController.popBackStack() },
                    onExerciseSelect = { exercise ->
                        if (target == "workout") {
                            workoutViewModel.addExerciseToActiveWorkout(exercise)
                            navController.popBackStack()
                        } else if (target == "template") {
                            templatesViewModel.handleIntent(TemplateEditorIntent.AddExercise(exercise))
                            navController.popBackStack()
                        }
                    }
                )
            }

            composable(Destinations.LIVE_WORKOUT) {
                LiveWorkoutScreen(
                    viewModel = workoutViewModel,
                    onFinishClick = { finishedId ->
                        navController.navigate(Destinations.workoutSummaryRoute(finishedId)) {
                            popUpTo(Destinations.TEMPLATES_LIST) { inclusive = false }
                        }
                    },
                    onDiscardClick = {
                        navController.popBackStack(Destinations.TEMPLATES_LIST, inclusive = false)
                    },
                    onAddExerciseClick = {
                        navController.navigate(Destinations.exercisePickerRoute("workout"))
                    }
                )
            }
        }
    }
}

/**
 * Фирменная нижняя панель навигации (Bottom Navigation Bar) в дизайне Tactical Cyber Lime.
 */
@Composable
fun GymBottomNavigationBar(
    currentRoute: String?,
    targetArg: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = GymDarkSurface,
        contentColor = GymTextPrimary,
        tonalElevation = 0.dp,
        modifier = Modifier.border(
            width = 1.dp,
            color = GymSurfaceBorder
        )
    ) {
        val navItems = listOf(
            NavigationItemData(
                route = Destinations.TEMPLATES_LIST,
                label = "Тренировка",
                icon = Icons.Default.FitnessCenter,
                isSelected = currentRoute == Destinations.TEMPLATES_LIST
            ),
            NavigationItemData(
                route = Destinations.WORKOUT_HISTORY,
                label = "История",
                icon = Icons.AutoMirrored.Filled.EventNote,
                isSelected = currentRoute == Destinations.WORKOUT_HISTORY
            ),
            NavigationItemData(
                route = Destinations.exercisePickerRoute("catalog"),
                label = "Каталог",
                icon = Icons.Default.Search,
                isSelected = currentRoute?.startsWith("exercise_picker") == true && targetArg == "catalog"
            ),
            NavigationItemData(
                route = Destinations.ANALYTICS,
                label = "Аналитика",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                isSelected = currentRoute == Destinations.ANALYTICS
            ),
            NavigationItemData(
                route = Destinations.COACH_REVIEWS,
                label = "Тренер",
                icon = Icons.Default.VideoCameraFront,
                isSelected = currentRoute == Destinations.COACH_REVIEWS
            )
        )

        navItems.forEach { item ->
            NavigationBarItem(
                selected = item.isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = if (item.isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GymAccentGreen,
                    selectedTextColor = GymAccentGreen,
                    unselectedIconColor = GymTextSecondary,
                    unselectedTextColor = GymTextSecondary,
                    indicatorColor = GymAccentGreenDim
                )
            )
        }
    }
}

private data class NavigationItemData(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isSelected: Boolean
)
