package com.meditrack.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meditrack.app.data.MedicineEntity
import com.meditrack.app.ui.components.MediCard
import com.meditrack.app.ui.theme.*
import com.meditrack.app.viewmodel.DisposalViewModel

private val DISPOSAL_METHODS = listOf(
    "Pharmacy Drop-Off",
    "Council Collection Point",
    "At-Home Safe Disposal",
    "Sharps Bin (Injectables)"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisposalScreen(
    viewModel: DisposalViewModel,
    // Pre-selected medicine passed from Inventory, or null for standalone browse
    preselectedMedicine: MedicineEntity? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val result by viewModel.result.collectAsState()

    var quantityText by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf(DISPOSAL_METHODS[0]) }
    var methodDropdownExpanded by remember { mutableStateOf(false) }
    var showRecordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(result) {
        result?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearResult()
            if (it.startsWith("Disposal recorded")) onBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TealPrimary)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Column {
                Text(
                    text = "Safe Disposal",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (preselectedMedicine != null)
                        "Disposing: ${preselectedMedicine.name}"
                    else
                        "Dispose of medicines safely",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- At-home disposal guide ---
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                "How to Dispose Safely at Home",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnWhite
            )
            Spacer(modifier = Modifier.height(12.dp))

            DisposalStepCard(
                step = "1",
                title = "Do NOT flush medicines",
                description = "Flushing medicines contaminates water supplies and harms aquatic life. Never pour liquids down the drain unless the label specifically says it's safe."
            )
            Spacer(modifier = Modifier.height(8.dp))
            DisposalStepCard(
                step = "2",
                title = "Mix with undesirable material",
                description = "For tablets and capsules: mix with coffee grounds, dirt, or cat litter. This makes them less appealing to children, pets, and wildlife."
            )
            Spacer(modifier = Modifier.height(8.dp))
            DisposalStepCard(
                step = "3",
                title = "Seal in a bag",
                description = "Place the mixture into a sealed plastic bag or container. Scratch out or cover any personal information on the original packaging."
            )
            Spacer(modifier = Modifier.height(8.dp))
            DisposalStepCard(
                step = "4",
                title = "Put in household bin",
                description = "Place the sealed bag in your household waste bin — not recycling. For large quantities, prefer a pharmacy drop-off point."
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Type-specific guidance
            MediCard {
                Text("Type-Specific Guidance", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = OnWhite)
                Spacer(modifier = Modifier.height(10.dp))
                TypeGuidanceRow("💊 Tablets / Capsules", "Mix with grounds/dirt, bag and bin.")
                TypeGuidanceRow("🫧 Liquids / Syrups", "Return to pharmacy. Do not pour down drain.")
                TypeGuidanceRow("🌬️ Inhalers", "Take to pharmacy — pressurised canisters cannot go in household waste.")
                TypeGuidanceRow("🩹 Patches", "Fold sticky sides together before disposal; still contain active drug.")
                TypeGuidanceRow("💉 Injectables / Sharps", "Use a sharps bin. Never put loose needles in household waste.")
                TypeGuidanceRow("🧴 Creams / Ointments", "Squeeze into a sealed bag and bin with household waste.")
                TypeGuidanceRow("👁️ Drops (eye/ear/nose)", "Cap tightly and place in household bin or return to pharmacy.")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Find a collection point
            MediCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Find a Collection Point", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = OnWhite)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Most pharmacies accept unused or expired medicines for free. " +
                    "Use the links below to find your nearest drop-off point.",
                    fontSize = 13.sp,
                    color = MediumGray
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val uri = Uri.parse("https://www.nhs.uk/nhs-services/prescriptions-and-pharmacies/find-a-pharmacy/")
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text("Find NHS Pharmacy Near Me")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        val query = Uri.encode("pharmacy near me medicine disposal")
                        val uri = Uri.parse("https://www.google.com/maps/search/$query")
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Search on Google Maps")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Record disposal (only shown if a medicine is preselected)
            if (preselectedMedicine != null) {
                MediCard {
                    Text("Record This Disposal", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = OnWhite)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Log how many units you disposed of for your waste statistics.", fontSize = 13.sp, color = MediumGray)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it.filter { c -> c.isDigit() } },
                        label = { Text("Quantity Disposed") },
                        placeholder = { Text("e.g. 10") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = methodDropdownExpanded,
                        onExpandedChange = { methodDropdownExpanded = !methodDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedMethod,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Disposal Method") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodDropdownExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = methodDropdownExpanded,
                            onDismissRequest = { methodDropdownExpanded = false }
                        ) {
                            DISPOSAL_METHODS.forEach { method ->
                                DropdownMenuItem(
                                    text = { Text(method) },
                                    onClick = { selectedMethod = method; methodDropdownExpanded = false }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showRecordDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        enabled = quantityText.isNotBlank()
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Record Disposal")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Confirm disposal dialog
    if (showRecordDialog && preselectedMedicine != null) {
        AlertDialog(
            onDismissRequest = { showRecordDialog = false },
            title = { Text("Confirm Disposal") },
            text = {
                Text(
                    "Record disposal of $quantityText unit(s) of ${preselectedMedicine.name} " +
                    "via $selectedMethod?"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.recordDisposal(
                        medicineId = preselectedMedicine.id,
                        medicineName = preselectedMedicine.name,
                        quantity = quantityText.toIntOrNull() ?: 0,
                        unit = preselectedMedicine.medicineType,
                        method = selectedMethod
                    )
                    showRecordDialog = false
                }) { Text("Confirm", color = TealPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showRecordDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DisposalStepCard(step: String, title: String, description: String) {
    MediCard {
        Row(verticalAlignment = Alignment.Top) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = TealPrimary,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(step, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnWhite)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, fontSize = 13.sp, color = MediumGray)
            }
        }
    }
}

@Composable
private fun TypeGuidanceRow(type: String, guidance: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(type, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = OnWhite, modifier = Modifier.width(160.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(guidance, fontSize = 13.sp, color = MediumGray, modifier = Modifier.weight(1f))
    }
}
