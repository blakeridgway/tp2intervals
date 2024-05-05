package org.freekode.tp2intervals.rest.configuration

import org.freekode.tp2intervals.app.confguration.ConfigurationService
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.freekode.tp2intervals.domain.config.LogLevelService
import org.freekode.tp2intervals.domain.config.PlatformInfo
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest
import org.freekode.tp2intervals.rest.ErrorResponseDTO
import org.springframework.boot.logging.LogLevel
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ConfigurationController(
    private val configurationService: ConfigurationService,
    private val logLevelService: LogLevelService
) {

    @GetMapping("/api/configuration")
    fun getConfigurations(): AppConfigurationDTO {
        val configurations = configurationService.getConfigurations()
        return AppConfigurationDTO(configurations.configMap)
    }

    @PutMapping("/api/configuration")
    fun updateConfiguration(@RequestBody requestDTO: UpdateConfigurationRequestDTO): ResponseEntity<ErrorResponseDTO> {
        val errors = configurationService.updateConfiguration(UpdateConfigurationRequest(requestDTO.config))
        if (errors.isNotEmpty()) {
            return ResponseEntity.badRequest().body(ErrorResponseDTO(errors.joinToString()))
        }
        return ResponseEntity.ok().build()
    }

    @GetMapping("/api/configuration/training-types")
    fun getAllTrainingTypes(): List<TrainingTypeDTO> {
        return TrainingType.entries.map { TrainingTypeDTO(it) }
    }

    @GetMapping("/api/configuration/{platform}")
    fun getConfigurations(@PathVariable platform: Platform): PlatformInfo {
        return configurationService.platformInfo(platform)
    }

    @PutMapping("/api/configuration/log-level/{logLevel}")
    fun setLogLevel(@PathVariable logLevel: LogLevel) {
        logLevelService.setLogLevel(logLevel)
    }
}
