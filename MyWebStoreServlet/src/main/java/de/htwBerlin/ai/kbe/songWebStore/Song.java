package de.htwBerlin.ai.kbe.songWebStore;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "song")
public class Song {

    private Integer id;
    @XmlElement(name = "title")
    private String title;
    @XmlElement(name = "artist")
    private String artist;
    @XmlElement(name = "album")
    private String album;
    @XmlElement(name = "released")
    private Integer released;

    public Song() {
    }

    public Song(Integer id, String title, String artist, String album, Integer released) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.released = released;
    }

    public Integer getId() {
        return id;
    }

    void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public Integer getReleased() {
        return released;
    }
}
