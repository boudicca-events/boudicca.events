package base.boudicca.springboot.common

import io.opentelemetry.api.OpenTelemetry
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
        val endpoint = env.getProperty<String>("boudicca.monitoring.tracing.endpoint")
        val serviceName = env.getProperty<String>("spring.application.name") ?: "unknown boudicca app"
        return OpenTelemetryUtils.createOpenTelemetry(endpoint, serviceName)
    }
}
