package com.meditrack.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meditrack.app.data.MedicineEntity
import com.meditrack.app.ui.components.*
import com.meditrack.app.ui.theme.*
import com.meditrack.app.util.DateTimeUtils
import com.meditrack.app.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddMedicine: () -> Unit,
    onViewSchedule: () -> Unit,
    onMarkDose: (MedicineEntity, String, String) -> Unit,
    onLoadDemoData: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Greeting header
        Text(
            text = state.greeting + if (state.displayName.isNotBlank()) ", ${state.displayName}!" else "!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = OnWhite
        )
        Text(text = state.todayDate, fontSize = 14.sp, color = MediumGray)

        // Expiry alerts banner
        if (state.expiredCount > 0 || state.expiringSoonCount > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            ExpiryAlertBanner(
                expiredCount = state.expiredCount,
                expiringSoonCount = state.expiringSoonCount
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick stats card
        MediCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(value = state.takenCount.toString(), label = "Taken Today", color = TakenGreen)
                VerticalDivider()
                StatItem(value = state.remainingCount.toString(), label = "Remaining", color = TealPrimary)
                VerticalDivider()
                StatItem(value = state.medicines.size.toString(), label = "Medicines", color = GreenSecondary)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick actions
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onAddMedicine,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Medicine", fontSize = 13.sp)
            }
            OutlinedButton(
                onClick = onViewSchedule,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Schedule", fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Today's schedule
        Text(text = "Today's Schedule", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = OnWhite)
        Spacer(modifier = Modifier.height(12.dp))

        if (state.medicines.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.Medication,
                title = "No medicines yet",
                message = "Add your first medicine to start tracking your doses.\nTap 'Add Medicine' above."
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onLoadDemoData,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Load Sample Data") }
        } else {
            val todayDoses = state.todayDoses
            val scheduleItems = state.medicines.flatMap { med ->
                med.reminderTimes.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    .sorted().map { time ->
                        val dose = todayDoses.find { it.medicineId == med.id && it.reminderTime == time }
                        Triple(med, time, dose?.status)
                    }
            }

            scheduleItems.forEach { (medicine, time, status) ->
                ScheduleItemCard(
                    medicine = medicine,
                    time = time,
                    status = status,
                    onTaken = { onMarkDose(medicine, time, "TAKEN") },
                    onSkipped = { onMarkDose(medicine, time, "SKIPPED") }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (scheduleItems.isEmpty()) {
                Text(
                    text = "No reminders scheduled for today",
                    fontSize = 14.sp,
                    color = MediumGray,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ExpiryAlertBanner(expiredCount: Int, expiringSoonCount: Int) {
    val message = buildString {
        if (expiredCount > 0) append("⚠️ $expiredCount medicine(s) have expired. ")
        if (expiringSoonCount > 0) append("⏰ $expiringSoonCount expiring soon.")
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFF3E0)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(message.trim(), fontSize = 13.sp, color = Color(0xFF7B4F00), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 12.sp, color = MediumGray)
    }
}

@Composable
private fun VerticalDivider() {
    Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color(0xFFE0E0E0)))
}

@Composable
private fun ScheduleItemCard(
    medicine: MedicineEntity,
    time: String,
    status: String?,
    onTaken: () -> Unit,
    onSkipped: () -> Unit
) {
    val displayTime = DateTimeUtils.formatTime(time)
    val isDone = status != null

    MediCard {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFE0F2F1)) {
                Text(
                    text = displayTime,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TealPrimary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = medicine.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = OnWhite)
                Text(text = medicine.dosage, fontSize = 13.sp, color = MediumGray)
            }
            if (isDone) {
                DoseStatusChip(status!!)
            } else {
                Row {
                    TakenButton(onClick = onTaken)
                    Spacer(modifier = Modifier.width(6.dp))
                    SkippedButton(onClick = onSkipped)
                }
            }
        }
    }
}
