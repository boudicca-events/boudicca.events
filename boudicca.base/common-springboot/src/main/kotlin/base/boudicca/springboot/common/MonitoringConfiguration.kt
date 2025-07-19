package base.boudicca.springboot.common

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.semconv.ServiceAttributes
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import org.springframework.core.env.getProperty
import java.time.Duration

@AutoConfiguration
class MonitoringConfiguration {
    @Bean
    fun otel(
        env: Environment
    ): OpenTelemetry {
        val endpoint = env.getProperty<String>("boudicca.monitoring.tracing.endpoint")
        if (endpoint.isNullOrEmpty()) {
            return OpenTelemetry.noop()
        }
        return OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .setResource(
                        Resource.getDefault().toBuilder().put(
                            ServiceAttributes.SERVICE_NAME,
                            env.getProperty<String>("spring.application.name") ?: "unknown boudicca app"
                        ).build()
                    ).addSpanProcessor(
                        BatchSpanProcessor.builder(
                            OtlpHttpSpanExporter.builder()
                                .setEndpoint(endpoint)
//                            .addHeader("api-key", "value") //TODO add auth for exporter?
                                .setTimeout(Duration.ofSeconds(10))
                                .build()
                        )
                            .setMaxQueueSize(2048)
                            .setExporterTimeout(Duration.ofSeconds(30))
                            .setScheduleDelay(Duration.ofSeconds(5))
                            .build()
                    )
                    .build()
            )
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .build()
    }
}
