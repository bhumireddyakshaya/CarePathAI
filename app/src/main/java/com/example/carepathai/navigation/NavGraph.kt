package com.example.carepathai.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.carepathai.ui.screens.*

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(route = Screen.Splash.route) {
            SplashScreen(onNext = {
                navController.navigate(Screen.Onboarding.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }
        composable(route = Screen.Onboarding.route) {
            OnboardingScreen(onFinished = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            })
        }
        composable(route = Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }
        composable(route = Screen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(route = Screen.SymptomAssessment.route) {
            SymptomAssessmentScreen(
                onBack = { navController.popBackStack() },
                onAnalyze = { symptoms ->
                    val symptomsStr = symptoms.joinToString(",")
                    navController.navigate(Screen.AIAnalysis.route + "/$symptomsStr")
                }
            )
        }
        composable(
            route = Screen.AIAnalysis.route + "/{symptoms}",
            arguments = listOf(navArgument("symptoms") { type = NavType.StringType })
        ) { backStackEntry ->
            val symptomsStr = backStackEntry.arguments?.getString("symptoms") ?: ""
            val symptoms = symptomsStr.split(",").filter { it.isNotEmpty() }
            AIAnalysisScreen(
                symptoms = symptoms,
                onBack = { navController.popBackStack() },
                onNavigateToFood = { navController.navigate(Screen.FoodRecommendations.route) },
                onNavigateToExercise = { navController.navigate(Screen.ExerciseRecommendations.route) }
            )
        }
        composable(route = Screen.FoodRecommendations.route) {
            FoodRecommendationsScreen(onBack = { navController.popBackStack() })
        }
        composable(route = Screen.ExerciseRecommendations.route) {
            ExerciseRecommendationsScreen(onBack = { navController.popBackStack() })
        }
        composable(route = Screen.MedicineReminder.route) {
            MedicineReminderScreen(onBack = { navController.popBackStack() })
        }
        composable(route = Screen.Emergency.route) {
            EmergencyScreen(onBack = { navController.popBackStack() })
        }
    }
}
