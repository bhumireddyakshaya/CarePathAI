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
        if (symptoms.isNotEmpty()) {
            viewModel.performAnalysis(symptoms)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Health Analysis", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        }
    ) { innerPadding ->
        if (result == null) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("AI is analyzing your symptoms...", color = Color.White)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.Black)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Analysis Score & Condition
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                            CircularProgressIndicator(
                                progress = 0.85f,
                                modifier = Modifier.fillMaxSize(),
                                strokeWidth = 8.dp,
                                color = if(result?.riskLevel == "High") Color.Red else Color(0xFF4CAF50),
                                trackColor = Color.DarkGray
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("85%", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge, color = Color.White)
                                Text("Confidence", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Possible Condition", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                        Text(result?.diagnosis ?: "Analyzing...", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Surface(
                            shape = CircleShape,
                            color = if(result?.riskLevel == "High") Color(0x33FF0000) else Color(0x334CAF50),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if(result?.riskLevel == "High") Color.Red else Color.Green))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Risk Level: ${result?.riskLevel ?: "Unknown"}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // AI Insights
                Text("AI Health Insights", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF262626))
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "Based on your symptoms (${symptoms.joinToString(", ")}), the AI suggests this might be related to ${result?.diagnosis?.lowercase() ?: "the condition identified"}. Monitor your symptoms closely.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Next Actions
                Text("Recommended Next Actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                
                ActionItem(Icons.Default.MonitorWeight, "Monitor your vitals twice daily.", Color.White)
                ActionItem(Icons.Default.WaterDrop, "Increase fluid intake (at least 2.5L).", Color.White)
                ActionItem(Icons.Default.Bedtime, "Ensure adequate rest (7-8 hours).", Color.White)

                Spacer(modifier = Modifier.height(24.dp))

                // Recommended Food & Exercise Cards
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MiniRecommendationCard("Diet Plan", Icons.Default.Restaurant, Color(0xFF4CAF50), Modifier.weight(1f), onNavigateToFood)
                    MiniRecommendationCard("Exercises", Icons.Default.FitnessCenter, Color(0xFF2196F3), Modifier.weight(1f), onNavigateToExercise)
                }

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(Color.White, Color.Gray)))
                ) {
                    Text("Back to Dashboard")
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ActionItem(icon: ImageVector, text: String, tint: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Color.White)
    }
}

@Composable
fun MiniRecommendationCard(title: String, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge, color = Color.White)
        }
    }
}
