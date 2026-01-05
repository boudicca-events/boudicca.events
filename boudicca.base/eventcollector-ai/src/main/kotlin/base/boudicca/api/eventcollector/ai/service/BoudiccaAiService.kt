package base.boudicca.api.eventcollector.ai.service

import base.boudicca.api.eventcollector.ai.model.EventData
import org.springframework.ai.chat.client.AdvisorParams
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.entity
import org.springframework.stereotype.Service

@Service
class BoudiccaAiService(chatClientBuilder: ChatClient.Builder) {

    val chatClient: ChatClient = chatClientBuilder.build()

    /**
     * Note: If this function throws an exception because the json cannot be parsed, then it is most likely the response
     * exceeds the maximum number of tokens returned by mistral. Mistral has a hard maximum limit of 4096 tokens apparently.
     */
    fun extractEventList(text: String): List<EventData> {
        return chatClient.prompt()
            .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
            .user { u ->
                u.text(
                    "Find name of the event, location, startDate and if given startTime for all events described in the following text. " +
                        "Return all events, do not truncate any data. Convert ranges given to a list of dates. Use YYYY-MM-DD format for dates and hh:mm for times. " +
                        "In case of ambiguous times use Abfahrt or Departure times. \n\n {eventText}",
                ).param("eventText", text)
            }.call().entity<List<EventData>>()
    }

    fun summarizeDescription(text: String): String {
        TODO()
        return text
    }

    fun deduceEnumValueFromText<T>(text: String, enumDescription: EnumDescription<T>): T {
        TODO()
        return enumDescription.values.first().value
    }

}

// Basierend auf dem input text gib mir eine Liste von Veranstaltungen die sich aus dem Text ergeben im JSON format zurück. Nutze dein Wissen über Wochentage um eine vollständige liste aller daten zu erstellen die sich aus dem text ergeben. Benutze das angegebene JSON Schema. nenne die liste "events". lasse felder für die du keine werte findest weg
//
// {
//     "properties": {
//     "date": {
//     "type": "string",
//     "description": "Datum der Veranstaltung"
// },
//     "location": {
//     "type": "string",
//     "description": "Name des Veranstaltungsortes"
// },
//     "name": {
//     "type": "string",
//     "description": "Name der Veranstaltung"
// },
//     "startTime": {
//     "type": "string",
//     "description": "Start Uhrzeit der Veranstaltung"
// }
// },
//     "required": [
//     "date"
//     ]
// }
//
// Feiertage 2026 in Österreich sind:
// Neujahr 2026	Donnerstag 1 Jänner 2026
// Heilige Drei Könige 2026	Dienstag 6 Jänner 2026
// Ostermontag 2026	Montag 6 April 2026
// Staatsfeiertag 2026	Freitag 1 Mai 2026
// Christi Himmelfahrt 2026	Donnerstag 14 Mai 2026
// Pfingstmontag 2026	Montag 25 Mai 2026
// Fronleichnam 2026	Donnerstag 4 Juni 2026
// Mariä Himmelfahrt 2026	Samstag 15 August 2026
// Nationalfeiertag 2026	Montag 26 Oktober 2026
// Allerheiligen 2026	Sonntag 1 November 2026
// Mariä Empfängnis 2026	Dienstag 8 Dezember 2026
// Christtag 2026	Freitag 25 Dezember 2026
// Stephanstag 2026	Samstag 26 Dezember 2026
