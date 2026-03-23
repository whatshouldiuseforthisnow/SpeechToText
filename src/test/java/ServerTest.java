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
        String baseUrl = secrets.getProperty("API_BASE_URL");
        assertNotNull(baseUrl, "API BASE URL NOT SET");
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/meeting-rooms/available"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        assertTrue(response.statusCode() == 200 || response.statusCode() == 401,
                "Server reachable but status :" + response.statusCode());
        System.out.println(("Server is up with Status:" + response.statusCode()));
    }
}
