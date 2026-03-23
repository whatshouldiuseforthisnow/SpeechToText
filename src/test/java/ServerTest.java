import org.example.SecretsLoader;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerTest {
    @Test
    void deployServerReach() throws Exception{
        Properties secrets = SecretsLoader.load();
        String baseUrl = System.getenv("API_BASE_URL");
        if (baseUrl == null || baseUrl.isEmpty()){
            baseUrl = secrets.getProperty("API_BASE_URL");
        }

        if(baseUrl == null || baseUrl.isEmpty()){
            System.out.println("skipping the test API BASE URL NOT read");
            return;
        }
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        assertTrue(response.statusCode() < 500,
                "Server reachable but status :" + response.statusCode());
        System.out.println(("Server is up with Status:" + response.statusCode()));
    }
}
