package org.freekode.tp2intervals.domain.workout

import org.freekode.tp2intervals.domain.TrainingType
import java.time.Duration
import java.time.LocalDate

data class Workout(
    val date: LocalDate,
    val type: TrainingType,
    val title: String,
    val description: String?,
    val duration: Duration?,
    val load: Double?,
    val steps: List<WorkoutStep>,
    val externalData: WorkoutExternalData,
)
