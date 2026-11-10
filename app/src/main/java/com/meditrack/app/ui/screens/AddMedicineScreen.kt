package com.meditrack.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meditrack.app.ui.components.DatePickerDialog
import com.meditrack.app.ui.components.TimePickerDialog
import com.meditrack.app.ui.theme.*
import com.meditrack.app.util.DateTimeUtils
import com.meditrack.app.viewmodel.MedicineViewModel
import java.util.Calendar

private val MEDICINE_TYPES = listOf("Tablet", "Capsule", "Liquid", "Inhaler", "Patch", "Injection", "Cream/Ointment", "Drops", "Other")

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineScreen(
    viewModel: MedicineViewModel,
    onDone: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val saveResult by viewModel.saveResult.collectAsState()

    // Core form state
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(DateTimeUtils.todayIso()) }
    var hasEndDate by remember { mutableStateOf(false) }
    var endDate by remember { mutableStateOf(DateTimeUtils.todayIso()) }
    var reminderTimes by remember { mutableStateOf(listOf("08:00")) }

    // New fields
    var hasExpiryDate by remember { mutableStateOf(false) }
    var expiryDate by remember { mutableStateOf(DateTimeUtils.getNDaysFromNow(365)) }
    var quantityText by remember { mutableStateOf("") }
    var medicineType by remember { mutableStateOf("Tablet") }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    // Picker visibility
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showExpiryDatePicker by remember { mutableStateOf(false) }
    var timePickerIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(saveResult) {
        saveResult?.let {
            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            if (it.success) {
                viewModel.clearSaveResult()
                onDone()
            } else {
                viewModel.clearSaveResult()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Medicine") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OffWhite)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Medicine Name *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Dosage
            OutlinedTextField(
                value = dosage,
                onValueChange = { dosage = it },
                label = { Text("Dosage *") },
                placeholder = { Text("e.g. 500mg, 1 tablet") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Instructions
            OutlinedTextField(
                value = instructions,
                onValueChange = { instructions = it },
                label = { Text("Instructions") },
                placeholder = { Text("e.g. After meals") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Medicine type dropdown
            Text("Medicine Type", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MediumGray)
            Spacer(modifier = Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = typeDropdownExpanded,
                onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = medicineType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = typeDropdownExpanded,
                    onDismissRequest = { typeDropdownExpanded = false }
                ) {
                    MEDICINE_TYPES.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                medicineType = type
                                typeDropdownExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Quantity in stock
            OutlinedTextField(
                value = quantityText,
                onValueChange = { quantityText = it.filter { c -> c.isDigit() } },
                label = { Text("Quantity in Stock") },
                placeholder = { Text("e.g. 30") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Start date
            Text("Start Date", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MediumGray)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(
                onClick = { showStartDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) { Text(DateTimeUtils.formatDisplayDate(startDate)) }

            Spacer(modifier = Modifier.height(12.dp))

            // End date toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = hasEndDate, onCheckedChange = { hasEndDate = it })
                Text("Set end date (treatment)", fontSize = 14.sp)
            }
            if (hasEndDate) {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = { showEndDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) { Text(DateTimeUtils.formatDisplayDate(endDate)) }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Expiry date toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = hasExpiryDate, onCheckedChange = { hasExpiryDate = it })
                Text("Set package expiry date", fontSize = 14.sp)
            }
            if (hasExpiryDate) {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = { showExpiryDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Expires: ${DateTimeUtils.formatDisplayDate(expiryDate)}")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reminder times
            Text("Reminder Times *", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MediumGray)
            Spacer(modifier = Modifier.height(8.dp))

            reminderTimes.forEachIndexed { index, time ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { timePickerIndex = index },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(DateTimeUtils.formatTime(time))
                    }
                    if (reminderTimes.size > 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = {
                            reminderTimes = reminderTimes.toMutableList().also { it.removeAt(index) }
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove time", tint = ErrorRed)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedButton(
                onClick = { reminderTimes = reminderTimes + "12:00" },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Another Time")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.addMedicine(
                        context = context,
                        name = name,
                        dosage = dosage,
                        instructions = instructions,
                        startDate = startDate,
                        endDate = if (hasEndDate) endDate else null,
                        reminderTimes = reminderTimes,
                        expiryDate = if (hasExpiryDate) expiryDate else null,
                        quantity = quantityText.toIntOrNull(),
                        medicineType = medicineType
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text("Save Medicine", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Date pickers
    if (showStartDatePicker) {
        val cal = Calendar.getInstance().apply { time = DateTimeUtils.parseDate(startDate) }
        DatePickerDialog(
            initialYear = cal.get(Calendar.YEAR),
            initialMonth = cal.get(Calendar.MONTH),
            initialDay = cal.get(Calendar.DAY_OF_MONTH),
            onDateSet = { y, m, d ->
                val c = Calendar.getInstance().apply { set(y, m, d) }
                startDate = DateTimeUtils.formatDate(c.time)
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }
    if (showEndDatePicker) {
        val cal = Calendar.getInstance().apply { time = DateTimeUtils.parseDate(endDate) }
        DatePickerDialog(
            initialYear = cal.get(Calendar.YEAR),
            initialMonth = cal.get(Calendar.MONTH),
            initialDay = cal.get(Calendar.DAY_OF_MONTH),
            onDateSet = { y, m, d ->
                val c = Calendar.getInstance().apply { set(y, m, d) }
                endDate = DateTimeUtils.formatDate(c.time)
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
    if (showExpiryDatePicker) {
        val cal = Calendar.getInstance().apply { time = DateTimeUtils.parseDate(expiryDate) }
        DatePickerDialog(
            initialYear = cal.get(Calendar.YEAR),
            initialMonth = cal.get(Calendar.MONTH),
            initialDay = cal.get(Calendar.DAY_OF_MONTH),
            onDateSet = { y, m, d ->
                val c = Calendar.getInstance().apply { set(y, m, d) }
                expiryDate = DateTimeUtils.formatDate(c.time)
                showExpiryDatePicker = false
            },
            onDismiss = { showExpiryDatePicker = false }
        )
    }
    if (timePickerIndex >= 0 && timePickerIndex < reminderTimes.size) {
        val parts = reminderTimes[timePickerIndex].split(":")
        TimePickerDialog(
            initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 8,
            initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0,
            is24Hour = true,
            onTimeSet = { hour, minute ->
                reminderTimes = reminderTimes.toMutableList().also {
                    it[timePickerIndex] = "%02d:%02d".format(hour, minute)
                }
                timePickerIndex = -1
            },
            onDismiss = { timePickerIndex = -1 }
        )
    }
}
