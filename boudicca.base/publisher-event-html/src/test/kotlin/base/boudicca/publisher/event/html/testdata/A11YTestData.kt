package base.boudicca.publisher.event.html.testdata

import base.boudicca.SemanticKeys
import base.boudicca.publisher.event.html.util.buildEventList
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.support.ParameterDeclarations
import java.util.stream.Stream

class A11YTestData : ArgumentsProvider {
    override fun provideArguments(parameterDeclarations: ParameterDeclarations, extensionContext: ExtensionContext): Stream<out Arguments> {
        return listOf(
            arguments(
                buildEventList(30),
                mapOf(
                    SemanticKeys.LOCATION_NAME to listOf("Location1", "Location2", "Location3"),
                    SemanticKeys.LOCATION_CITY to listOf("City1", "City2", "City3"),
                    SemanticKeys.CONCERT_BANDLIST to listOf("Band1", "Band2", "Band3"),
                    SemanticKeys.TAGS to emptyList(),
                    SemanticKeys.TYPE to emptyList(),
                    SemanticKeys.CONCERT_GENRE to emptyList()
                )
            )
        ).stream()
    }
}
