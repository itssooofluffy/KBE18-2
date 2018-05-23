package de.htwBerlin.ai.kbe.songWebStore;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.StringReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.SAXException;

@WebServlet(
        name = "songsServlet",
        urlPatterns = "/*",
        initParams = {
            @WebInitParam(name = "signature", value = "HUHU")}
)
public class SongsStoreServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String APPLICATION_JSON = "application/json";
    private static final String TEXT_PLAIN = "text/plain";
    private final Map<Integer, Song> songStore = new HashMap<>();
    private Integer currentID;
    private ArrayList<Song> songList = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SongsStoreServlet() {
        this.currentID = 1;
    } 

    // load songStore from JSON file and set currentID
    @Override
    public void init(ServletConfig servletConfig) throws ServletException {

        InputStream input = this.getClass().getClassLoader().getResourceAsStream("songs.json");
        try {
            songList = objectMapper.readValue(input, new TypeReference<List<Song>>() {
            });
        } catch (JsonParseException e) {
        } catch (JsonMappingException e) {
        } catch (IOException e) {
        }

        for (int i = songList.size() - 1; i >= 0; i--) {
            currentID++;
            songStore.put(currentID, songList.get(i));
        }
        currentID--;
        System.out.println("Init Finished");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter printwriter;
        String queryParam = "default";
        Enumeration<String> paramKeys = request.getParameterNames();

        while (paramKeys.hasMoreElements()) {
            queryParam = paramKeys.nextElement();
        }

        switch (queryParam) {
            case "all":
                response.setContentType(APPLICATION_JSON);
                response.setStatus(200);
                printwriter = response.getWriter();
                printwriter.append(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(songList));
                printwriter.close();
                break;
            case "songId":
                try {
                    request.getParameterValues("id");
                } catch (NoSuchElementException e) {

                }
                printwriter = response.getWriter();
                Integer key = -1;
                ;
                try {
                    key = Integer.parseInt(request.getParameter(queryParam));
                } catch (NumberFormatException e) {
                    response.setContentType(TEXT_PLAIN);
                    printwriter.append("Keine sinnvolle Eingabe für ID.");
                }
                if (songStore.containsKey(key)) {
                    response.setContentType(APPLICATION_JSON);
                    response.setStatus(200);
                    printwriter.append(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(songStore.get(key + 1)));
                } else {
                    response.setContentType(TEXT_PLAIN);
                    printwriter.append("Diese ID gibt es leider noch nicht");
                }
                printwriter.close();
                break;
            default:
                response.setContentType(TEXT_PLAIN);
                printwriter = response.getWriter();
                printwriter.append("Query hat noch keine Funktion");
                printwriter.close();
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        try {
            BufferedReader bufferedReader = request.getReader();
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            if (isValidJson(stringBuilder.toString())) {
                Song newSong = objectMapper.readValue(stringBuilder.toString(), new TypeReference<Song>() {
                });
                createResponse(newSong, response);

            } else if (isValidXML(stringBuilder.toString())) {
                Songs songs;
                songs = readXMLToSongs(stringBuilder.toString());
                Song newSong;
                newSong = songs.getSongs().get(0);
                
                createResponse(newSong, response);       

            } else {
                response.setContentType(TEXT_PLAIN);
                PrintWriter printwriter = response.getWriter();
                printwriter.append("Request enthält keinen gueltigen Format");
            }
        } catch (SAXException | JAXBException ex) {
        }
    }

    // Reads a list of songs from an XML-file into Songs.java
    static Songs readXMLToSongs(String xmlSong) throws JAXBException, IOException {
        JAXBContext context = JAXBContext.newInstance(Songs.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        StringReader reader = new StringReader(xmlSong);
        return (Songs) unmarshaller.unmarshal(reader);

    }

    private boolean isValidXML(String xml) throws SAXException {
        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        Schema schema = factory.newSchema(new StreamSource(this.getClass().getClassLoader().getResourceAsStream("songs.xsd")));
        final Validator validator = schema.newValidator();
        try {
            validator.validate(new StreamSource(new StringReader(xml)));
            return true;
        } catch (final IOException | SAXException ioe) {
        }
        return false;
    }

    private boolean isValidJson(String maybeJson) {
        try {
            objectMapper.readTree(maybeJson);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private synchronized void createResponse(Song newSong, HttpServletResponse response) throws IOException {
        currentID++;
        newSong.setId(currentID);
        songList.add(newSong);
        songStore.put(currentID, newSong);

        response.setContentType(TEXT_PLAIN);
        response.setStatus(200);
        PrintWriter printwriter = response.getWriter();
        printwriter.append("Added new Song '" + songStore.get(currentID).getTitle() + "', id= " + currentID);
        printwriter.close();
    }

    // save songStore to file
    @Override
    public void destroy() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream("output.json"), songStore);
        } catch (IOException e) {
        }
    }
}
