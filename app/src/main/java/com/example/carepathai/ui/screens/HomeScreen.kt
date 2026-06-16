package com.example.carepathai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

sealed class BottomNavItem(val title: String, val icon: ImageVector, val route: String) {
    object Home : BottomNavItem("Home", Icons.Default.Home, "home_tab")
    object Assessment : BottomNavItem("Assessment", Icons.Default.HealthAndSafety, "assessment_tab")
    object Wellness : BottomNavItem("Wellness", Icons.Default.Favorite, "wellness_tab")
    object History : BottomNavItem("History", Icons.Default.History, "history_tab")
    object Profile : BottomNavItem("Profile", Icons.Default.Person, "profile_tab")
}

@Composable
fun HomeScreen(navController: NavHostController) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Assessment,
        BottomNavItem.Wellness,
        BottomNavItem.History,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Open AI Chatbot */ }) {
                Icon(Icons.Default.Chat, contentDescription = "AI Assistant")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedItem) {
                0 -> DashboardScreen(
                    onSymptomCheckerClick = { selectedItem = 1 },
                    onMedicineClick = { navController.navigate("medicine_reminder") },
                    onEmergencyClick = { navController.navigate("emergency") },
                    onHistoryClick = { selectedItem = 3 }
                )
                1 -> SymptomAssessmentScreen(
                    onBack = { selectedItem = 0 },
                    onAnalyze = { symptoms ->
                        navController.navigate("ai_analysis")
                    }
                )
                2 -> WellnessTrackerScreen(
                    onFoodRecommendationClick = { navController.navigate("food_recommendations") },
                    onExerciseRecommendationClick = { navController.navigate("exercise_recommendations") }
                )
                3 -> HistoryScreen()
                4 -> ProfileScreen(onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                })
            }
        }
    }
}

@Composable
fun DashboardScreen(
    onSymptomCheckerClick: () -> Unit,
    onMedicineClick: () -> Unit,
    onEmergencyClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Welcome Back!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Track your health and wellness today.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Daily Wellness Score Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Daily Wellness Score", fontWeight = FontWeight.Bold)
                Text("85/100", style = MaterialTheme.typography.displaySmall)
                Text("You're doing great! Keep it up.")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Adherence Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    progress = 0.75f,
                    modifier = Modifier.size(40.dp),
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Medicine Adherence", fontWeight = FontWeight.Bold)
                    Text("3 of 4 doses taken today", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Upcoming Reminders",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Medication, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Medicine Reminder", fontWeight = FontWeight.Bold)
                    Text("Paracetamol - 10:00 AM", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Quick Insights",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        // Grid of cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DashboardCard("Symptom Analysis", Icons.Default.Troubleshoot, Modifier.weight(1f), onSymptomCheckerClick)
            DashboardCard("Recommended Food", Icons.Default.Restaurant, Modifier.weight(1f), {})
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DashboardCard("Exercise Plan", Icons.Default.FitnessCenter, Modifier.weight(1f), {})
            DashboardCard("Health History", Icons.Default.History, Modifier.weight(1f), onHistoryClick)
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onEmergencyClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.Emergency, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Emergency Support")
        }
    }
}

@Composable
fun DashboardCard(title: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(
        modifier = modifier.height(110.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
