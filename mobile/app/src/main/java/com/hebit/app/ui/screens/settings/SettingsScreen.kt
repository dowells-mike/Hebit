package com.hebit.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val settingsState by viewModel.settingsState.collectAsState()
    val storageState by viewModel.storageState.collectAsState()
    
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showClearOfflineDataDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
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
        ) {
            // Settings content depends on state
            when (settingsState) {
                is SettingsUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is SettingsUiState.Success -> {
                    val preferences = (settingsState as SettingsUiState.Success).preferences
                    SettingsContent(
                        preferences = preferences,
                        storageMetrics = storageState,
                        onUpdatePreference = { key, value -> viewModel.updatePreference(key, value) },
                        onSyncNow = { viewModel.forceSyncNow() },
                        onClearCache = { showClearCacheDialog = true },
                        onClearOfflineData = { showClearOfflineDataDialog = true },
                        onLogout = { showLogoutDialog = true },
                        formatLastSyncedTime = { timestamp -> viewModel.formatLastSyncedTime(timestamp) }
                    )
                }
                
                is SettingsUiState.Syncing -> {
                    val preferences = (settingsState as SettingsUiState.Syncing).preferences
                    SettingsContent(
                        preferences = preferences,
                        storageMetrics = storageState,
                        onUpdatePreference = { _, _ -> /* Disable during sync */ },
                        onSyncNow = { /* Disable during sync */ },
                        onClearCache = { /* Disable during sync */ },
                        onClearOfflineData = { /* Disable during sync */ },
                        onLogout = { /* Disable during sync */ },
                        formatLastSyncedTime = { "Syncing now..." },
                        isSyncing = true
                    )
                }
                
                is SettingsUiState.ClearingCache -> {
                    val preferences = (settingsState as SettingsUiState.ClearingCache).preferences
                    SettingsContent(
                        preferences = preferences,
                        storageMetrics = storageState,
                        onUpdatePreference = { _, _ -> /* Disable during operation */ },
                        onSyncNow = { /* Disable during operation */ },
                        onClearCache = { /* Disable during operation */ },
                        onClearOfflineData = { /* Disable during operation */ },
                        onLogout = { /* Disable during operation */ },
                        formatLastSyncedTime = { timestamp -> viewModel.formatLastSyncedTime(timestamp) },
                        isClearingCache = true
                    )
                }
                
                is SettingsUiState.ClearingOfflineData -> {
                    val preferences = (settingsState as SettingsUiState.ClearingOfflineData).preferences
                    SettingsContent(
                        preferences = preferences,
                        storageMetrics = storageState,
                        onUpdatePreference = { _, _ -> /* Disable during operation */ },
                        onSyncNow = { /* Disable during operation */ },
                        onClearCache = { /* Disable during operation */ },
                        onClearOfflineData = { /* Disable during operation */ },
                        onLogout = { /* Disable during operation */ },
                        formatLastSyncedTime = { timestamp -> viewModel.formatLastSyncedTime(timestamp) },
                        isClearingOfflineData = true
                    )
                }
                
                is SettingsUiState.Error -> {
                    val errorMessage = (settingsState as SettingsUiState.Error).message
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Error loading settings",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = { /* Reload settings */ },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Try Again")
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log out") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        // Implement logout functionality
                    }
                ) {
                    Text("Log out")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Clear cache confirmation dialog
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("Clear Cache") },
            text = { Text("Are you sure you want to clear the app cache? This will free up ${storageState.cacheSizeMB} MB.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearCacheDialog = false
                        viewModel.clearCache()
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearCacheDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Clear offline data confirmation dialog
    if (showClearOfflineDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearOfflineDataDialog = false },
            title = { Text("Clear Offline Data") },
            text = { Text("Are you sure you want to clear all offline data? You will need to reconnect to download your data. This will free up ${storageState.offlineDataSizeGB} GB.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearOfflineDataDialog = false
                        viewModel.clearOfflineData()
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearOfflineDataDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsContent(
    preferences: com.hebit.app.domain.model.UserPreferences,
    storageMetrics: StorageMetrics,
    onUpdatePreference: (String, Any) -> Unit,
    onSyncNow: () -> Unit,
    onClearCache: () -> Unit,
    onClearOfflineData: () -> Unit,
    onLogout: () -> Unit,
    formatLastSyncedTime: (Long) -> String,
    isSyncing: Boolean = false,
    isClearingCache: Boolean = false,
    isClearingOfflineData: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // General Section
        SettingsSection(
            title = "General",
            icon = Icons.Default.Settings,
            content = {
                // Dark Mode
                SettingsSwitchItem(
                    title = "Dark Mode",
                    subtitle = "Enable dark theme",
                    icon = Icons.Default.DarkMode,
                    checked = preferences.darkMode,
                    onCheckedChange = { onUpdatePreference("darkMode", it) }
                )

                HorizontalDivider()
                
                // Notifications
                SettingsSwitchItem(
                    title = "Notifications",
                    subtitle = "Enable push notifications",
                    icon = Icons.Default.Notifications,
                    checked = preferences.notificationsEnabled,
                    onCheckedChange = { onUpdatePreference("notificationsEnabled", it) }
                )

                HorizontalDivider()
                
                // Biometric Auth
                SettingsSwitchItem(
                    title = "Biometric Authentication",
                    subtitle = "Use fingerprint or face ID to login",
                    icon = Icons.Default.Fingerprint,
                    checked = preferences.biometricAuth,
                    onCheckedChange = { onUpdatePreference("biometricAuth", it) }
                )
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Sync Section
        SettingsSection(
            title = "Sync",
            icon = Icons.Default.Sync,
            content = {
                // Data Sync
                SettingsSwitchItem(
                    title = "Data Sync",
                    subtitle = "Enable data synchronization",
                    icon = Icons.Default.CloudSync,
                    checked = preferences.dataSync,
                    onCheckedChange = { onUpdatePreference("dataSync", it) }
                )

                HorizontalDivider()
                
                // Auto Sync
                SettingsSwitchItem(
                    title = "Auto Sync",
                    subtitle = "Automatically sync data",
                    icon = Icons.Default.AutoAwesome,
                    checked = preferences.syncSettings.autoSync,
                    onCheckedChange = { onUpdatePreference("autoSync", it) }
                )

                HorizontalDivider()
                
                // WiFi Only
                SettingsSwitchItem(
                    title = "WiFi Only",
                    subtitle = "Sync only when connected to WiFi",
                    icon = Icons.Default.Wifi,
                    checked = preferences.syncSettings.wifiOnly,
                    onCheckedChange = { onUpdatePreference("wifiOnly", it) }
                )

                HorizontalDivider()
                
                // Last Synced
                SettingsInfoItem(
                    title = "Last Synced",
                    subtitle = formatLastSyncedTime(preferences.syncSettings.lastSynced),
                    icon = Icons.Default.Update
                )
                
                // Sync Now button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onSyncNow,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSyncing
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (isSyncing) "Syncing..." else "Sync Now")
                    }
                }
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Storage Section
        SettingsSection(
            title = "Storage",
            icon = Icons.Default.Storage,
            content = {
                // Storage Usage
                SettingsInfoItem(
                    title = "Storage Usage",
                subtitle = "${storageMetrics.usedStorage} GB of ${storageMetrics.totalStorage} GB",
                    icon = Icons.Default.SdCard,
                    trailing = {
                        Text(
                            text = "${storageMetrics.usedPercentage}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                )
                
                LinearProgressIndicator(
            progress = { storageMetrics.usedStorage / storageMetrics.totalStorage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                )

                HorizontalDivider()
                
                // Clear Cache
                SettingsActionItem(
                    title = "Clear Cache",
                    subtitle = "${storageMetrics.cacheSizeMB} MB",
                    icon = Icons.Default.ClearAll,
                    onClick = onClearCache,
                    isLoading = isClearingCache
                )

                HorizontalDivider()
                
                // Clear Offline Data
                SettingsActionItem(
                    title = "Clear Offline Data",
                    subtitle = "${storageMetrics.offlineDataSizeGB} GB",
                    icon = Icons.Default.DeleteSweep,
                    onClick = onClearOfflineData,
                    isLoading = isClearingOfflineData
                )

                HorizontalDivider()
                
                // App Version
                SettingsInfoItem(
                    title = "App Version",
                    subtitle = storageMetrics.appVersion,
                    icon = Icons.Default.Info
                )
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Account Section
        SettingsSection(
            title = "Account",
            icon = Icons.Default.AccountCircle,
            content = {
                // Logout
                SettingsActionItem(
                    title = "Log Out",
                    subtitle = "Sign out of your account",
                    icon = Icons.AutoMirrored.Filled.Logout,
                    onClick = onLogout,
                    iconTint = MaterialTheme.colorScheme.error,
                    titleColor = MaterialTheme.colorScheme.error
                )
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Content
            content()
        }
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsInfoItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        trailing?.invoke()
    }
}

@Composable
fun SettingsActionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onPrimaryContainer,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    isLoading: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading) { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
