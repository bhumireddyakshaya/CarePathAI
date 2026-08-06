package com.example.carepathai.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.carepathai.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogout: () -> Unit = {}
) {
    val profile by viewModel.userProfile.collectAsState()
    val updateStatus by viewModel.updateStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current
    var isEditing by remember { mutableStateOf(false) }
    
    // Editable states - ensure they react to profile changes
    var fullName by remember(profile.fullName) { mutableStateOf(profile.fullName) }
    var mobileNumber by remember(profile.mobileNumber) { mutableStateOf(profile.mobileNumber) }
    var age by remember(profile.age) { mutableStateOf(profile.age.toString()) }
    var bloodGroup by remember(profile.bloodGroup) { mutableStateOf(profile.bloodGroup) }
    var height by remember(profile.height) { mutableStateOf(profile.height.toString()) }
    var weight by remember(profile.weight) { mutableStateOf(profile.weight.toString()) }
    var fitnessGoals by remember(profile.fitnessGoals) { mutableStateOf(profile.fitnessGoals) }

    LaunchedEffect(updateStatus) {
        updateStatus?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearStatus()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                actions = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(end = 16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = { 
                            if (isEditing) {
                                viewModel.updateProfile(profile.copy(
                                    fullName = fullName,
                                    mobileNumber = mobileNumber,
                                    age = age.toIntOrNull() ?: profile.age,
                                    bloodGroup = bloodGroup,
                                    height = height.toFloatOrNull() ?: profile.height,
                                    weight = weight.toFloatOrNull() ?: profile.weight,
                                    fitnessGoals = fitnessGoals
                                ))
                            }
                            isEditing = !isEditing 
                        }) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Save else Icons.Default.Edit,
                                contentDescription = "Toggle Edit",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Profile Image
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isEditing) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    singleLine = true
                )
            } else {
                Text(
                    text = profile.fullName.ifEmpty { "User" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = profile.email,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Information sections
            ProfileCard(title = "Personal Details") {
                if (isEditing) {
                    EditRow(value = mobileNumber, onValueChange = { mobileNumber = it }, label = "Mobile", icon = Icons.Default.Phone)
                    EditRow(value = age, onValueChange = { age = it }, label = "Age", icon = Icons.Default.Cake)
                } else {
                    DisplayRow(label = "Mobile", value = mobileNumber, icon = Icons.Default.Phone)
                    DisplayRow(label = "Age", value = age, icon = Icons.Default.Cake)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ProfileCard(title = "Health Stats") {
                if (isEditing) {
                    EditRow(value = bloodGroup, onValueChange = { bloodGroup = it }, label = "Blood Group", icon = Icons.Default.Bloodtype)
                    EditRow(value = height, onValueChange = { height = it }, label = "Height (cm)", icon = Icons.Default.Height)
                    EditRow(value = weight, onValueChange = { weight = it }, label = "Weight (kg)", icon = Icons.Default.MonitorWeight)
                    EditRow(value = fitnessGoals, onValueChange = { fitnessGoals = it }, label = "Goals", icon = Icons.Default.Flag)
                } else {
                    DisplayRow(label = "Blood Group", value = bloodGroup, icon = Icons.Default.Bloodtype)
                    DisplayRow(label = "Height", value = "$height cm", icon = Icons.Default.Height)
                    DisplayRow(label = "Weight", value = "$weight kg", icon = Icons.Default.MonitorWeight)
                    DisplayRow(label = "Goals", value = fitnessGoals, icon = Icons.Default.Flag)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                enabled = !isLoading
            ) {
                Text("Sign Out")
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ProfileCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun DisplayRow(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value.ifEmpty { "Not set" }, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun EditRow(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        leadingIcon = { Icon(icon, null, modifier = Modifier.size(20.dp)) },
        singleLine = true
    )
}
