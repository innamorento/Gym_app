package com.gym.tracker.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gym.core.database.dao.UserProfileDao
import com.gym.core.ui.theme.GymAccentGreen
import com.gym.core.ui.theme.GymDarkBackground
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

object Destinations {
    const val ONBOARDING = "onboarding"
    const val TEMPLATES_LIST = "templates_list"
    const val TEMPLATE_EDITOR = "template_editor/{templateId}"
    const val EXERCISE_PICKER = "exercise_picker?target={target}"
    const val LIVE_WORKOUT = "live_workout"
    const val EQUIPMENT_SETTINGS = "equipment_settings"
    const val COACH_REVIEWS = "coach_reviews"
    const val ANALYTICS = "analytics"

    fun templateEditorRoute(templateId: Long) = "template_editor/$templateId"
    fun exercisePickerRoute(target: String = "template") = "exercise_picker?target=$target"
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

    NavHost(
        navController = navController,
        startDestination = if (hasCompletedProfile == true) Destinations.TEMPLATES_LIST else Destinations.ONBOARDING,
        modifier = modifier
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
            TemplatesListScreen(
                viewModel = templatesViewModel,
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
            CoachReviewScreen(
                reviews = emptyList(),
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
                    } else {
                        templatesViewModel.handleIntent(TemplateEditorIntent.AddExercise(exercise))
                    }
                    navController.popBackStack()
                }
            )
        }

        composable(Destinations.LIVE_WORKOUT) {
            LiveWorkoutScreen(
                viewModel = workoutViewModel,
                onFinishClick = {
                    navController.popBackStack(Destinations.TEMPLATES_LIST, inclusive = false)
                },
                onAddExerciseClick = {
                    navController.navigate(Destinations.exercisePickerRoute("workout"))
                }
            )
        }
    }
}
