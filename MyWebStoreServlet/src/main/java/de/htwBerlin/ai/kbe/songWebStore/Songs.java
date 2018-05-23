package de.htwBerlin.ai.kbe.songWebStore;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "songs")
public class Songs {

    List<Song> songs;

    public List<Song> getSongs() {
        return songs;
    }

    @XmlElement(name = "song")
    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

}
