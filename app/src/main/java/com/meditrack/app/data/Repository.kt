package com.meditrack.app.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

/**
 * Central repository that abstracts DAO access for ViewModels.
 */
class MediTrackRepository(
    private val medicineDao: MedicineDao,
    private val doseDao: DoseDao,
    private val disposalDao: DisposalDao,
    private val settingsStore: SettingsStore
) {
    // --- Medicines ---
    fun getAllMedicines(): Flow<List<MedicineEntity>> = medicineDao.getAllMedicines()

    suspend fun getMedicineById(id: Long): MedicineEntity? = medicineDao.getById(id)

    suspend fun insertMedicine(medicine: MedicineEntity): Long = medicineDao.insert(medicine)

    suspend fun updateMedicine(medicine: MedicineEntity) = medicineDao.update(medicine)

    suspend fun deleteMedicine(medicine: MedicineEntity) {
        // Also delete all dose history and disposal records for this medicine
        doseDao.deleteDosesForMedicine(medicine.id)
        disposalDao.deleteDisposalsForMedicine(medicine.id)
        medicineDao.delete(medicine)
    }

    suspend fun getAllMedicinesList(): List<MedicineEntity> = medicineDao.getAllMedicinesList()

    // --- Doses ---
    fun getDosesForDate(date: String): Flow<List<DoseEntity>> = doseDao.getDosesForDate(date)

    fun getAllDoses(): Flow<List<DoseEntity>> = doseDao.getAllDoses()

    fun getDosesForRange(startDate: String, endDate: String): Flow<List<DoseEntity>> =
        doseDao.getDosesForRange(startDate, endDate)

    suspend fun recordDose(dose: DoseEntity): Long = doseDao.insert(dose)

    suspend fun getDose(medicineId: Long, time: String, date: String): DoseEntity? =
        doseDao.getDose(medicineId, time, date)

    fun getAdherenceStats(startDate: String, endDate: String): Flow<Pair<Int, Int>> =
        kotlinx.coroutines.flow.combine(
            doseDao.getTakenCount(startDate, endDate),
            doseDao.getTotalCount(startDate, endDate)
        ) { taken, total -> taken to total }

    // --- Disposals ---
    fun getAllDisposals(): Flow<List<DisposalEntity>> = disposalDao.getAllDisposals()

    fun getDisposalsForMedicine(medicineId: Long): Flow<List<DisposalEntity>> =
        disposalDao.getDisposalsForMedicine(medicineId)

    suspend fun recordDisposal(disposal: DisposalEntity): Long = disposalDao.insert(disposal)

    fun getTotalDisposalCount(): Flow<Int> = disposalDao.getTotalDisposalCount()

    fun getTotalQuantityDisposed(): Flow<Int?> = disposalDao.getTotalQuantityDisposed()

    fun getDisposalsForRange(startDate: String, endDate: String): Flow<List<DisposalEntity>> =
        disposalDao.getDisposalsForRange(startDate, endDate)

    // --- Settings ---
    val displayName: Flow<String> = settingsStore.displayName
    val notificationsEnabled: Flow<Boolean> = settingsStore.notificationsEnabled
    val onboardingDone: Flow<Boolean> = settingsStore.onboardingDone
    val expiryWarningDays: Flow<Int> = settingsStore.expiryWarningDays

    suspend fun setDisplayName(name: String) = settingsStore.setDisplayName(name)
    suspend fun setNotificationsEnabled(enabled: Boolean) = settingsStore.setNotificationsEnabled(enabled)
    suspend fun setOnboardingDone(done: Boolean) = settingsStore.setOnboardingDone(done)
    suspend fun setExpiryWarningDays(days: Int) = settingsStore.setExpiryWarningDays(days)

    companion object {
        @Volatile
        private var INSTANCE: MediTrackRepository? = null

        fun getInstance(context: Context): MediTrackRepository {
            return INSTANCE ?: synchronized(this) {
                val db = MediTrackDatabase.getInstance(context)
                val store = SettingsStore(context)
                val repo = MediTrackRepository(db.medicineDao(), db.doseDao(), db.disposalDao(), store)
                INSTANCE = repo
                repo
            }
        }
    }
}
