package com.meditrack.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meditrack.app.ui.components.MediCard
import com.meditrack.app.ui.theme.*
import com.meditrack.app.util.DateTimeUtils
import com.meditrack.app.viewmodel.StatsViewModel

@Composable
fun StatsScreen(viewModel: StatsViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Statistics",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = OnWhite
        )
        Text("Your medicine cabinet overview", fontSize = 13.sp, color = MediumGray)

        Spacer(modifier = Modifier.height(20.dp))

        // --- Expiry summary ---
        Text("Expiry Overview", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = OnWhite)
        Spacer(modifier = Modifier.height(10.dp))

        MediCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ExpiryStatItem(state.expiredCount.toString(), "Expired", ErrorRed)
                StatDivider()
                ExpiryStatItem(state.expiringSoonCount.toString(), "Expiring Soon", AmberWarning)
                StatDivider()
                ExpiryStatItem(state.inDateCount.toString(), "In Date", TakenGreen)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        MediCard {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ExpiryStatItem(state.totalMedicines.toString(), "Total Medicines", TealPrimary)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Disposal summary ---
        Text("Disposal Summary", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = OnWhite)
        Spacer(modifier = Modifier.height(10.dp))

        MediCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ExpiryStatItem(state.totalDisposals.toString(), "Events", TealPrimary)
                StatDivider()
                ExpiryStatItem(state.totalUnitsDisposed.toString(), "Units Disposed", GreenSecondary)
            }
        }

        // Disposal method breakdown
        if (state.disposalMethodBreakdown.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            MediCard {
                Text("By Method", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnWhite)
                Spacer(modifier = Modifier.height(8.dp))
                state.disposalMethodBreakdown.forEach { (method, count) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(method, fontSize = 13.sp, color = MediumGray, modifier = Modifier.weight(1f))
                        Text(
                            "$count event(s)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TealPrimary
                        )
                    }
                    if (method != state.disposalMethodBreakdown.keys.last()) {
                        HorizontalDivider(color = LightGray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Adherence ---
        Text("30-Day Adherence", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = OnWhite)
        Spacer(modifier = Modifier.height(10.dp))

        MediCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { state.adherencePercent / 100f },
                        modifier = Modifier.size(80.dp),
                        strokeWidth = 8.dp,
                        color = when {
                            state.adherencePercent >= 80 -> TakenGreen
                            state.adherencePercent >= 50 -> AmberWarning
                            else -> ErrorRed
                        },
                        trackColor = LightGray
                    )
                    Text(
                        "${state.adherencePercent}%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnWhite
                    )
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column {
                    Text("Doses taken in last 30 days", fontSize = 13.sp, color = MediumGray)
                    val grade = when {
                        state.adherencePercent >= 80 -> "Excellent 🌟"
                        state.adherencePercent >= 60 -> "Good 👍"
                        state.adherencePercent >= 40 -> "Needs improvement ⚠️"
                        else -> "Poor — please stay on track ❗"
                    }
                    Text(grade, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnWhite)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Recent disposals ---
        if (state.recentDisposals.isNotEmpty()) {
            Text("Recent Disposals", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = OnWhite)
            Spacer(modifier = Modifier.height(10.dp))
            state.recentDisposals.forEach { disposal ->
                MediCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(disposal.medicineName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = OnWhite)
                            Text(
                                "${disposal.quantity} ${disposal.unit}  •  ${disposal.disposalMethod}",
                                fontSize = 13.sp,
                                color = MediumGray
                            )
                        }
                        Text(
                            DateTimeUtils.formatDayMonth(disposal.disposedDate),
                            fontSize = 12.sp,
                            color = MediumGray
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // --- SDG note ---
        Spacer(modifier = Modifier.height(8.dp))
        MediCard {
            Text("🌍 SDG Impact", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TealPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "By tracking and safely disposing of medicines you contribute to:\n" +
                "• SDG 3 – Good Health and Well-being\n" +
                "• SDG 12 – Responsible Consumption and Production",
                fontSize = 13.sp,
                color = MediumGray
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ExpiryStatItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 12.sp, color = MediumGray)
    }
}

@Composable
private fun StatDivider() {
    Box(modifier = Modifier.width(1.dp).height(40.dp).background(LightGray))
}
