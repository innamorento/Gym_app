package com.gym.tracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gym.feature.exercises.ExercisesViewModel
import com.gym.feature.exercises.ui.ExercisesScreen
import com.gym.feature.templates.TemplateEditorIntent
import com.gym.feature.templates.TemplatesViewModel
import com.gym.feature.templates.ui.EquipmentSettingsScreen
import com.gym.feature.templates.ui.TemplateEditorScreen
import com.gym.feature.templates.ui.TemplatesListScreen
import com.gym.feature.workout.WorkoutViewModel
import com.gym.feature.workout.ui.CoachReviewScreen
import com.gym.feature.workout.ui.LiveWorkoutScreen
import org.koin.androidx.compose.koinViewModel

object Destinations {
    const val TEMPLATES_LIST = "templates_list"
    const val TEMPLATE_EDITOR = "template_editor/{templateId}"
    const val EXERCISE_PICKER = "exercise_picker?target={target}"
    const val LIVE_WORKOUT = "live_workout"
    const val EQUIPMENT_SETTINGS = "equipment_settings"
    const val COACH_REVIEWS = "coach_reviews"

    fun templateEditorRoute(templateId: Long) = "template_editor/$templateId"
    fun exercisePickerRoute(target: String = "template") = "exercise_picker?target=$target"
}

@Composable
fun GymNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val templatesViewModel: TemplatesViewModel = koinViewModel()
    val exercisesViewModel: ExercisesViewModel = koinViewModel()
    val workoutViewModel: WorkoutViewModel = koinViewModel()

    NavHost(
        navController = navController,
        startDestination = Destinations.TEMPLATES_LIST,
        modifier = modifier
    ) {
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
                onCoachReviewsClick = {
                    navController.navigate(Destinations.COACH_REVIEWS)
                }
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
                onAddExerciseClick = { navController.navigate(Destinations.exercisePickerRoute("template")) }
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
