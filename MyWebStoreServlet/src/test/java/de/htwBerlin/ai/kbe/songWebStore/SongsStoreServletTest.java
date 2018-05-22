package de.htwBerlin.ai.kbe.songWebStore;

import static org.junit.Assert.assertEquals;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SongsStoreServletTest {
	
    private SongsStoreServlet servlet;
    private MockServletConfig config;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private 	ObjectMapper objectMapper;
    private String json;
    
    @Before
    public void setUp() throws ServletException {
    		objectMapper = new ObjectMapper();
    		this.request = new MockHttpServletRequest("GET", "http://localhost:8888/songWebStore/servlet/de.htwBerlin.ai.kbe.songWebStore.SongsStoreServlet");
    		this.response = new MockHttpServletResponse();
    		this.servlet = new SongsStoreServlet();
    		this.config = new MockServletConfig("SongsStoreServlet");
    }
    
    @SuppressWarnings("unchecked") 
	@Test
    public void playingWithJackson() throws IOException {
	    // songs.json and testSongs.json contain songs from this Top-10:
	    // http://time.com/collection-post/4577404/the-top-10-worst-songs/
	    
    	    // Read a JSON file and create song list:
    		InputStream input = this.getClass().getClassLoader().getResourceAsStream("testSongs.json");
	    
    		List<Song> songList = (List<Song>) objectMapper.readValue(input, new TypeReference<List<Song>>(){});
	    
	    	// write a list of objects to a JSON-String with prettyPrinter
	    	json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(songList);
	    
	    	// write a list of objects to an outputStream in JSON format
	    	objectMapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream("output.json"), songList);
    
	    	// Create a song and write to JSON
	    	Song song = new Song (null, "titleXX", "artistXX", "albumXX", 1999);
	    byte[] jsonBytes = objectMapper.writeValueAsBytes(song);
	    
	    // Read JSON from byte[] into Object
	    Song newSong = (Song) objectMapper.readValue(jsonBytes, Song.class);
	    assertEquals(song.getTitle(), newSong.getTitle());
	    assertEquals(song.getArtist(), newSong.getArtist());
    }
    
    @Test
    public void initShouldLoadSongList() throws ServletException {
    	servlet.init(config);
    }
    
	@Test
    public void doGetShouldGetNoQueryOrWrongQuery() throws IOException, ServletException {	
		servlet.init(config);

		String noQuery = "Query hat noch keine Funktion";
    	
    	servlet.doGet(request, response);
    	assertEquals(noQuery, response.getContentAsString());
    	this.request.setQueryString("huhu");
    	assertEquals(noQuery,response.getContentAsString());
    }

	@Test
    public void doGetShouldGetFullList() throws IOException, ServletException {	
		servlet.init(config);

		InputStream input = this.getClass().getClassLoader().getResourceAsStream("testSongs.json");  
		List<Song> songList = objectMapper.readValue(input, new TypeReference<List<Song>>(){}); 
    	json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(songList);
    	
    	request.addParameter("all");
    	
    	servlet.doGet(request, response);
    	assertEquals(json, response.getContentAsString());
    }
	
	@Test
	   public void doGetShouldGetSpecificId() throws IOException, ServletException {	
			servlet.init(config);
		
			InputStream input = this.getClass().getClassLoader().getResourceAsStream("testSongs.json");  
			List<Song> songList = objectMapper.readValue(input, new TypeReference<List<Song>>(){}); 
	    	String songNrSechs = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(songList.get(4));
	    	
	    	this.request.setRequestURI("http://localhost:8080/songWebStore/servlet/de.htwBerlin.ai.kbe.songWebStore.SongsStoreServlet");
	    	this.request.addParameter("songId", "6");
	    	servlet.doGet(this.request, this.response);
	    	assertEquals(songNrSechs, response.getContentAsString());
	    }   
}
