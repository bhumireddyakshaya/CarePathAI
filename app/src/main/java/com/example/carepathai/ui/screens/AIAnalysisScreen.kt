package com.example.carepathai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.carepathai.ui.viewmodel.AnalysisViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAnalysisScreen(
    symptoms: List<String>,
    onBack: () -> Unit,
    onNavigateToFood: () -> Unit,
    onNavigateToExercise: () -> Unit,
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    val result by viewModel.analysisResult.collectAsState()

    LaunchedEffect(symptoms) {
        viewModel.performAnalysis(symptoms)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Health Analysis", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE8F5E9)
                )
            )
        }
    ) { innerPadding ->
        if (result == null) {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE8F5E9)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("AI is analyzing your symptoms...", color = Color(0xFF2E7D32))
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Brush.verticalGradient(listOf(Color(0xFFE8F5E9), Color.White)))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Analysis Score & Condition
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                            CircularProgressIndicator(
                                progress = 0.85f,
                                modifier = Modifier.fillMaxSize(),
                                strokeWidth = 8.dp,
                                color = Color(0xFF4CAF50),
                                trackColor = Color(0xFFE8F5E9)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("85%", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                                Text("Confidence", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Possible Condition", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                        Text(result?.diagnosis ?: "Analyzing...", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Surface(
                            shape = CircleShape,
                            color = if(result?.riskLevel == "High") Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if(result?.riskLevel == "High") Color.Red else Color.Green))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Risk Level: ${result?.riskLevel ?: "Unknown"}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // AI Insights
                Text("AI Health Insights", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "Based on your symptoms (${symptoms.joinToString(", ")}), the AI suggests this might be related to ${result?.diagnosis?.lowercase() ?: "the condition identified"}. We recommend monitoring your temperature.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Next Actions
                Text("Recommended Next Actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                
                ActionItem(Icons.Default.MonitorWeight, "Monitor your vitals twice daily.")
                ActionItem(Icons.Default.WaterDrop, "Increase fluid intake (at least 2.5L).")
                ActionItem(Icons.Default.Bedtime, "Ensure adequate rest (7-8 hours).")

                Spacer(modifier = Modifier.height(24.dp))

                // Recommended Food & Exercise Cards
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MiniRecommendationCard("Diet Plan", Icons.Default.Restaurant, Color(0xFF4CAF50), Modifier.weight(1f), onNavigateToFood)
                    MiniRecommendationCard("Exercises", Icons.Default.FitnessCenter, Color(0xFF2196F3), Modifier.weight(1f), onNavigateToExercise)
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Consult Doctor Button
                Button(
                    onClick = { /* Navigate to Doctor Search */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Icon(Icons.Default.LocalHospital, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Consult a Doctor", fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Back to Dashboard")
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ActionItem(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun MiniRecommendationCard(title: String, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
        }
    }
}
