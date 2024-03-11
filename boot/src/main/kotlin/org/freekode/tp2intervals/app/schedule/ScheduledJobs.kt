package org.freekode.tp2intervals.app.schedule

import org.freekode.tp2intervals.app.workout.ScheduleWorkoutsRequest
import org.freekode.tp2intervals.app.workout.WorkoutService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ScheduledJobs(
    private val scheduleService: ScheduleService,
    private val workoutService: WorkoutService,
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @Scheduled(cron = "#{planWorkoutsCron}")
    fun planWorkouts() {
        val scheduleWorkoutsRequest = scheduleService.getScheduledRequest(ScheduleWorkoutsRequest::class.java) ?: return
        log.info("Start scheduled workouts planning")
        val response = workoutService.copyPlannedWorkouts(scheduleWorkoutsRequest)
        log.debug("Scheduled workouts planning, response: {}", response)
        log.info("End scheduled workouts planning")
    }
}
