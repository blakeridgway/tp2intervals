package org.freekode.tp2intervals.infrastructure.thirdparty.workout

import java.time.LocalDateTime

class ThirdPartyNoteDTO(
    var id: Long,
    var noteDate: LocalDateTime,
    var title: String,
    var description: String?,
)
