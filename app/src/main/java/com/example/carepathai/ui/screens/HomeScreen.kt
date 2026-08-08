package com.example.carepathai.ui.screens

import android.net.Uri
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.carepathai.ui.theme.CarePathAITheme
import androidx.navigation.NavHostController

import androidx.hilt.navigation.compose.hiltViewModel
import com.example.carepathai.data.local.entity.Medicine
import com.example.carepathai.ui.viewmodel.HomeViewModel

sealed class BottomNavItem(val title: String, val icon: ImageVector, val route: String) {
    object Home : BottomNavItem("Home", Icons.Default.Home, "home_tab")
    object Assessment : BottomNavItem("Assessment", Icons.Default.HealthAndSafety, "assessment_tab")
    object History : BottomNavItem("History", Icons.Default.History, "history_tab")
    object Profile : BottomNavItem("Profile", Icons.Default.Person, "profile_tab")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val userMedicines by homeViewModel.userMedicines.collectAsState()
    val healthHistory by homeViewModel.healthHistory.collectAsState()
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Assessment,
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
                        onClick = { 
                            if (selectedItem != index) {
                                selectedItem = index
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedItem) {
                0 -> DashboardScreen(
                    medicines = userMedicines,
                    healthHistory = healthHistory,
                    onSymptomCheckerClick = { selectedItem = 1 },
                    onMedicineClick = { navController.navigate("medicine_reminder") },
                    onEmergencyClick = { navController.navigate("emergency") },
                    onHistoryClick = { selectedItem = 2 },
                    onFoodClick = { navController.navigate("food_recommendations") },
                    onExerciseClick = { navController.navigate("exercise_recommendations") }
                )
                1 -> SymptomAssessmentScreen(
                    onBack = { selectedItem = 0 },
                    onAnalyze = { symptoms ->
                        val symptomsStr = symptoms.joinToString(",")
                        val encodedSymptoms = Uri.encode(symptomsStr)
                        navController.navigate("ai_analysis/$encodedSymptoms")
                    }
                )
                2 -> HistoryScreen()
                3 -> ProfileScreen(onLogout = {
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
    medicines: List<Medicine>,
    healthHistory: List<com.example.carepathai.data.local.entity.HealthHistory> = emptyList(),
    onSymptomCheckerClick: () -> Unit,
    onMedicineClick: () -> Unit,
    onEmergencyClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onFoodClick: () -> Unit,
    onExerciseClick: () -> Unit
) {
    val totalCount = medicines.size
    val takenCount = medicines.count { it.isTaken }
    val progressRatio = if (totalCount > 0) takenCount.toFloat() / totalCount.toFloat() else 0f
    val adherenceSubtext = if (totalCount > 0) "$takenCount of $totalCount doses taken today" else "No medicines added yet"

    val latestHistory = healthHistory.firstOrNull()
    val wellnessScore = when (latestHistory?.riskLevel) {
        "High" -> "50/100"
        "Medium" -> "75/100"
        "Low" -> "95/100"
        else -> "100/100"
    }
    val wellnessMessage = when (latestHistory?.riskLevel) {
        "High" -> "High risk concern detected. Consult a healthcare professional."
        "Medium" -> "Moderate concern detected. Follow AI recommendations."
        "Low" -> "You're doing great! Keep up the healthy routine."
        else -> "Complete a symptom assessment to track your score."
    }

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
                Text(wellnessScore, style = MaterialTheme.typography.displaySmall)
                Text(wellnessMessage)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Adherence Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
            onClick = onMedicineClick
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    progress = { progressRatio },
                    modifier = Modifier.size(40.dp),
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Medicine Adherence", fontWeight = FontWeight.Bold)
                    Text(adherenceSubtext, style = MaterialTheme.typography.bodyMedium)
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
        
        val firstMed = medicines.firstOrNull()
        val reminderText = if (firstMed != null) {
            val timeStr = firstMed.morningTime ?: firstMed.afternoonTime ?: firstMed.eveningTime ?: firstMed.nightTime ?: "Today"
            "${firstMed.name} - ${firstMed.dosage} ($timeStr)"
        } else {
            "No upcoming medicine reminders"
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onMedicineClick
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Medication, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Medicine Reminder", fontWeight = FontWeight.Bold)
                    Text(reminderText, style = MaterialTheme.typography.bodyMedium)
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
            DashboardCard("Recommended Food", Icons.Default.Restaurant, Modifier.weight(1f), onFoodClick)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DashboardCard("Exercise Plan", Icons.Default.FitnessCenter, Modifier.weight(1f), onExerciseClick)
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

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    CarePathAITheme {
        DashboardScreen(
            medicines = emptyList(),
            onSymptomCheckerClick = {},
            onMedicineClick = {},
            onEmergencyClick = {},
            onHistoryClick = {},
            onFoodClick = {},
            onExerciseClick = {}
        )
    }
}
