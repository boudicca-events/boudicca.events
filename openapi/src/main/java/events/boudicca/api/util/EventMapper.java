package events.boudicca.api.util;


import events.boudicca.SemanticKeys;
import events.boudicca.api.Event;
import events.boudicca.api.RegistrationEnum;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class EventMapper {
    public static events.boudicca.openapi.model.Event toOpenApiEvent(Event event) {
        events.boudicca.openapi.model.Event openApiEvent = new events.boudicca.openapi.model.Event();

        openApiEvent.setName(event.getName());
        openApiEvent.setStartDate(event.getStartDate());

        Map<String, String> openApiData = new HashMap<>();
        for (Map.Entry<String, String> entry : event.getAdditionalData().entrySet()) {
            openApiData.put(entry.getKey(), entry.getValue());
        }

        openApiData.put(SemanticKeys.ENDDATE, event.getEndDate().format(DateTimeFormatter.ISO_DATE));
        openApiData.put(SemanticKeys.URL, event.getUrl());
        openApiData.put(SemanticKeys.TYPE, event.getType());
        openApiData.put(SemanticKeys.DESCRIPTION, event.getDescription());
        openApiData.put(SemanticKeys.REGISTRATION, event.getRegistration() != null ? event.getRegistration().name() : null);
//        openApiData.put(SemanticKeys.TAGS, event.getTags()); TODO
        openApiData.put(SemanticKeys.LOCATION_NAME, event.getLocation().getName());
        openApiData.put(SemanticKeys.LOCATION_URL, event.getLocation().getUrl());
        openApiData.put(SemanticKeys.LOCATION_COORDINATES, event.getLocation().getCoordinates());
        openApiData.put(SemanticKeys.LOCATION_CITY, event.getLocation().getCity());
        openApiData.put(SemanticKeys.CONCERT_GENRE, event.getConcert().getGenre());
//        openApiData.put(SemanticKeys.CONCERT_BANDLIST, event.getConcert().getBandList()); TODO

        return openApiEvent;
    }

    public static Event toEvent(events.boudicca.openapi.model.Event openApiEvent) {

        Event event = new Event();

        event.setName(openApiEvent.getName());
        event.setStartDate(openApiEvent.getStartDate());

        if(openApiEvent.getData() == null) {
            return event;
        }

        event.setAdditionalData(openApiEvent.getData()); //TODO filter known keys

        // event.setEndDate(OffsetDateTime.parse(openApiEvent.getData().get(SemanticKeys.ENDDATE), DateTimeFormatter.ISO_DATE));
        event.setUrl(openApiEvent.getData().get(SemanticKeys.URL));
        event.setType(openApiEvent.getData().get(SemanticKeys.TYPE));
        event.setDescription(openApiEvent.getData().get(SemanticKeys.DESCRIPTION));
        event.setRegistration(openApiEvent.getData().get(SemanticKeys.DESCRIPTION) != null ? RegistrationEnum.valueOf(openApiEvent.getData().get(SemanticKeys.DESCRIPTION)) : null);
//        openApiData.put(SemanticKeys.TAGS, event.getTags()); TODO
        event.getLocation().setName(openApiEvent.getData().get(SemanticKeys.LOCATION_NAME));
        event.getLocation().setUrl(openApiEvent.getData().get(SemanticKeys.LOCATION_URL));
        event.getLocation().setCoordinates(openApiEvent.getData().get(SemanticKeys.LOCATION_COORDINATES));
        event.getLocation().setCity(openApiEvent.getData().get(SemanticKeys.LOCATION_CITY));
        event.getConcert().setGenre(openApiEvent.getData().get(SemanticKeys.CONCERT_GENRE));
//        openApiData.put(SemanticKeys.CONCERT_BANDLIST, event.getConcert().getBandList()); TODO

        return event;
    }
}
