package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout

import com.fasterxml.jackson.databind.ObjectMapper
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.plan.Plan
import org.freekode.tp2intervals.domain.workout.Workout
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.freekode.tp2intervals.infrastructure.PlatformException
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.TrainingPeaksApiClient
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.configuration.TrainingPeaksConfigurationRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.library.TPWorkoutLibraryRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.plan.TPPlanRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.user.TrainingPeaksUserRepository
import org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure.StructureToTPConverter
import org.freekode.tp2intervals.infrastructure.utils.Date
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository


@CacheConfig(cacheNames = ["tpWorkoutsCache"])
@Repository
class TrainingPeaksWorkoutRepository(
    private val trainingPeaksApiClient: TrainingPeaksApiClient,
    private val tpToWorkoutConverter: TPToWorkoutConverter,
    private val tpPlanRepository: TPPlanRepository,
    private val trainingPeaksUserRepository: TrainingPeaksUserRepository,
    private val tpWorkoutLibraryRepository: TPWorkoutLibraryRepository,
    private val trainingPeaksConfigurationRepository: TrainingPeaksConfigurationRepository,
    private val objectMapper: ObjectMapper,
) : WorkoutRepository {
    override fun platform() = Platform.TRAINING_PEAKS

    override fun planWorkout(workout: Workout) {
        val structureStr = StructureToTPConverter.toStructureString(objectMapper, workout)
        val athleteId = trainingPeaksUserRepository.getUserId()
        val createRequest = CreateTPWorkoutDTO.planWorkout(
            athleteId,
            workout,
            structureStr
        )
        trainingPeaksApiClient.createAndPlanWorkout(athleteId, createRequest)
    }

    @Cacheable(key = "#plan.externalData.trainingPeaksId")
    override fun getWorkoutsFromLibrary(plan: Plan): List<Workout> {
        return if (plan.isPlan) {
            getWorkoutsFromTPPlan(plan)
        } else {
            getWorkoutsFromTPLibrary(plan)
        }
    }

    override fun saveWorkoutToLibrary(workout: Workout, plan: Plan) {
        throw PlatformException(Platform.TRAINING_PEAKS, "TP doesn't support workout copying")
    }

    override fun getPlannedWorkouts(startDate: LocalDate, endDate: LocalDate): List<Workout> {
        val userId = trainingPeaksUserRepository.getUserId()
        val tpWorkouts = trainingPeaksApiClient.getWorkouts(userId, startDate.toString(), endDate.toString())

        val noteEndDate = getNoteEndDateForFilter(startDate, endDate)
        val tpNotes = trainingPeaksApiClient.getNotes(userId, startDate.toString(), noteEndDate.toString())
        val workouts = tpWorkouts.map { tpToWorkoutConverter.toWorkout(it) }
        val notes = tpNotes.map { tpToWorkoutConverter.toWorkout(it) }
        return workouts + notes
    }

    override fun findWorkoutsFromLibraryByName(name: String): List<WorkoutDetails> {
        return tpWorkoutLibraryRepository.getLibraries()
            .flatMap { tpWorkoutLibraryRepository.getLibraryItems(it.externalData.trainingPeaksId!!) }
            .map { it.details }
            .filter { it.name.contains(name) }
    }

    private fun getWorkoutsFromTPPlan(plan: Plan): List<Workout> {
        val planId = plan.externalData.trainingPeaksId!!
        val planApplyDate = getPlanApplyDate()
        val response = tpPlanRepository.applyPlan(planId, planApplyDate)

        try {
            val planEndDate = LocalDateTime.parse(response.endDate).toLocalDate()

            val workoutDateShiftDays = Date.daysDiff(plan.startDate, planApplyDate)
            val workouts = getPlannedWorkouts(planApplyDate, planEndDate)
                .map { it.withDate(it.date!!.minusDays(workoutDateShiftDays.toLong())) }
            val tpPlan = tpPlanRepository.getPlan(planId)
            assert(tpPlan.workoutCount == workouts.size)
            return workouts
        } catch (e: Exception) {
            throw e
        } finally {
            tpPlanRepository.removeAppliedPlan(response.appliedPlanId)
        }
    }

    private fun getWorkoutsFromTPLibrary(library: Plan): List<Workout> {
        return tpWorkoutLibraryRepository.getLibraryItems(library.externalData.trainingPeaksId!!)
    }

    private fun getPlanApplyDate(): LocalDate = LocalDate.now()
        .plusDays(trainingPeaksConfigurationRepository.getConfiguration().planDaysShift)
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    private fun getNoteEndDateForFilter(startDate: LocalDate, endDate: LocalDate): LocalDate =
        if (startDate == endDate) endDate.plusDays(1) else endDate

}
