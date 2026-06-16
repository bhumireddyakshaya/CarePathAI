package com.example.carepathai.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.carepathai.data.local.entity.Medicine
import com.example.carepathai.ui.viewmodel.MedicineReminderViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineReminderScreen(
    viewModel: MedicineReminderViewModel = hiltViewModel(),
    onBack: (() -> Unit)? = null
) {
    val medicines by viewModel.allMedicines.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (onBack != null) {
                TopAppBar(
                    title = { Text("Medicine Reminders") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Medicine")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (onBack == null) {
                Text(
                    text = "Medicine Reminders",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (medicines.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No medicines added yet.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(medicines) { medicine ->
                        MedicineCard(
                            medicine = medicine,
                            onDelete = { viewModel.deleteMedicine(medicine) },
                            onToggleTaken = { viewModel.updateMedicine(medicine.copy(isTaken = !medicine.isTaken)) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddMedicineDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, dosage, frequency, morning, afternoon, night ->
                viewModel.addMedicine(
                    Medicine(
                        name = name,
                        dosage = dosage,
                        frequency = frequency,
                        startDate = System.currentTimeMillis(),
                        endDate = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000, // Default 1 week
                        morning = morning,
                        afternoon = afternoon,
                        night = night
                    )
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
fun MedicineCard(
    medicine: Medicine,
    onDelete: () -> Unit,
    onToggleTaken: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Medication,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (medicine.isTaken) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medicine.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (medicine.isTaken) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                )
                Text(text = "${medicine.dosage} • ${medicine.frequency}", style = MaterialTheme.typography.bodyMedium)
                
                val schedule = mutableListOf<String>()
                if (medicine.morning) schedule.add("Morning")
                if (medicine.afternoon) schedule.add("Afternoon")
                if (medicine.night) schedule.add("Night")
                
                Text(text = schedule.joinToString(", "), style = MaterialTheme.typography.labelSmall)
            }
            
            IconButton(onClick = onToggleTaken) {
                Icon(
                    if (medicine.isTaken) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Toggle Taken",
                    tint = if (medicine.isTaken) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddMedicineDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Boolean, Boolean, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("Daily") }
    var morning by remember { mutableStateOf(false) }
    var afternoon by remember { mutableStateOf(false) }
    var night by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Medicine") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Medicine Name") })
                OutlinedTextField(value = dosage, onValueChange = { dosage = it }, label = { Text("Dosage (e.g. 500mg)") })
                
                Text("Schedule", style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = morning, onCheckedChange = { morning = it })
                    Text("Morning")
                    Spacer(modifier = Modifier.width(8.dp))
                    Checkbox(checked = afternoon, onCheckedChange = { afternoon = it })
                    Text("Afternoon")
                    Spacer(modifier = Modifier.width(8.dp))
                    Checkbox(checked = night, onCheckedChange = { night = it })
                    Text("Night")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, dosage, frequency, morning, afternoon, night) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
