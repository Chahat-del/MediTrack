package com.meditrack.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v2: added expiryDate, quantity, medicineType columns to medicines table;
 *     added disposals table.
 */
@Database(
    entities = [MedicineEntity::class, DoseEntity::class, DisposalEntity::class],
    version = 2,
    exportSchema = false
)
abstract class MediTrackDatabase : RoomDatabase() {
    abstract fun medicineDao(): MedicineDao
    abstract fun doseDao(): DoseDao
    abstract fun disposalDao(): DisposalDao

    companion object {
        @Volatile
        private var INSTANCE: MediTrackDatabase? = null

        /** Migrate from v1 → v2: add new columns to medicines, create disposals table. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns to medicines (nullable / with defaults)
                db.execSQL("ALTER TABLE medicines ADD COLUMN expiryDate TEXT")
                db.execSQL("ALTER TABLE medicines ADD COLUMN quantity INTEGER")
                db.execSQL("ALTER TABLE medicines ADD COLUMN medicineType TEXT NOT NULL DEFAULT 'Tablet'")
                // Create the new disposals table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS disposals (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        medicineId INTEGER NOT NULL,
                        medicineName TEXT NOT NULL,
                        quantity INTEGER NOT NULL,
                        unit TEXT NOT NULL,
                        disposalMethod TEXT NOT NULL,
                        disposedDate TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): MediTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MediTrackDatabase::class.java,
                    "meditrack.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
