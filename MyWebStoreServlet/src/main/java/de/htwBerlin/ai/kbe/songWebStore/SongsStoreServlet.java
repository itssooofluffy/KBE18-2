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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
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
    private Map<Integer, Song> songStore = new HashMap<Integer, Song>();
    private Integer currentID = 1;
    private ArrayList<Song> songList = new ArrayList<Song>();
    private ObjectMapper objectMapper = new ObjectMapper();
    private File file;

    // load songStore from JSON file and set currentID
    public void init(ServletConfig servletConfig) throws ServletException {

        InputStream input = this.getClass().getClassLoader().getResourceAsStream("songs.json");
        try {
            songList = objectMapper.readValue(input, new TypeReference<List<Song>>() {
            });
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = songList.size() - 1; i >= 0; i--) {
            currentID++;
            songStore.put(currentID, songList.get(i));
        }
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

        BufferedReader bufferedReader = request.getReader();
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        if (isValidJson(stringBuilder.toString())) {

            Song newSong = objectMapper.readValue(stringBuilder.toString(), new TypeReference<Song>() {
            });
            createResonse(newSong, response);

        } else if (isValidXML(stringBuilder.toString())) {
            Song newSong = JAXB.unmarshal(new StringReader(stringBuilder.toString()), Song.class);
            createResonse(newSong, response);

        } else {
            response.setContentType(TEXT_PLAIN);
            PrintWriter printwriter = response.getWriter();
            printwriter.append("Request enthält keinen gueltigen Format");
        }
    }

    private boolean isValidXML(String maybeXML) {
        Song song = JAXB.unmarshal(new StringReader(maybeXML), Song.class);
        return true;
    }

    private boolean isValidJson(String maybeJson) {
        try {
            objectMapper.readTree(maybeJson);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void createResonse(Song newSong, HttpServletResponse response) throws IOException {
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
        System.out.println("In destroy");
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new FileOutputStream("output.json"), songList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
