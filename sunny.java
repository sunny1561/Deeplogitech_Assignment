
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class  sunny{
    public static void main(String[] args) throws IOException {
        //i used this port
        int port = 8080; 
        String serviceUrl = "http://localhost:" + port + "/getTimeStories";

        //created the http server at the port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Create a context for the /getTimeStories endpoint
        server.createContext("/getTimeStories", new TimeStoriesHandler());

        // Start the server 
        server.start();
        

        // Printing here the service URL  
        System.out.println("Service URL: " + serviceUrl);

      
        System.out.println("Press Enter to stop the server...");
        System.in.read();

        // press enter to stop the server
        server.stop(0);
    }
    
    // This is the  Custom handler for /getTimeStories endpoint (as Mentioned)
    static class TimeStoriesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Get the latest stories
            List<String> latestStories = Get_latestStories("https://time.com");

            String jsonResponse = "[" + String.join(",", latestStories) + "]";
           
            // Set response headers
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.length());

            // sending  the JSON response to the end user
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(jsonResponse.getBytes());
            }
        }
    }

    private static List<String> Get_latestStories(String url) {
        List<String> latestStories = new ArrayList<>();

        try {
            // Example using the constructor URL(String, String)
             

            @SuppressWarnings("deprecation")
            HttpURLConnection connection =   (HttpURLConnection) new URL(url).openConnection();
            
            connection.setRequestMethod("GET");
            //to check the connection
            System.out.println("Successfully Connected!! : " + connection.getResponseCode());

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder content = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }


                //using the boelow regular expression we can direcly fetch the link and title from the source page
            Pattern obj_pattern = Pattern.compile("<a[^>]*href\\s*=\\s*[\"']([^\"']*)[\"'][^>]*>" +
                                                   "\\s*<h3\\s+class=\"latest-stories__item-headline\">(.*?)<\\/h3>",
                                                   Pattern.CASE_INSENSITIVE);
            Matcher obj_Matcher = obj_pattern.matcher(content.toString());

            int count = 0; // to extract 6 latest news
            while (obj_Matcher.find() && count < 6) {
                String prefix="https://time.com";
                String storyUrl = prefix+obj_Matcher.group(1);
                String storyTitle = obj_Matcher.group(2);
                String json = "{\"title\":\"" + storyTitle + "\",\"link\":\"" + storyUrl + "\"}";
                latestStories.add(json);

                count++;
            }
               
                reader.close();
            } else {
                System.out.println("HTTP request failed: " + connection.getResponseCode() + " " + connection.getResponseMessage());
            }

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return latestStories;
    }
}
