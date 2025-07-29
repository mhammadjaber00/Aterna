package io.yavero.pocketadhd.core.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import io.yavero.pocketadhd.core.data.database.PocketAdhdDatabase
import io.yavero.pocketadhd.core.domain.model.MedicationIntake
import io.yavero.pocketadhd.core.domain.model.MedicationPlan
import io.yavero.pocketadhd.core.domain.repository.AdherenceStats
import io.yavero.pocketadhd.core.domain.repository.MedicationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MedicationRepositoryImpl(
    private val database: PocketAdhdDatabase
) : MedicationRepository {

    private val medicationQueries = database.medicationQueries
    private val json = Json { ignoreUnknownKeys = true }

    // Medication Plans
    override fun getAllMedicationPlans(): Flow<List<MedicationPlan>> {
        return medicationQueries.selectAllMedicationPlans()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { mapPlanEntityToDomain(it) }
            }
    }

    override fun getActiveMedicationPlans(): Flow<List<MedicationPlan>> {
        return medicationQueries.selectActiveMedicationPlans()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { mapPlanEntityToDomain(it) }
            }
    }

    override fun getMedicationPlanById(id: String): Flow<MedicationPlan?> {
        return medicationQueries.selectMedicationPlanById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { entity ->
                entity?.let { mapPlanEntityToDomain(it) }
            }
    }

    override suspend fun insertMedicationPlan(plan: MedicationPlan) {
        medicationQueries.insertMedicationPlan(
            id = plan.id,
            name = plan.name,
            dose = plan.dose,
            times = json.encodeToString(plan.times.map { it.toString() }),
            daysOfWeek = json.encodeToString(plan.daysOfWeek),
            isActive = if (plan.isActive) 1L else 0L
        )
    }

    override suspend fun updateMedicationPlan(plan: MedicationPlan) {
        medicationQueries.updateMedicationPlan(
            name = plan.name,
            dose = plan.dose,
            times = json.encodeToString(plan.times.map { it.toString() }),
            daysOfWeek = json.encodeToString(plan.daysOfWeek),
            isActive = if (plan.isActive) 1L else 0L,
            id = plan.id
        )
    }

    override suspend fun deleteMedicationPlan(id: String) {
        database.transaction {
            // Delete all intakes for this plan first
            medicationQueries.selectIntakesByPlanId(id).executeAsList().forEach { intake ->
                medicationQueries.deleteMedicationIntake(intake.id)
            }
            medicationQueries.deleteMedicationPlan(id)
        }
    }

    override suspend fun toggleMedicationPlanActive(id: String) {
        val plan = medicationQueries.selectMedicationPlanById(id).executeAsOneOrNull()
        plan?.let {
            medicationQueries.updateMedicationPlan(
                name = it.name,
                dose = it.dose,
                times = it.times,
                daysOfWeek = it.daysOfWeek,
                isActive = if (it.isActive == 1L) 0L else 1L,
                id = it.id
            )
        }
    }

    // Medication Intakes
    override fun getAllMedicationIntakes(): Flow<List<MedicationIntake>> {
        return medicationQueries.selectAllMedicationIntakes()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { mapIntakeEntityToDomain(it) }
            }
    }

    override fun getMedicationIntakeById(id: String): Flow<MedicationIntake?> {
        return medicationQueries.selectMedicationIntakeById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { entity ->
                entity?.let { mapIntakeEntityToDomain(it) }
            }
    }

    override fun getIntakesByPlanId(planId: String): Flow<List<MedicationIntake>> {
        return medicationQueries.selectIntakesByPlanId(planId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { mapIntakeEntityToDomain(it) }
            }
    }

    override fun getIntakesByDateRange(startDate: Instant, endDate: Instant): Flow<List<MedicationIntake>> {
        return medicationQueries.selectIntakesByDateRange(
            startDate.toEpochMilliseconds(),
            endDate.toEpochMilliseconds()
        )
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { mapIntakeEntityToDomain(it) }
            }
    }

    override fun getRecentIntakes(limit: Int): Flow<List<MedicationIntake>> {
        return medicationQueries.selectRecentIntakes(limit.toLong())
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { mapIntakeEntityToDomain(it) }
            }
    }

    override suspend fun getAdherenceByPlanAndPeriod(planId: String, startDate: Instant, endDate: Instant): AdherenceStats? {
        val result = medicationQueries.selectAdherenceByPlanAndPeriod(
            planId = planId,
            timestamp = startDate.toEpochMilliseconds(),
            timestamp_ = endDate.toEpochMilliseconds()
        ).executeAsOneOrNull()

        return result?.let {
            AdherenceStats(
                totalIntakes = it.totalIntakes.toInt(),
                takenCount = it.takenCount?.toInt() ?: 0,
                adherencePercentage = it.adherencePercentage ?: 0.0
            )
        }
    }

    override suspend fun insertMedicationIntake(intake: MedicationIntake) {
        medicationQueries.insertMedicationIntake(
            id = intake.id,
            planId = intake.planId,
            timestamp = intake.timestamp.toEpochMilliseconds(),
            taken = if (intake.taken) 1L else 0L,
            sideEffectsNotes = intake.sideEffectsNotes
        )
    }

    override suspend fun updateMedicationIntake(intake: MedicationIntake) {
        medicationQueries.updateMedicationIntake(
            taken = if (intake.taken) 1L else 0L,
            sideEffectsNotes = intake.sideEffectsNotes,
            id = intake.id
        )
    }

    override suspend fun deleteMedicationIntake(id: String) {
        medicationQueries.deleteMedicationIntake(id)
    }

    private fun mapPlanEntityToDomain(entity: io.yavero.pocketadhd.core.data.database.MedicationPlanEntity): MedicationPlan {
        return MedicationPlan(
            id = entity.id,
            name = entity.name,
            dose = entity.dose,
            times = try {
                json.decodeFromString<List<String>>(entity.times).map { LocalTime.parse(it) }
            } catch (e: Exception) {
                emptyList()
            },
            daysOfWeek = try {
                json.decodeFromString<List<Int>>(entity.daysOfWeek)
            } catch (e: Exception) {
                emptyList()
            },
            isActive = entity.isActive == 1L
        )
    }

    private fun mapIntakeEntityToDomain(entity: io.yavero.pocketadhd.core.data.database.MedicationIntakeEntity): MedicationIntake {
        return MedicationIntake(
            id = entity.id,
            planId = entity.planId,
            timestamp = Instant.fromEpochMilliseconds(entity.timestamp),
            taken = entity.taken == 1L,
            sideEffectsNotes = entity.sideEffectsNotes
        )
    }
}