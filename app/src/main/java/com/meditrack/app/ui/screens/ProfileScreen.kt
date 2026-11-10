package com.meditrack.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meditrack.app.ui.components.MediCard
import com.meditrack.app.ui.theme.*
import com.meditrack.app.viewmodel.SettingsViewModel

@Composable
fun ProfileScreen(viewModel: SettingsViewModel) {
    val displayName by viewModel.displayName.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val message by viewModel.message.collectAsState()
    val context = LocalContext.current

    var nameInput by remember(displayName) { mutableStateOf(displayName) }

    // Show toast on messages
    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Profile header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color(0xFFE0F2F1),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = if (displayName.isNotBlank()) displayName else "Welcome to MediTrack",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnWhite
                )
                Text(
                    text = "Manage your profile and preferences",
                    fontSize = 13.sp,
                    color = MediumGray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Display name section
        Text("Display Name", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MediumGray)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            label = { Text("Your name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { viewModel.updateDisplayName(nameInput.trim()) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
            Text("Save Name")
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Notification settings
        MediCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Notifications, contentDescription = null, tint = TealPrimary)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Medicine Reminders", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = OnWhite)
                    Text(
                        "Enable notifications for dose reminders",
                        fontSize = 13.sp,
                        color = MediumGray
                    )
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { viewModel.setNotificationsEnabled(context, it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = WhiteBackground,
                        checkedTrackColor = TealPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Expiry warning threshold
        val expiryWarningDays by viewModel.expiryWarningDays.collectAsState()
        MediCard {
            Text("Expiry Warning Threshold", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = OnWhite)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Alert me $expiryWarningDays days before a medicine expires",
                fontSize = 13.sp,
                color = MediumGray
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(7, 14, 30, 60).forEach { days ->
                    FilterChip(
                        selected = expiryWarningDays == days,
                        onClick = { viewModel.setExpiryWarningDays(days) },
                        label = { Text("${days}d") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // About section
        MediCard {
            Text("About MedCycle", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = OnWhite)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "MedCycle helps you track your household medicine inventory, monitor expiry dates, " +
                       "and find safe disposal guidance. All data is stored locally on your device.",
                fontSize = 14.sp,
                color = MediumGray
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = MediumGray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Version 2.0", fontSize = 13.sp, color = MediumGray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Note: Reminder delivery is best-effort. Android battery optimization " +
                       "may delay notifications on some devices.",
                fontSize = 12.sp,
                color = MediumGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "MedCycle does not provide medical advice. Always consult your healthcare provider.",
                fontSize = 12.sp,
                color = MediumGray
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
