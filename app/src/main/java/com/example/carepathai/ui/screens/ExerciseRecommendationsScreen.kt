package com.example.carepathai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

data class ExerciseItem(
    val name: String,
    val duration: String,
    val intensity: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    var isCompleted: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseRecommendationsScreen(onBack: () -> Unit) {
    var exercises by remember { mutableStateOf(listOf(
        ExerciseItem("Deep Breathing", "5 mins", "Low", "Focus on slow, rhythmic breaths to relax.", Icons.Default.Air, Color(0xFF64B5F6)),
        ExerciseItem("Neck & Shoulder Stretch", "10 mins", "Low", "Gentle movements to release tension.", Icons.Default.SelfImprovement, Color(0xFF9575CD)),
        ExerciseItem("Brisk Walking", "20 mins", "Medium", "Keep a steady pace to boost heart health.", Icons.Default.DirectionsWalk, Color(0xFF4DB6AC)),
        ExerciseItem("Light Yoga", "15 mins", "Low", "Basic poses for flexibility and calm.", Icons.Default.Spa, Color(0xFF81C784))
    )) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Wellness Exercises", 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF3E5F5)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Brush.verticalGradient(listOf(Color(0xFFF3E5F5), Color.White)))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF9575CD))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "AI Wellness Insight",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "“Light movements and breathing exercises will help reduce your fatigue.”",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Completion Progress
                    val completedCount = exercises.count { it.isCompleted }
                    val progress = completedCount.toFloat() / exercises.size
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier.weight(1f).height(8.dp).clip(CircleShape),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "$completedCount/${exercises.size}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recovery Section
            Text(
                "Recovery Focus",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF512DA8)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF3F51B5))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Avoid intense workouts. Focus on mobility and hydration today.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Exercise List
            Text(
                "Today's Personalized Plan",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF512DA8)
            )
            Spacer(modifier = Modifier.height(12.dp))
            exercises.forEachIndexed { index, exercise ->
                ExerciseListItem(
                    exercise = exercise,
                    onCompleteChange = { completed ->
                        val newList = exercises.toMutableList()
                        newList[index] = exercise.copy(isCompleted = completed)
                        exercises = newList
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AI Suggestion Tip
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFF4CAF50))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Pro Tip", fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
                        Text(
                            "Do these exercises in a well-ventilated room for better oxygen intake.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ExerciseListItem(exercise: ExerciseItem, onCompleteChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (exercise.isCompleted) Color(0xFFF5F5F5) else Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = exercise.color.copy(alpha = 0.1f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(exercise.icon, contentDescription = null, tint = exercise.color, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${exercise.duration} • ${exercise.intensity}",
                    color = exercise.color,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    exercise.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (exercise.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                )
                Text(exercise.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Checkbox(
                checked = exercise.isCompleted,
                onCheckedChange = onCompleteChange,
                colors = CheckboxDefaults.colors(checkedColor = exercise.color)
            )
        }
    }
}
