package events.boudicca;

import java.util.Set;

public enum EventCategory {

    MUSIC(Set.of("konzert", "concert", "alternative", "singer/songwriter", "party", "songwriter/alternative", "musik")),
    ART(Set.of("kabarett", "theater", "wissenskabarett", "provinzkrimi", "comedy", "figurentheater", "film", "visual comedy", "tanz", "performance", "musiklesung", "literatur", "museen", "museum", "ausstellung", "musical")),
    TECH(Set.of("techmeetup"));

    public final Set<String> types;

    EventCategory(Set<String> types) {
        this.types = types;
    }


}