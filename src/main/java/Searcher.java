import java.io.*;

import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;


import com.google.common.io.Files;
import org.json.simple.JSONArray;

import org.json.simple.JSONObject;

import org.json.simple.parser.JSONParser;

import org.json.simple.parser.ParseException;

//Класс для отправки запросов и десереализации в ArrayList
public class Searcher {

    private static final String filePath = "";
    private static final String BaseURL = "http://tacoburrito.pythonanywhere.com";
    public static final boolean BOOKS = true;
    public static final boolean DOCS = false;
    public static void main(String[] args) {
        JSONObject jsonTest = searchItems("sql", BOOKS);
        System.out.println(jsonTest.toJSONString());
        System.out.println(jsonTest.toString());
        ArrayList<Book> books = parseBooks(jsonTest);
        System.out.println(books.toString());

    }
    // Отправляет запрос на поиск книг/документов и возвращает JSONObject
    public static JSONObject searchItems(String phrase, boolean kind) {
        String urlPath = kind ? "apisearchbooks?q" : "apisearchdocs?q";
        URL url = null;
        JSONObject response = null;
        try {
            url = new URL(String.format("%s/%s=%s", BaseURL, urlPath, phrase));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setConnectTimeout(10000);
            con.setReadTimeout(10000);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            JSONParser jsonParser = new JSONParser();
            response = (JSONObject) jsonParser.parse(content.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static JSONObject getItemById(String key) {
        String[] kind_id = key.split("-");
        boolean kind = kind_id[0].equals("b"); // Если начинается с 'b' -> запрос на книгу, если с 'd' -> на документ
        String id = kind_id[1];
        String urlPath = kind ? "getbookbyid?id" : "getdocumentbyid?id";
        URL url;
        JSONObject response = null;
        File file = null;
        InputStream inputStream = null;
        try {
            url = new URL(String.format("%s/%s=%s", BaseURL, urlPath, id));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setConnectTimeout(8000);
            con.setReadTimeout(8000);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            JSONParser jsonParser = new JSONParser();
            response = (JSONObject) jsonParser.parse(content.toString());

//            String path = response.get(id).toString();
//            URL fullURL = path.startsWith("/media") ? new URL("https://tacoburrito.pythonanywhere.com" + path) : new URL(path);
//            inputStream = fullURL.openStream();
//            downloadUsingNIO(fullURL.toString(), "downloadedfiles/temp.pdf");
//            file = new File(fullURL.toURI());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
//        catch (URISyntaxException e) {
//            e.printStackTrace();
//        }
        return response;
    }
    private static void downloadUsingNIO(String urlStr, String file) throws IOException {
        URL url = new URL(urlStr);
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
    }

    // Преобразует JSONObject в ArrayList<Book>
    public static ArrayList<Book> parseBooks(JSONObject jsonObject) {
        ArrayList<Book> books = new ArrayList<Book>();
        try {
            Set keys = jsonObject.keySet();
            for (Object key : keys) {
                Map properties = (Map<String, Object>) jsonObject.get(key);
                books.add(new Book(key.toString(), properties.get("name").toString(), properties.get("file").toString(),
                        (ArrayList<String>) properties.get("categories")));

            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        System.gc();
        return books;
    }

    // Преобразует JSONObject в ArrayList<Document>
    public static ArrayList<Document> parseDocuments(JSONObject jsonObject) {
        ArrayList<Document> books = new ArrayList<Document>();
        try {
            Set keys = jsonObject.keySet();
            for (Object key : keys) {
                Map properties = (Map<String, Object>) jsonObject.get(key);
                books.add(new Document(key.toString(), properties.get("name").toString(), properties.get("file").toString()));
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        System.gc();
        return books;
    }
}

