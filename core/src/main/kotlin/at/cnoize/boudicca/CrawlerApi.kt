package at.cnoize.boudicca

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition
import org.eclipse.microprofile.openapi.annotations.info.Contact
import org.eclipse.microprofile.openapi.annotations.info.Info
import org.eclipse.microprofile.openapi.annotations.info.License
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import javax.ws.rs.core.Application

@OpenAPIDefinition(
    tags = [
        Tag(
            name = "Crawler API",
            description = "Provide event data to the system."
        )
    ],
    info = Info(
        title = "Swagger with Quarkus",
        version = "0.0.1",
        contact = Contact(
            name = "Matthias 'Yolgie' Holzinger",
            url = "https://github.com/Yolgie"
        ),
        license = License(
            name = "GNU General Public License v3.0"
        )
    )
)
class CrawlerApi : Application()
