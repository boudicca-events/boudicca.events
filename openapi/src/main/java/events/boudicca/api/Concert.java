package events.boudicca.api;

import java.util.List;

public class Concert {
    private String genre;
    private List<String> bandList;

    public String getGenre() {
        return genre;
    }

    public Concert setGenre(String genre) {
        this.genre = genre;
        return this;
    }

    public List<String> getBandList() {
        return bandList;
    }

    public Concert setBandList(List<String> bandList) {
        this.bandList = bandList;
        return this;
    }
}
