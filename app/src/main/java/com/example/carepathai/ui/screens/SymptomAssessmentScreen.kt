package com.example.carepathai.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomAssessmentScreen(onBack: () -> Unit, onAnalyze: (List<String>) -> Unit) {
    var step by remember { mutableIntStateOf(1) }
    var selectedBodyPart by remember { mutableStateOf<String?>(null) }
    val selectedSymptoms = remember { mutableStateListOf<String>() }
    var searchText by remember { mutableStateOf("") }
    
    // Step 2 Data
    var severity by remember { mutableFloatStateOf(1f) }
    var duration by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val bodyParts = listOf(
        BodyPart("Head", Icons.Default.Face),
        BodyPart("Chest", Icons.Default.Favorite),
        BodyPart("Abdomen", Icons.Default.Restaurant), // Placeholder icon
        BodyPart("Arms", Icons.Default.PanTool),
        BodyPart("Legs", Icons.AutoMirrored.Filled.DirectionsRun)
    )

    val symptomCategories = mapOf(
        "General" to listOf("Fever", "Chills", "Fatigue", "Weakness", "Weight Loss"),
        "Respiratory" to listOf("Cough", "Shortness of Breath", "Wheezing", "Chest Tightness"),
        "Digestive" to listOf("Nausea", "Vomiting", "Stomach Pain", "Diarrhea", "Constipation"),
        "Neurological" to listOf("Headache", "Migraine", "Dizziness"),
        "Cardiovascular" to listOf("Chest Pain", "Palpitations")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Symptom Assessment") },
                navigationIcon = {
                    IconButton(onClick = { if (step > 1) step -= 1 else onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            LinearProgressIndicator(
                progress = { step / 3f },
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            )

            when (step) {
                1 -> {
                    Text("Where do you feel discomfort?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(bodyParts) { part ->
                            BodyPartCard(
                                part = part,
                                isSelected = selectedBodyPart == part.name,
                                onClick = { selectedBodyPart = part.name }
                            )
                        }
                    }
                    
                    Button(
                        onClick = { step = 2 },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        enabled = selectedBodyPart != null
                    ) {
                        Text("Next")
                    }
                }
                2 -> {
                    Text("Select your symptoms", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search symptoms...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        symptomCategories.forEach { (category, symptoms) ->
                            item {
                                Text(category, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 8.dp))
                            }
                            items(symptoms) { symptom ->
                                SymptomItem(
                                    name = symptom,
                                    isSelected = selectedSymptoms.contains(symptom),
                                    onToggle = {
                                        if (selectedSymptoms.contains(symptom)) selectedSymptoms.remove(symptom)
                                        else selectedSymptoms.add(symptom)
                                    }
                                )
                            }
                        }
                    }
                    
                    Button(
                        onClick = { step = 3 },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        enabled = selectedSymptoms.isNotEmpty()
                    ) {
                        Text("Next")
                    }
                }
                3 -> {
                    Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                        Text("Additional Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text("Symptom Severity: ${severity.toInt()}")
                        Slider(
                            value = severity,
                            onValueChange = { severity = it },
                            valueRange = 1f..10f,
                            steps = 8
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = { Text("How long have you had these?") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Any additional notes?") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                    
                    Button(
                        onClick = { onAnalyze(selectedSymptoms.toList()) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    ) {
                        Text("Analyze Symptoms")
                    }
                }
            }
        }
    }
}

data class BodyPart(val name: String, val icon: ImageVector)

@Composable
fun BodyPartCard(part: BodyPart, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.height(100.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(part.icon, contentDescription = null, tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
            Text(part.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@Composable
fun SymptomItem(name: String, isSelected: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = isSelected, onCheckedChange = { onToggle() })
        Text(name, modifier = Modifier.padding(start = 8.dp))
    }
}
