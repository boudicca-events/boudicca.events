package base.boudicca.springboot.common

import io.micrometer.core.instrument.MeterRegistry
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.micrometer.v1_5.OpenTelemetryMeterRegistry
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableConfigurationProperties(MonitoringConfigurationProperties::class)
class MonitoringAutoConfiguration(
    val monitoringConfig: MonitoringConfigurationProperties,
    @param:Value("spring.application.name") val serviceName: String = "unknown boudicca app",
) {
    @Bean
    fun otel(): OpenTelemetry = OpenTelemetryUtils.createOpenTelemetry(monitoringConfig.endpoint, monitoringConfig.user, monitoringConfig.password, serviceName)

    @Bean
    fun meterRegistry(otel: OpenTelemetry): MeterRegistry = OpenTelemetryMeterRegistry.builder(otel).build()
}
