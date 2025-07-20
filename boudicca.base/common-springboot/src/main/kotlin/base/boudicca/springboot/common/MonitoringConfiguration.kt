package base.boudicca.springboot.common

import io.micrometer.core.instrument.MeterRegistry
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.micrometer.v1_5.OpenTelemetryMeterRegistry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import org.springframework.core.env.getProperty

@AutoConfiguration
class MonitoringConfiguration {
    @Bean
    fun otel(
        env: Environment
    ): OpenTelemetry {
        val endpoint = env.getProperty<String>("boudicca.monitoring.endpoint")
        val user = env.getProperty<String>("boudicca.monitoring.user")
        val password = env.getProperty<String>("boudicca.monitoring.password")
        val serviceName = env.getProperty<String>("spring.application.name") ?: "unknown boudicca app"
        return OpenTelemetryUtils.createOpenTelemetry(endpoint, user, password, serviceName)
    }

    @Bean
    fun meterRegistry(otel: OpenTelemetry): MeterRegistry {
        return OpenTelemetryMeterRegistry.builder(otel).build()
    }
}
