package advisor;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;

public class Model {

    private static String code;
    private static String accessToken;
    private static HttpResponse<String> response;

    public void createHTTPServer(int port, String accessServer) {
        CountDownLatch latch = new CountDownLatch(1);

        String accessLink = accessServer + "/authorize" +
                "?client_id=" + System.getenv("CLIENT_ID") +
                "&redirect_uri=http://localhost:8090" +
                "&response_type=code";

        try {
            HttpServer server = HttpServer.create();
            server.bind(new InetSocketAddress(port), 0);

            System.out.println("use this link to request the access code:");
            System.out.println(accessLink);

            server.createContext("/", new HttpHandler() {
                @Override
                public void handle(HttpExchange httpExchange) throws IOException {
                    String message;
                    String query = httpExchange.getRequestURI().getQuery();
                    if(query != null && query.contains("code")) {
                        code = query.substring(5);
                        message = "Got the code. Return back to your program.";
                    } else {
                        message = "Authorization code not found. Try again.";
                    }
                    latch.countDown();
                    httpExchange.sendResponseHeaders(200, message.length());
                    httpExchange.getResponseBody().write(message.getBytes());
                    httpExchange.getResponseBody().close();
                }
            });
            server.start();
            System.out.println("waiting for code...");
            latch.await();
            server.stop(10);

            System.out.println("code received");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean makeAuthRequest(String accessServer) {

        String bodyRequest = "grant_type=authorization_code" + "&"
                + "code=" + code + "&"
                + "redirect_uri=" + "http://localhost:8090" + "&"
                + "client_id=" + System.getenv("CLIENT_ID") + "&"
                + "client_secret=" + System.getenv("CLIENT_SECRET");

        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(accessServer + "/api/token"))
                .POST(HttpRequest.BodyPublishers.ofString(bodyRequest))
                .build();

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        accessToken = View.getAuthCode(response.body());

        return response.statusCode() == 200;
    }

    public String makeAPIRequest(String resourceServer, int limit) {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .headers("Authorization", "Bearer " + accessToken)
                .uri(URI.create(resourceServer + "?limit=" + limit))
                .GET()
                .build();

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public HttpResponse<String> makeAPIRequest(String uri) {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .headers("Authorization", "Bearer " + accessToken)
                .uri(URI.create(uri))
                .GET()
                .build();
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getPage(String direction) {
        return View.getURI(response.body(), direction);

    }

}