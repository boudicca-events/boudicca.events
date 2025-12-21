package base.boudicca.springboot.common

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.metrics.MeterProvider
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
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Supplier
import kotlin.io.encoding.Base64

@Suppress("MagicNumber")
object OpenTelemetryUtils {
    fun createOpenTelemetry(endpoint: String?, user: String?, password: String?, serviceName: String): OpenTelemetry {
        if (endpoint.isNullOrEmpty() || user.isNullOrEmpty() || password.isNullOrEmpty()) {
            return OpenTelemetry.noop()
        }

        val resource = createResource(serviceName)
        val authHeader = createAuthHeader(user, password)

        val meterProvider = createMeterProvider(resource, endpoint, authHeader)
        val otel =
            OpenTelemetrySdk
                .builder()
                .setMeterProvider(meterProvider)
                .setTracerProvider(createTracerProvider(resource, endpoint, authHeader, meterProvider))
                .setLoggerProvider(createLoggerProvider(resource, endpoint, authHeader, meterProvider))
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .build()

        OpenTelemetryAppender.install(otel)

        return otel
    }

    private fun createAuthHeader(user: String, password: String): String = "Basic " + Base64.encode(("$user:$password").encodeToByteArray())

    private fun createResource(serviceName: String): Resource = Resource
        .getDefault()
        .toBuilder()
        .put(
            ServiceAttributes.SERVICE_NAME,
            serviceName,
        ).build()

    private fun createTracerProvider(resource: Resource, endpoint: String, authHeader: String, meterProvider: SdkMeterProvider): SdkTracerProvider = SdkTracerProvider
        .builder()
        .setResource(resource)
        .addSpanProcessor(
            BatchSpanProcessor
                .builder(
                    OtlpHttpSpanExporter
                        .builder()
                        .setMeterProvider(meterProvider)
                        .setEndpoint(getCorrectEndpoint(endpoint, "traces"))
                        .setTimeout(Duration.ofSeconds(10))
                        .addHeader("Authorization", authHeader)
                        .build(),
                ).setMeterProvider(meterProvider)
                .setMaxQueueSize(2048 * 10)
                .setExporterTimeout(Duration.ofSeconds(30))
                .setScheduleDelay(Duration.ofSeconds(5))
                .build(),
        ).build()

    private fun createMeterProvider(resource: Resource, endpoint: String, authHeader: String): SdkMeterProvider {
        val ref = AtomicReference<MeterProvider>(null)
        val meterProvider =
            SdkMeterProvider
                .builder()
                .setResource(resource)
                .registerMetricReader(
                    PeriodicMetricReader
                        .builder(
                            OtlpHttpMetricExporter
                                .builder()
                                .setMeterProvider(Supplier(ref::get))
                                .setEndpoint(getCorrectEndpoint(endpoint, "metrics"))
                                .setTimeout(Duration.ofSeconds(10))
                                .addHeader("Authorization", authHeader)
                                .build(),
                        ).setInterval(Duration.ofSeconds(60))
                        .build(),
                ).build()
        ref.set(meterProvider)
        return meterProvider
    }

    private fun createLoggerProvider(resource: Resource, endpoint: String, authHeader: String, meterProvider: SdkMeterProvider): SdkLoggerProvider = SdkLoggerProvider
        .builder()
        .setResource(resource)
        .addLogRecordProcessor(
            BatchLogRecordProcessor
                .builder(
                    OtlpHttpLogRecordExporter
                        .builder()
                        .setMeterProvider(meterProvider)
                        .setEndpoint(getCorrectEndpoint(endpoint, "logs"))
                        .setTimeout(Duration.ofSeconds(10))
                        .addHeader("Authorization", authHeader)
                        .build(),
                ).setMeterProvider(meterProvider)
                .setMaxQueueSize(2048)
                .setExporterTimeout(Duration.ofSeconds(30))
                .setScheduleDelay(Duration.ofSeconds(1))
                .build(),
        ).setLogLimits {
            LogLimits
                .builder()
                .setMaxNumberOfAttributes(128)
                .setMaxAttributeValueLength(1024 * 100)
                .build()
        }.build()

    private fun getCorrectEndpoint(baseEndpoint: String, suffix: String): String {
        var result = baseEndpoint
        if (!result.endsWith("/")) {
            result += "/"
        }
        return result + suffix
    }
}
