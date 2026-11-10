package com.meditrack.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meditrack.app.data.MedicineEntity
import com.meditrack.app.ui.components.EmptyState
import com.meditrack.app.ui.components.MediCard
import com.meditrack.app.ui.theme.*
import com.meditrack.app.util.DateTimeUtils
import com.meditrack.app.viewmodel.ExpiryStatus
import com.meditrack.app.viewmodel.MedicineViewModel
import com.meditrack.app.viewmodel.MedicineWithExpiry

@Composable
fun InventoryScreen(
    viewModel: MedicineViewModel,
    onAddMedicine: () -> Unit,
    onEditMedicine: (Long) -> Unit,
    onDispose: (MedicineEntity) -> Unit
) {
    val medicinesWithExpiry by viewModel.medicinesWithExpiry.collectAsState()
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf<MedicineEntity?>(null) }

    val expired = medicinesWithExpiry.filter { it.expiryStatus == ExpiryStatus.EXPIRED }
    val expiringSoon = medicinesWithExpiry.filter { it.expiryStatus == ExpiryStatus.EXPIRING_SOON }
    val inDate = medicinesWithExpiry.filter { it.expiryStatus == ExpiryStatus.IN_DATE }
    val noExpiry = medicinesWithExpiry.filter { it.expiryStatus == ExpiryStatus.NO_EXPIRY }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMedicine,
                containerColor = TealPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Outlined.Inventory2, contentDescription = "Add Medicine")
            }
        }
    ) { padding ->
        if (medicinesWithExpiry.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().background(OffWhite).padding(padding),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    icon = Icons.Outlined.Inventory2,
                    title = "Cabinet is empty",
                    message = "Add medicines to track their expiry dates and stock levels."
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().background(OffWhite).padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "Medicine Cabinet",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnWhite,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // --- Expired section ---
            if (expired.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "🔴 Expired (${expired.size})",
                        color = ErrorRed
                    )
                }
                items(expired, key = { it.medicine.id }) { item ->
                    InventoryCard(
                        item = item,
                        onEdit = { onEditMedicine(item.medicine.id) },
                        onDelete = { showDeleteDialog = item.medicine },
                        onDispose = { onDispose(item.medicine) }
                    )
                }
            }

            // --- Expiring soon section ---
            if (expiringSoon.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "🟡 Expiring Soon (${expiringSoon.size})",
                        color = AmberWarning
                    )
                }
                items(expiringSoon, key = { it.medicine.id }) { item ->
                    InventoryCard(
                        item = item,
                        onEdit = { onEditMedicine(item.medicine.id) },
                        onDelete = { showDeleteDialog = item.medicine },
                        onDispose = { onDispose(item.medicine) }
                    )
                }
            }

            // --- In date section ---
            if (inDate.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "🟢 In Date (${inDate.size})",
                        color = TakenGreen
                    )
                }
                items(inDate, key = { it.medicine.id }) { item ->
                    InventoryCard(
                        item = item,
                        onEdit = { onEditMedicine(item.medicine.id) },
                        onDelete = { showDeleteDialog = item.medicine },
                        onDispose = null
                    )
                }
            }

            // --- No expiry set ---
            if (noExpiry.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "⚪ No Expiry Set (${noExpiry.size})",
                        color = MediumGray
                    )
                }
                items(noExpiry, key = { it.medicine.id }) { item ->
                    InventoryCard(
                        item = item,
                        onEdit = { onEditMedicine(item.medicine.id) },
                        onDelete = { showDeleteDialog = item.medicine },
                        onDispose = null
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    showDeleteDialog?.let { medicine ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete ${medicine.name}?") },
            text = { Text("This will remove the medicine and all its dose history. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteMedicine(context, medicine)
                    showDeleteDialog = null
                }) { Text("Delete", color = ErrorRed) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String, color: Color) {
    Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun InventoryCard(
    item: MedicineWithExpiry,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDispose: (() -> Unit)?
) {
    val med = item.medicine
    val expiryBadgeColor = when (item.expiryStatus) {
        ExpiryStatus.EXPIRED -> ErrorRed
        ExpiryStatus.EXPIRING_SOON -> AmberWarning
        ExpiryStatus.IN_DATE -> TakenGreen
        ExpiryStatus.NO_EXPIRY -> MediumGray
    }
    val expiryLabel = when (item.expiryStatus) {
        ExpiryStatus.EXPIRED -> "Expired"
        ExpiryStatus.EXPIRING_SOON -> "Expires in ${item.daysUntilExpiry}d"
        ExpiryStatus.IN_DATE -> "In Date"
        ExpiryStatus.NO_EXPIRY -> "No Expiry"
    }

    MediCard {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = med.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnWhite
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Expiry status chip
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = expiryBadgeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = expiryLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = expiryBadgeColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text("${med.dosage}  •  ${med.medicineType}", fontSize = 13.sp, color = MediumGray)
                if (med.expiryDate != null) {
                    Text(
                        "Expires: ${DateTimeUtils.formatDayMonth(med.expiryDate)}  ${
                            med.expiryDate.take(4)
                        }",
                        fontSize = 12.sp,
                        color = expiryBadgeColor
                    )
                }
                if (med.quantity != null) {
                    Text("Stock: ${med.quantity} units", fontSize = 12.sp, color = MediumGray)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = TealPrimary, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(20.dp))
                }
                if (onDispose != null) {
                    IconButton(onClick = onDispose, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = "Dispose",
                            tint = AmberWarning,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
