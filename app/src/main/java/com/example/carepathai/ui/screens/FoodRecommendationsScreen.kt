package com.example.carepathai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class FoodItem(
    val name: String,
    val category: String,
    val description: String,
    val nutritionalValue: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodRecommendationsScreen(onBack: () -> Unit) {
    val meals = listOf(
        FoodItem("Oatmeal with Almonds", "Breakfast", "High in fiber and heart-healthy fats.", "320 kcal | 8g Protein", Icons.Default.BreakfastDining, Color(0xFF8BC34A)),
        FoodItem("Quinoa Salad", "Lunch", "Rich in protein and essential amino acids.", "450 kcal | 15g Protein", Icons.Default.LunchDining, Color(0xFF009688)),
        FoodItem("Baked Mackerel", "Dinner", "Excellent source of Omega-3 fatty acids.", "550 kcal | 25g Protein", Icons.Default.DinnerDining, Color(0xFF00BCD4)),
        FoodItem("Greek Yogurt", "Snack", "Probiotics for gut health and immunity.", "150 kcal | 12g Protein", Icons.Default.Fastfood, Color(0xFFFF9800))
    )

    val immunityFoods = listOf(
        FoodItem("Citrus Fruits", "Immunity", "Vitamin C boost", "", Icons.Default.Eco, Color(0xFFFFC107)),
        FoodItem("Ginger Tea", "Immunity", "Anti-inflammatory", "", Icons.Default.EmojiFoodBeverage, Color(0xFF795548)),
        FoodItem("Spinach", "Immunity", "Rich in antioxidants", "", Icons.Default.Spa, Color(0xFF4CAF50))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Nutrition AI", 
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
                    containerColor = Color(0xFFE0F2F1)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Navigate to AI Chatbot */ },
                containerColor = Color(0xFF009688),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Chat, contentDescription = "AI Assistant")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Brush.verticalGradient(listOf(Color(0xFFE0F2F1), Color.White)))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF009688))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Daily Nutrition Insight",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "“Boost your immunity with Vitamin C-rich foods today.”",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        NutrientInfo("1,850", "Calories", Color.White)
                        NutrientInfo("75g", "Protein", Color.White)
                        NutrientInfo("210g", "Carbs", Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Hydration Section
            Text(
                "Hydration Status",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00796B)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF2196F3))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Target: 2500 ml", fontWeight = FontWeight.Bold)
                        LinearProgressIndicator(
                            progress = 0.6f,
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                            color = Color(0xFF2196F3),
                            trackColor = Color.White
                        )
                        Text("1500 ml consumed", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Meal Plan
            Text(
                "Personalized Meal Plan",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00796B)
            )
            Spacer(modifier = Modifier.height(12.dp))
            meals.forEach { meal ->
                MealListItem(meal)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Immunity Boosters
            Text(
                "Immunity Boosting Foods",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00796B)
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(immunityFoods) { food ->
                    ImmunityFoodCard(food)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Foods to Avoid
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Foods to Avoid", fontWeight = FontWeight.Bold, color = Color.Red)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Avoid oily, processed foods and excessive sugar to speed up recovery.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun NutrientInfo(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.7f))
    }
}

@Composable
fun MealListItem(meal: FoodItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = meal.color.copy(alpha = 0.1f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(meal.icon, contentDescription = null, tint = meal.color, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(meal.category, color = meal.color, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text(meal.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(meal.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                if (meal.nutritionalValue.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(meal.nutritionalValue, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ImmunityFoodCard(food: FoodItem) {
    Card(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(food.icon, contentDescription = null, tint = food.color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(food.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(food.description, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}
