package advisor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class View {

    public static String getAuthCode(String response) {
        String accessToken;
        JsonObject jo = JsonParser.parseString(response).getAsJsonObject();
        accessToken = jo.get("access_token").getAsString();

        return  accessToken;
    }
    public void getNewReleases(String response) {
        JsonObject jo = JsonParser.parseString(response).getAsJsonObject();
        JsonObject albums = jo.getAsJsonObject("albums");
        JsonArray items = albums.getAsJsonArray("items");

        for(JsonElement e : items) {
            JsonObject item = e.getAsJsonObject();
            String name = item.get("name").getAsString();
            JsonObject url = item.getAsJsonObject("external_urls");
            String link = url.get("spotify").getAsString();
            JsonArray artistsArray = item.getAsJsonArray("artists");
            List<String> artistsList = new ArrayList<>();

            for (JsonElement a : artistsArray) {
                JsonObject artist = a.getAsJsonObject();
                String artistName = artist.get("name").getAsString();
                artistsList.add(artistName);
            }
            String artists = String.join(", ", artistsList);

            System.out.println(name + "\n" + "[" + artists + "]\n" + link);
        }
    }

    public void getFeatured(String response) {

        JsonObject jo = JsonParser.parseString(response).getAsJsonObject();
        JsonObject playlist = jo.getAsJsonObject("playlists");
        JsonArray items = playlist.getAsJsonArray("items");

        for (JsonElement e : items) {
            JsonObject item = e.getAsJsonObject();
            String name = item.get("name").getAsString();
            JsonObject url = item.getAsJsonObject("external_urls");
            String link = url.get("spotify").getAsString();

            System.out.println(name + "\n" + link);
        }
    }

    public HashMap<String, String> getCategories(String response, Boolean print) {
        HashMap<String, String> categoriesMap = new HashMap<>();
        JsonObject jo = JsonParser.parseString(response).getAsJsonObject();
        JsonObject categories = jo.getAsJsonObject("categories");
        JsonArray items = categories.getAsJsonArray("items");

        for(JsonElement e : items) {
            JsonObject item = e.getAsJsonObject();
            String categoryName = item.get("name").getAsString();
            String categoryID = item.get("id").getAsString();
            categoriesMap.put(categoryName, categoryID);
        }
        if (print) {
            for(String category : categoriesMap.keySet()) {
                System.out.println(category);
            }
        }
        return categoriesMap;
    }

    public void getPlaylists(String response) {
        if (response.contains("error")) {
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            JsonObject error = json.getAsJsonObject("error");
            System.out.println(error.get("message").getAsString());
        }
        JsonObject jo = JsonParser.parseString(response).getAsJsonObject();
        JsonObject playlists = jo.getAsJsonObject("playlists");
        JsonArray items = playlists.getAsJsonArray("items");

        for(JsonElement e : items) {
            JsonObject item = e.getAsJsonObject();
            String playlistName = item.get("name").getAsString();
            JsonObject url = item.getAsJsonObject("external_urls");
            String link = url.get("spotify").getAsString();
            System.out.println(playlistName + "\n" + link);
        }
    }

    public static String getURI(String response, String direction) {
        JsonObject jo = JsonParser.parseString(response).getAsJsonObject();
        var keys = new ArrayList<>(jo.keySet());
        var key = keys.get(keys.size() - 1);
        var elem = jo.get(key).getAsJsonObject().get(direction);

        return elem.getAsString();
    }

    public void printPage(HttpResponse<String> response) {
        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        var keys = new ArrayList<>(json.keySet());
        var key = keys.get(keys.size() - 1);

        switch (key) {
            case "albums":
                getNewReleases(response.body());
                break;
            case "categories":
                getCategories(response.body(),true);
                break;
            case "playlists":
                getPlaylists(response.body());
                break;
        }
    }

}