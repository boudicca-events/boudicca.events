package events.boudicca.api;

public class Location {
    private String name;
    private String url;
    private String coordinates; //???
    private String city;

    public String getName() {
        return name;
    }

    public Location setName(String name) {
        this.name = name;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public Location setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public Location setCoordinates(String coordinates) {
        this.coordinates = coordinates;
        return this;
    }

    public String getCity() {
        return city;
    }

    public Location setCity(String city) {
        this.city = city;
        return this;
    }
}
