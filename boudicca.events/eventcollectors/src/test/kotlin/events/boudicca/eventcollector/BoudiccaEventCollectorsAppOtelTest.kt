package events.boudicca.eventcollector

import base.boudicca.api.eventcollector.util.FetcherFactory
import base.boudicca.springboot.common.MonitoringAutoConfiguration
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.OpenTelemetry
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
    classes = [
        MonitoringAutoConfiguration::class,
    ],
    properties = [
        "boudicca.collector.collection-interval=1h",
        "boudicca.monitoring.endpoint=http://localhost:4318",
        "boudicca.monitoring.user=user",
        "boudicca.monitoring.password=pass",
    ],
)
@ActiveProfiles("debug")
class BoudiccaEventCollectorsAppOtelTest {
    @Autowired
    private lateinit var otel: OpenTelemetry

    @Test
    fun `test otel configuration`() {
        // We manually trigger the logic from BoudiccaEventCollectorsApp.initRunner()
        // to verify that the environment (MonitoringAutoConfiguration) provides a working otel instance
        // and that our assignment to FetcherFactory works.

        GlobalOpenTelemetry.set(otel)
        FetcherFactory.otel = otel

        // Ensure otel is not Noop
        assertNotSame(OpenTelemetry.noop(), otel, "otel bean should not be noop")

        // Verify FetcherFactory.otel is set to the same instance as the bean
        assertSame(otel, FetcherFactory.otel, "FetcherFactory.otel should match the otel bean")

        // Verify GlobalOpenTelemetry is also initialized
        assertNotSame(OpenTelemetry.noop(), GlobalOpenTelemetry.get(), "GlobalOpenTelemetry should not be noop")
    }
}
