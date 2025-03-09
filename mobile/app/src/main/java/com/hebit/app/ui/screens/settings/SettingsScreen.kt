package com.hebit.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    // Mock settings state - would come from ViewModel in real app
    var darkMode by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var reminderTime by remember { mutableStateOf("08:00") }
    var dataSync by remember { mutableStateOf(true) }
    var biometricAuth by remember { mutableStateOf(false) }
    
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Profile section
            ProfileSection()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Appearance settings
            SettingsCategory(title = "Appearance") {
                SwitchSettingItem(
                    title = "Dark Mode",
                    description = "Use dark theme",
                    icon = Icons.Default.DarkMode,
                    checked = darkMode,
                    onCheckedChange = { darkMode = it }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Notification settings
            SettingsCategory(title = "Notifications") {
                SwitchSettingItem(
                    title = "Enable Notifications",
                    description = "Get reminders for tasks and habits",
                    icon = Icons.Default.Notifications,
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
                
                if (notificationsEnabled) {
                    TimePickerSettingItem(
                        title = "Daily Reminder",
                        description = "Set your preferred time for daily reminders",
                        icon = Icons.Default.Schedule,
                        time = reminderTime,
                        onTimeSelected = { reminderTime = it }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Data & Privacy settings
            SettingsCategory(title = "Data & Privacy") {
                SwitchSettingItem(
                    title = "Data Sync",
                    description = "Synchronize data across devices",
                    icon = Icons.Default.Sync,
                    checked = dataSync,
                    onCheckedChange = { dataSync = it }
                )
                
                SwitchSettingItem(
                    title = "Biometric Authentication",
                    description = "Secure app with fingerprint or face recognition",
                    icon = Icons.Default.Fingerprint,
                    checked = biometricAuth,
                    onCheckedChange = { biometricAuth = it }
                )
                
                ClickableSettingItem(
                    title = "Clear App Data",
                    description = "Delete all locally stored data",
                    icon = Icons.Default.DeleteForever,
                    iconTint = MaterialTheme.colorScheme.error,
                    onClick = { showClearDataDialog = true }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Account settings
            SettingsCategory(title = "Account") {
                ClickableSettingItem(
                    title = "Edit Profile",
                    description = "Change your name, email, and photo",
                    icon = Icons.Default.Person,
                    onClick = { /* Navigate to profile edit screen */ }
                )
                
                ClickableSettingItem(
                    title = "Change Password",
                    description = "Update your password",
                    icon = Icons.Default.Lock,
                    onClick = { /* Navigate to change password screen */ }
                )
                
                ClickableSettingItem(
                    title = "Logout",
                    description = "Sign out from this device",
                    icon = Icons.Default.Logout,
                    iconTint = MaterialTheme.colorScheme.error,
                    onClick = { showLogoutDialog = true }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // About section
            SettingsCategory(title = "About") {
                ClickableSettingItem(
                    title = "App Version",
                    description = "1.0.0 (Build 101)",
                    icon = Icons.Default.Info,
                    onClick = { /* Show version details */ }
                )
                
                ClickableSettingItem(
                    title = "Terms of Service",
                    description = "View app terms and conditions",
                    icon = Icons.Default.Description,
                    onClick = { /* Navigate to terms page */ }
                )
                
                ClickableSettingItem(
                    title = "Privacy Policy",
                    description = "How we handle your data",
                    icon = Icons.Default.Security,
                    onClick = { /* Navigate to privacy policy page */ }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        // Handle logout
                    }
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Clear data confirmation dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear App Data") },
            text = { Text("This will delete all your local data including tasks, habits, and goals. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDataDialog = false
                        // Handle data clearing
                    }
                ) {
                    Text("Clear Data", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfileSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile image placeholder
            Surface(
                modifier = Modifier.size(64.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "Mike Dowells",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "mikedowells400@gmail.com",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            IconButton(onClick = { /* Open edit profile screen */ }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SettingsCategory(
    title: String,
    content: @Composable () -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            content()
        }
    }
}

@Composable
fun SwitchSettingItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
    
    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
}

@Composable
fun ClickableSettingItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    
    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
}

@Composable
fun TimePickerSettingItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    time: String,
    onTimeSelected: (String) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showTimePicker = true }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = time,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
    
    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
    
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Set Reminder Time") },
            text = {
                // Simple time picker UI
                Column {
                    val timeOptions = listOf(
                        "06:00", "07:00", "08:00", "09:00", "10:00",
                        "12:00", "15:00", "18:00", "20:00", "22:00"
                    )
                    
                    timeOptions.forEach { timeOption ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onTimeSelected(timeOption)
                                    showTimePicker = false
                                }
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = timeOption == time,
                                onClick = {
                                    onTimeSelected(timeOption)
                                    showTimePicker = false
                                }
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Text(text = timeOption)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
