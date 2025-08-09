package io.yavero.pocketadhd.core.domain.repository

import io.yavero.pocketadhd.core.domain.model.MedicationIntake
import io.yavero.pocketadhd.core.domain.model.MedicationPlan
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

data class AdherenceStats(
    val totalIntakes: Int,
    val takenCount: Int,
    val adherencePercentage: Double
)

interface MedicationRepository {

    fun getAllMedicationPlans(): Flow<List<MedicationPlan>>
    fun getActiveMedicationPlans(): Flow<List<MedicationPlan>>
    fun getMedicationPlanById(id: String): Flow<MedicationPlan?>
    suspend fun insertMedicationPlan(plan: MedicationPlan)
    suspend fun updateMedicationPlan(plan: MedicationPlan)
    suspend fun deleteMedicationPlan(id: String)
    suspend fun toggleMedicationPlanActive(id: String)


    fun getAllMedicationIntakes(): Flow<List<MedicationIntake>>
    fun getMedicationIntakeById(id: String): Flow<MedicationIntake?>
    fun getIntakesByPlanId(planId: String): Flow<List<MedicationIntake>>
    fun getIntakesByDateRange(startDate: Instant, endDate: Instant): Flow<List<MedicationIntake>>
    fun getRecentIntakes(limit: Int): Flow<List<MedicationIntake>>
    suspend fun getAdherenceByPlanAndPeriod(planId: String, startDate: Instant, endDate: Instant): AdherenceStats?
    suspend fun insertMedicationIntake(intake: MedicationIntake)
    suspend fun updateMedicationIntake(intake: MedicationIntake)
    suspend fun deleteMedicationIntake(id: String)
}