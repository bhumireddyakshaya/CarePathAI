package com.example.carepathai.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.carepathai.ui.theme.CarePathAITheme

@Composable
fun WellnessTrackerScreen(
    onFoodRecommendationClick: () -> Unit,
    onExerciseRecommendationClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Wellness Tracker",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        // Progress Summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            WellnessRing(progress = 0.7f, label = "Water", color = Color(0xFF2196F3))
            WellnessRing(progress = 0.85f, label = "Sleep", color = Color(0xFF673AB7))
            WellnessRing(progress = 0.5f, label = "Steps", color = Color(0xFF4CAF50))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Tracking Cards
        TrackingCard(
            title = "Water Intake",
            value = "1500 / 2500 ml",
            icon = Icons.Default.WaterDrop,
            color = Color(0xFF2196F3),
            onAdd = {}
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TrackingCard(
            title = "Sleep Hours",
            value = "7.5 / 8 hours",
            icon = Icons.Default.Bedtime,
            color = Color(0xFF673AB7),
            onAdd = {}
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "AI Insights",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "You're almost at your hydration goal! Drinking 2 more glasses of water will improve your focus.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recommendations Buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onFoodRecommendationClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Restaurant, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Diet Plan")
            }
            Button(
                onClick = onExerciseRecommendationClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Exercises")
            }
        }
    }
}

@Composable
fun WellnessRing(progress: Float, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
            Canvas(modifier = Modifier.size(70.dp)) {
                drawCircle(
                    color = color.copy(alpha = 0.2f),
                    style = Stroke(width = 8.dp.toPx())
                )
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360 * progress,
                    useCenter = false,
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun TrackingCard(title: String, value: String, icon: ImageVector, color: Color, onAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(value, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onAdd) {
                Icon(Icons.Default.AddCircle, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WellnessTrackerScreenPreview() {
    CarePathAITheme {
        WellnessTrackerScreen(
            onFoodRecommendationClick = {},
            onExerciseRecommendationClick = {}
        )
    }
}
