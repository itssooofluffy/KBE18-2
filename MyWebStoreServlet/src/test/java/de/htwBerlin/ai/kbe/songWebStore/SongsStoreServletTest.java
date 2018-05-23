package de.htwBerlin.ai.kbe.songWebStore;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SongsStoreServletTest {

    private SongsStoreServlet servlet;
    private MockServletConfig config;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private ObjectMapper objectMapper;
    private String json;

    @Before
    public void setUp() throws ServletException {
        objectMapper = new ObjectMapper();
        this.request = new MockHttpServletRequest("GET", "http://localhost:8080/songWebStore/servlet/de.htwBerlin.ai.kbe.songWebStore.SongsStoreServlet");
        this.response = new MockHttpServletResponse();
        this.servlet = new SongsStoreServlet();
        this.config = new MockServletConfig("SongsStoreServlet");
    }

    @Test
    public void initShouldLoadSongList() throws ServletException {
        servlet.init(config);
    }

    @Test
    public void doGetShouldGetNoQueryOrWrongQuery() throws IOException, ServletException {
        servlet.init(config);

        String noQuery = "Query hat noch keine Funktion.";

        servlet.doGet(request, response);
        assertEquals(noQuery, response.getContentAsString());
        this.request.setQueryString("huhu");
        assertEquals(noQuery, response.getContentAsString());
    }

    @Test
    public void doGetShouldGetFullList() throws IOException, ServletException {
        servlet.init(config);

        InputStream input = this.getClass().getClassLoader().getResourceAsStream("testSongs.json");
        List<Song> songList = objectMapper.readValue(input, new TypeReference<List<Song>>() {
        });
        json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(songList);

        request.addParameter("all");

        servlet.doGet(request, response);
        assertEquals(json, response.getContentAsString());
    }

    @Test
    public void doGetShouldGetSpecificId() throws IOException, ServletException {
        servlet.init(config);

        InputStream input = this.getClass().getClassLoader().getResourceAsStream("testSongs.json");
        List<Song> songList = objectMapper.readValue(input, new TypeReference<List<Song>>() {
        });
        String songNrSechs = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(songList.get(4));

        this.request.setRequestURI("http://localhost:8080/songWebStore/servlet/de.htwBerlin.ai.kbe.songWebStore.SongsStoreServlet");
        this.request.addParameter("songId", "6");
        servlet.doGet(this.request, this.response);
        assertEquals(songNrSechs, response.getContentAsString());
    }

    @Test
    public void doGetShouldGetNoNumericId() throws IOException, ServletException {
        servlet.init(config);

        InputStream input = this.getClass().getClassLoader().getResourceAsStream("testSongs.json");
        List<Song> songList = objectMapper.readValue(input, new TypeReference<List<Song>>() {
        });
        String nonNumeric = "Keine sinnvolle Eingabe für ID.";

        this.request.setRequestURI("http://localhost:8080/songWebStore/servlet/de.htwBerlin.ai.kbe.songWebStore.SongsStoreServlet");
        this.request.addParameter("songId", "n");
        servlet.doGet(this.request, this.response);
        assertEquals(nonNumeric, response.getContentAsString());
    }

    @Test
    public void doGetShouldGetNoExistingNumericId() throws IOException, ServletException {
        servlet.init(config);

        InputStream input = this.getClass().getClassLoader().getResourceAsStream("testSongs.json");
        List<Song> songList = objectMapper.readValue(input, new TypeReference<List<Song>>() {
        });
        String nonExisting = "Diese ID gibt es leider noch nicht.";

        this.request.setRequestURI("http://localhost:8080/songWebStore/servlet/de.htwBerlin.ai.kbe.songWebStore.SongsStoreServlet");
        this.request.addParameter("songId", "100");
        servlet.doGet(this.request, this.response);
        assertEquals(nonExisting, response.getContentAsString());
    }

}
