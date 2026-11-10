package com.meditrack.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Records a medicine disposal event — when a user marks an expired or unwanted medicine
 * as disposed, capturing what was disposed, how much, and the method used.
 */
@Entity(tableName = "disposals")
data class DisposalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val medicineId: Long,
    val medicineName: String,
    val quantity: Int,            // how many units were disposed
    val unit: String,             // "tablets", "ml", etc.
    val disposalMethod: String,   // e.g. "Pharmacy Drop-Off", "At-Home Safe Disposal"
    val disposedDate: String,     // yyyy-MM-dd
    val timestamp: Long = System.currentTimeMillis()
)
