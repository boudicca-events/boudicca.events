package base.boudicca.springboot.common

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.logs.LogLimits
import io.opentelemetry.sdk.logs.SdkLoggerProvider
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.semconv.ServiceAttributes
import java.time.Duration
import kotlin.io.encoding.Base64


@Suppress("MagicNumber")
object OpenTelemetryUtils {
    fun createOpenTelemetry(endpoint: String?, user: String?, password: String?, serviceName: String): OpenTelemetry {
        if (endpoint.isNullOrEmpty() || user.isNullOrEmpty() || password.isNullOrEmpty()) {
            return OpenTelemetry.noop()
        }

        val resource = createResource(serviceName)
        val authHeader = createAuthHeader(user, password)

        val otel = OpenTelemetrySdk.builder()
            .setTracerProvider(createTracerProvider(resource, endpoint, authHeader))
            .setMeterProvider(createMeterProvider(resource, endpoint, authHeader))
            .setLoggerProvider(createLoggerProvider(resource, endpoint, authHeader))
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .build()

        OpenTelemetryAppender.install(otel)

        return otel
    }

    private fun createAuthHeader(user: String, password: String): String {
        return "Basic " + Base64.Default.encode(("$user:$password").encodeToByteArray())
    }

    private fun createResource(serviceName: String): Resource {
        return Resource.getDefault().toBuilder().put(
            ServiceAttributes.SERVICE_NAME, serviceName
        ).build()
    }

    private fun createTracerProvider(
        resource: Resource, endpoint: String, authHeader: String
    ): SdkTracerProvider {
        return SdkTracerProvider.builder().setResource(resource).addSpanProcessor(
            BatchSpanProcessor.builder(
                OtlpHttpSpanExporter.builder()
                    .setEndpoint(getCorrectEndpoint(endpoint, "traces"))
                    .setTimeout(Duration.ofSeconds(10))
                    .addHeader("Authorization", authHeader)
                    .build()
            )
                .setMaxQueueSize(2048)
                .setExporterTimeout(Duration.ofSeconds(30))
                .setScheduleDelay(Duration.ofSeconds(5))
                .build()
        ).build()
    }

    private fun createMeterProvider(
        resource: Resource, endpoint: String, authHeader: String
    ): SdkMeterProvider {
        return SdkMeterProvider.builder().setResource(resource).registerMetricReader(
            PeriodicMetricReader.builder(
                OtlpHttpMetricExporter.builder()
                    .setEndpoint(getCorrectEndpoint(endpoint, "metrics"))
                    .setTimeout(Duration.ofSeconds(10))
                    .addHeader("Authorization", authHeader)
                    .build()
            ).setInterval(Duration.ofSeconds(60)).build()
        ).build()
    }

    private fun createLoggerProvider(
        resource: Resource, endpoint: String, authHeader: String
    ): SdkLoggerProvider {
        return SdkLoggerProvider.builder().setResource(resource).addLogRecordProcessor(
            BatchLogRecordProcessor.builder(
                OtlpHttpLogRecordExporter.builder()
                    .setEndpoint(getCorrectEndpoint(endpoint, "logs"))
                    .setTimeout(Duration.ofSeconds(10))
                    .addHeader("Authorization", authHeader)
                    .build()
            )
                .setMaxQueueSize(2048)
                .setExporterTimeout(Duration.ofSeconds(30))
                .setScheduleDelay(Duration.ofSeconds(1))
                .build()
        ).setLogLimits {
            LogLimits.builder().setMaxNumberOfAttributes(128).setMaxAttributeValueLength(1024).build()
        }.build()
    }

    private fun getCorrectEndpoint(baseEndpoint: String, suffix: String): String {
        var result = baseEndpoint
        if (!result.endsWith("/")) {
            result += "/"
        }
        return result + suffix
    }
}
