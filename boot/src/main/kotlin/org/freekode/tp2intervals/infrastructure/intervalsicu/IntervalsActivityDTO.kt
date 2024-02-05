package org.freekode.tp2intervals.infrastructure.intervalsicu

import java.time.LocalDateTime
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.infrastructure.intervalsicu.workout.IntervalsEventTypeMapper

class IntervalsActivityDTO(
    val id: String,
    val name: String,
    val description: String?,
    val start_date_local: LocalDateTime,
    val type: String?,
    val moving_time: Long,
    val icu_training_load: Long?,
) {
    fun mapType(): TrainingType = type?.let { IntervalsEventTypeMapper.getByIntervalsType(it) } ?: TrainingType.UNKNOWN
}
