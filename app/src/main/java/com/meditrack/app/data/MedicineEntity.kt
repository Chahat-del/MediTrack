package com.meditrack.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a medicine the user is tracking.
 * Each medicine can have multiple daily reminder times stored as a comma-separated string (HH:mm).
 *
 * v2 additions:
 *  - expiryDate  : physical expiry printed on the package (yyyy-MM-dd), distinct from endDate
 *  - quantity    : number of units/pills currently in stock (null = not tracked)
 *  - medicineType: free-form category used for disposal instructions (e.g. "Tablet", "Liquid")
 */
@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val dosage: String,           // e.g. "500mg" or "1 tablet"
    val instructions: String,    // e.g. "After meals"
    val startDate: String,       // ISO format: yyyy-MM-dd
    val endDate: String?,        // null = ongoing (treatment end, NOT expiry)
    val reminderTimes: String,   // comma-separated "08:00,14:00,20:00"
    val expiryDate: String? = null,   // physical package expiry yyyy-MM-dd
    val quantity: Int? = null,        // units in stock; null means not tracked
    val medicineType: String = "Tablet", // used for disposal guidance
    val createdAt: Long = System.currentTimeMillis()
)
