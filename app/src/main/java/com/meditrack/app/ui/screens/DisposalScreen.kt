package com.meditrack.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meditrack.app.data.MedicineEntity
import com.meditrack.app.ui.components.MediCard
import com.meditrack.app.ui.theme.*
import com.meditrack.app.viewmodel.DisposalViewModel

private val DISPOSAL_METHODS = listOf(
    "Pharmacy Drop-Off",
    "Council / NHS Collection Point",
    "At-Home Safe Disposal",
    "Sharps Bin (Injectables only)"
)

// Data for each medicine type tab
private data class TypeDisposalInfo(
    val emoji: String,
    val label: String,
    val doThis: List<String>,
    val neverDo: List<String>,
    val whyItMatters: String
)

private val TYPE_GUIDANCE = listOf(
    TypeDisposalInfo(
        emoji = "💊", label = "Tablets / Capsules",
        doThis = listOf(
            "Remove pills from blister packs or bottles",
            "Mix with coffee grounds, soil or cat litter — this deters pets and children",
            "Place the mixture in a sealed zip-lock bag or any container with a lid",
            "Conceal any personal details on the original packaging (scratch or tear off label)",
            "Drop the sealed bag into your household waste bin"
        ),
        neverDo = listOf(
            "Never flush tablets down the toilet",
            "Never put loose pills directly in the bin without concealing them",
            "Never give leftover medicines to friends or family without medical advice"
        ),
        whyItMatters = "Tablets that reach landfill or waterways release active pharmaceutical compounds that contaminate groundwater and harm aquatic life for years."
    ),
    TypeDisposalInfo(
        emoji = "🫧", label = "Liquids & Syrups",
        doThis = listOf(
            "Keep the liquid sealed in its original bottle",
            "Return it to any pharmacy — they accept liquid medicines for free",
            "If pharmacy return is not possible, mix with an absorbent material like sawdust or cat litter in a sealable container",
            "Place the sealed container in household waste — not recycling"
        ),
        neverDo = listOf(
            "Never pour medicines down the sink or toilet",
            "Liquid medicines dilute easily in water and pass straight through sewage treatment"
        ),
        whyItMatters = "Liquid medicines are the highest contamination risk — a single bottle of antibiotic syrup poured down a drain can disrupt the microbial balance of a local river."
    ),
    TypeDisposalInfo(
        emoji = "🌬️", label = "Inhalers",
        doThis = listOf(
            "Take the inhaler to any pharmacy — this is the only safe route",
            "Do not attempt to puncture or dismantle the canister",
            "Many pharmacies participate in inhaler recycling schemes (e.g. GSK Complete the Cycle)"
        ),
        neverDo = listOf(
            "Never put pressurised inhalers in household waste — they are fire and explosion hazards",
            "Never incinerate or crush the canister"
        ),
        whyItMatters = "Inhalers account for 3–4% of the NHS carbon footprint. Returning them enables safe propellant recovery and canister recycling."
    ),
    TypeDisposalInfo(
        emoji = "🩹", label = "Patches",
        doThis = listOf(
            "Fold the used or unused patch in half, sticky sides together — this locks in the drug",
            "Place folded patches in a sealed bag before binning",
            "Return large quantities to a pharmacy"
        ),
        neverDo = listOf(
            "Never flush patches — they retain up to 50% of the drug after use",
            "Never leave used patches where children or pets could find them"
        ),
        whyItMatters = "A single used fentanyl patch can still contain a lethal dose. Folding it traps the drug and prevents accidental contact."
    ),
    TypeDisposalInfo(
        emoji = "💉", label = "Injectables & Sharps",
        doThis = listOf(
            "Place used needles and syringes directly into an approved sharps bin immediately after use",
            "When the sharps bin is ¾ full, seal it and return it to your GP, pharmacy or council collection point",
            "Never attempt to re-cap needles by hand"
        ),
        neverDo = listOf(
            "Never put loose needles in household waste — they are a puncture and infection hazard",
            "Never overfill a sharps bin — loose contents can escape the seal"
        ),
        whyItMatters = "Needle-stick injuries carry bloodborne disease risk. Proper sharps disposal protects waste workers and prevents community harm."
    ),
    TypeDisposalInfo(
        emoji = "🧴", label = "Creams & Ointments",
        doThis = listOf(
            "Keep the cream or ointment in its original sealed tube or pot",
            "Place it in a sealable bag and put it in household waste",
            "Return hormone creams (e.g. HRT gels) to a pharmacy — they contain oestrogen which is an environmental hormone disruptor"
        ),
        neverDo = listOf(
            "Never rinse tubes down the sink",
            "Do not leave open containers where children or pets can access them"
        ),
        whyItMatters = "Hormone-based creams that enter the water supply cause endocrine disruption in fish and amphibians at extremely low concentrations."
    ),
    TypeDisposalInfo(
        emoji = "👁️", label = "Eye / Ear / Nose Drops",
        doThis = listOf(
            "Keep drops in their original sealed bottle",
            "Place in a sealable bag and put in household waste, or return to pharmacy",
            "Check the packaging — some eye drop containers are recyclable once empty"
        ),
        neverDo = listOf(
            "Never share eye or ear drops with another person — risk of cross-infection",
            "Never reuse opened drops beyond the discard date printed on the box (usually 28 days)"
        ),
        whyItMatters = "Eye drops often contain preservatives that are harmful to aquatic ecosystems. Keeping them out of drains matters."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisposalScreen(
    viewModel: DisposalViewModel,
    preselectedMedicine: MedicineEntity? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val result by viewModel.result.collectAsState()

    var quantityText by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf(DISPOSAL_METHODS[0]) }
    var methodDropdownExpanded by remember { mutableStateOf(false) }
    var showRecordDialog by remember { mutableStateOf(false) }
    var selectedTypeIndex by remember { mutableIntStateOf(0) }
    var quickReferenceExpanded by remember { mutableStateOf(false) }

    // Preselect the matching type tab if medicine is known
    LaunchedEffect(preselectedMedicine) {
        preselectedMedicine?.let { med ->
            val idx = TYPE_GUIDANCE.indexOfFirst {
                it.label.contains(med.medicineType, ignoreCase = true) ||
                med.medicineType.contains(it.emoji)
            }
            if (idx >= 0) selectedTypeIndex = idx
        }
    }

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

        // ── Hero header ─────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TealDark)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("♻️", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Safe Medicine Disposal",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (preselectedMedicine != null)
                                "Disposing: ${preselectedMedicine.name}  •  ${preselectedMedicine.medicineType}"
                            else
                                "Protect your family, community and environment",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.80f)
                        )
                    }
                }

                if (preselectedMedicine != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MediInfoChip("Medicine", preselectedMedicine.name)
                            MediInfoChip("Type", preselectedMedicine.medicineType)
                            MediInfoChip("Stock", if (preselectedMedicine.quantity != null)
                                "${preselectedMedicine.quantity} units" else "Unknown")
                        }
                    }
                }
            }
        }

        // ── Why disposal matters banner ─────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFFFF8E1)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = Color(0xFFE65100),
                    modifier = Modifier.size(20.dp).padding(top = 1.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Unused medicines flushed down drains or put in bins unsafely contaminate " +
                           "drinking water, harm wildlife, and can be accidentally consumed by children and pets. " +
                           "Correct disposal takes under 2 minutes.",
                    fontSize = 13.sp,
                    color = Color(0xFF4E342E),
                    lineHeight = 19.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {

            // ── Quick reference (collapsible) ───────────────────────────────────
            MediCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { quickReferenceExpanded = !quickReferenceExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "⚡ Quick Reference — Do's & Don'ts",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnWhite
                    )
                    Icon(
                        if (quickReferenceExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = TealPrimary
                    )
                }
                AnimatedVisibility(
                    visible = quickReferenceExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        DosDontsRow(isOk = true,  text = "Return to any pharmacy — always free, always accepted")
                        DosDontsRow(isOk = true,  text = "Mix tablets with coffee grounds/soil, seal, then bin")
                        DosDontsRow(isOk = true,  text = "Fold patches sticky-side in before disposal")
                        DosDontsRow(isOk = true,  text = "Use a sharps bin for all needles and syringes")
                        DosDontsRow(isOk = true,  text = "Scratch out your name on packaging before disposal")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = LightGray)
                        DosDontsRow(isOk = false, text = "NEVER flush any medicine down the toilet or sink")
                        DosDontsRow(isOk = false, text = "NEVER put loose tablets directly in the bin")
                        DosDontsRow(isOk = false, text = "NEVER put inhalers in household waste — fire risk")
                        DosDontsRow(isOk = false, text = "NEVER give leftover prescriptions to others")
                        DosDontsRow(isOk = false, text = "NEVER leave medicines where children or pets can reach")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Step-by-step general guide ──────────────────────────────────────
            Text(
                "General Disposal Steps",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnWhite
            )
            Spacer(modifier = Modifier.height(10.dp))

            StepCard(
                step = "1",
                color = TealPrimary,
                title = "Check if pharmacy return is possible",
                body = "Most pharmacies accept expired and unused medicines at no charge — no questions asked. " +
                       "This is the safest and most environmentally responsible option for all medicine types."
            )
            Spacer(modifier = Modifier.height(8.dp))
            StepCard(
                step = "2",
                color = TealPrimary,
                title = "If binning at home — make it unrecognisable",
                body = "Mix solid medicines (tablets, capsules) with something undesirable — coffee grounds, " +
                       "soil, or cat litter. This deters accidental ingestion by children, pets, or wildlife " +
                       "that may rummage through waste."
            )
            Spacer(modifier = Modifier.height(8.dp))
            StepCard(
                step = "3",
                color = TealPrimary,
                title = "Seal it completely",
                body = "Place the mixture in a sealed zip-lock bag, an old coffee tin, or any container that " +
                       "closes. Never leave the bag open or tied loosely."
            )
            Spacer(modifier = Modifier.height(8.dp))
            StepCard(
                step = "4",
                color = TealPrimary,
                title = "Protect your personal information",
                body = "Scratch out or tear off any labels with your name, address, or NHS number before " +
                       "placing packaging in the bin. Medicine labels carry personal health data."
            )
            Spacer(modifier = Modifier.height(8.dp))
            StepCard(
                step = "5",
                color = TealPrimary,
                title = "Household bin — not recycling",
                body = "Place the sealed bag in the general household waste bin, not the recycling bin. " +
                       "Recycling facilities are not equipped to handle pharmaceutical waste."
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Type-specific guidance tabs ─────────────────────────────────────
            Text(
                "Guidance by Medicine Type",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "Tap your medicine type for specific instructions",
                fontSize = 13.sp,
                color = MediumGray
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Emoji type selector grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TYPE_GUIDANCE.take(4).forEachIndexed { idx, info ->
                        TypeChip(
                            emoji = info.emoji,
                            label = info.label.split("/")[0].trim().split("&")[0].trim(),
                            selected = selectedTypeIndex == idx,
                            onClick = { selectedTypeIndex = idx },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TYPE_GUIDANCE.drop(4).forEachIndexed { idx, info ->
                        TypeChip(
                            emoji = info.emoji,
                            label = info.label.split("/")[0].trim().split("&")[0].trim(),
                            selected = selectedTypeIndex == idx + 4,
                            onClick = { selectedTypeIndex = idx + 4 },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Filler to keep last row aligned
                    if (TYPE_GUIDANCE.drop(4).size < 4) {
                        repeat(4 - TYPE_GUIDANCE.drop(4).size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Expanded type guidance card
            TYPE_GUIDANCE.getOrNull(selectedTypeIndex)?.let { info ->
                MediCard {
                    // Header
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(info.emoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            info.label,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Why it matters
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TealPrimary.copy(alpha = 0.08f)
                    ) {
                        Row(modifier = Modifier.padding(10.dp)) {
                            Text("ℹ️ ", fontSize = 14.sp)
                            Text(
                                info.whyItMatters,
                                fontSize = 13.sp,
                                color = TealDark,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Do this
                    Text(
                        "✅  What to do",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TakenGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    info.doThis.forEachIndexed { i, step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(TakenGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${i + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TakenGreen
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(step, fontSize = 13.sp, color = OnWhite, modifier = Modifier.weight(1f), lineHeight = 18.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Never do
                    Text(
                        "🚫  What NOT to do",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ErrorRed
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    info.neverDo.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("✗ ", fontSize = 13.sp, color = ErrorRed, fontWeight = FontWeight.Bold)
                            Text(item, fontSize = 13.sp, color = OnWhite, modifier = Modifier.weight(1f), lineHeight = 18.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Find a collection point ─────────────────────────────────────────
            Text(
                "Find a Collection Point Near You",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnWhite
            )
            Spacer(modifier = Modifier.height(10.dp))

            MediCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(TealPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.LocationOn, null, tint = TealPrimary, modifier = Modifier.size(26.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Pharmacy Drop-Off", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = OnWhite)
                        Text(
                            "Any UK pharmacy accepts medicines free of charge. No receipt or prescription needed.",
                            fontSize = 13.sp,
                            color = MediumGray,
                            lineHeight = 18.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        val uri = Uri.parse("https://www.nhs.uk/nhs-services/prescriptions-and-pharmacies/find-a-pharmacy/")
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Icon(Icons.Filled.LocationOn, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Find NHS Pharmacy Near Me")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        val query = Uri.encode("pharmacy medicine disposal near me")
                        val uri = Uri.parse("https://www.google.com/maps/search/$query")
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Open in Google Maps")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        val uri = Uri.parse("https://www.recyclenow.com/recycling-knowledge/how-to-recycle/medicines")
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Recycle Now — Medicine Guide")
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = LightGray)
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "💡  Tip: Keep a small bag in your medicine cabinet. Add expired medicines to it as you go, then take the whole bag to the pharmacy in one trip.",
                    fontSize = 13.sp,
                    color = MediumGray,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Record disposal form ────────────────────────────────────────────
            if (preselectedMedicine != null) {
                Text(
                    "Record This Disposal",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Log the disposal so your waste statistics stay accurate.",
                    fontSize = 13.sp,
                    color = MediumGray
                )
                Spacer(modifier = Modifier.height(10.dp))

                MediCard {
                    // Medicine summary row
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LightGray
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                preselectedMedicine.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OnWhite,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                preselectedMedicine.medicineType,
                                fontSize = 13.sp,
                                color = MediumGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it.filter { c -> c.isDigit() } },
                        label = { Text("Quantity Disposed") },
                        placeholder = { Text("e.g. 10") },
                        supportingText = { Text("How many units (tablets, ml, etc.) are you disposing of?") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ExposedDropdownMenuBox(
                        expanded = methodDropdownExpanded,
                        onExpandedChange = { methodDropdownExpanded = !methodDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedMethod,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Disposal Method") },
                            supportingText = { Text("How are you disposing of this medicine?") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodDropdownExpanded)
                            },
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
                                    onClick = {
                                        selectedMethod = method
                                        methodDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showRecordDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        enabled = quantityText.isNotBlank()
                    ) {
                        Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Confirm & Record Disposal",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                // Standalone guide — prompt to go to cabinet
                MediCard {
                    Text(
                        "Ready to dispose a medicine?",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnWhite
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Go to the Cabinet tab, tap the ✓ icon on any expired or expiring medicine, " +
                        "and you'll be brought here with that medicine pre-selected to log the disposal.",
                        fontSize = 13.sp,
                        color = MediumGray,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ── Confirm dialog ──────────────────────────────────────────────────────────
    if (showRecordDialog && preselectedMedicine != null) {
        AlertDialog(
            onDismissRequest = { showRecordDialog = false },
            icon = { Text("♻️", fontSize = 24.sp) },
            title = { Text("Confirm Disposal") },
            text = {
                Text(
                    "You are recording the disposal of $quantityText unit(s) of " +
                    "${preselectedMedicine.name} via $selectedMethod.\n\n" +
                    "This will be added to your waste statistics.",
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.recordDisposal(
                            medicineId = preselectedMedicine.id,
                            medicineName = preselectedMedicine.name,
                            quantity = quantityText.toIntOrNull() ?: 0,
                            unit = preselectedMedicine.medicineType,
                            method = selectedMethod
                        )
                        showRecordDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) { Text("Record It") }
            },
            dismissButton = {
                TextButton(onClick = { showRecordDialog = false }) { Text("Go Back") }
            }
        )
    }
}

// ── Small helpers ────────────────────────────────────────────────────────────────

@Composable
private fun MediInfoChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.65f))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

@Composable
private fun DosDontsRow(isOk: Boolean, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            if (isOk) "✓" else "✗",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isOk) TakenGreen else ErrorRed,
            modifier = Modifier.width(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontSize = 13.sp, color = OnWhite, modifier = Modifier.weight(1f), lineHeight = 18.sp)
    }
}

@Composable
private fun StepCard(step: String, color: Color, title: String, body: String) {
    MediCard {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(step, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnWhite)
                Spacer(modifier = Modifier.height(4.dp))
                Text(body, fontSize = 13.sp, color = MediumGray, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
private fun TypeChip(
    emoji: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (selected) TealPrimary else WhiteBackground,
        modifier = modifier
            .border(
                width = 1.5.dp,
                color = if (selected) TealPrimary else LightGray,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 20.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                label,
                fontSize = 10.sp,
                color = if (selected) Color.White else MediumGray,
                textAlign = TextAlign.Center,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 2,
                lineHeight = 13.sp
            )
        }
    }
}
