package events.boudicca.api;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Event {
    private String name;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private String url;
    private String type;
    private String description;
    private List<String> tags;
    private RegistrationEnum registration;
    private Location location = new Location();
    private Concert concert = new Concert();
    private Map<String, String> additionalData = new HashMap<>();

    public String getName() {
        return name;
    }

    public Event setName(String name) {
        this.name = name;
        return this;
    }

    public OffsetDateTime getStartDate() {
        return startDate;
    }

    public Event setStartDate(OffsetDateTime startDate) {
        this.startDate = startDate;
        return this;
    }

    public OffsetDateTime getEndDate() {
        return endDate;
    }

    public Event setEndDate(OffsetDateTime endDate) {
        this.endDate = endDate;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public Event setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getType() {
        return type;
    }

    public Event setType(String type) {
        this.type = type;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Event setDescription(String description) {
        this.description = description;
        return this;
    }

    public List<String> getTags() {
        return tags;
    }

    public Event setTags(List<String> tags) {
        this.tags = tags;
        return this;
    }

    public RegistrationEnum getRegistration() {
        return registration;
    }

    public Event setRegistration(RegistrationEnum registration) {
        this.registration = registration;
        return this;
    }

    public Location getLocation() {
        return location;
    }

    public Event setLocation(Location location) {
        this.location = location;
        return this;
    }

    public Concert getConcert() {
        return concert;
    }

    public Event setConcert(Concert concert) {
        this.concert = concert;
        return this;
    }

    public Map<String, String> getAdditionalData() {
        return additionalData;
    }

    public Event setAdditionalData(Map<String, String> additionalData) {
        this.additionalData = additionalData;
        return this;
    }
}
