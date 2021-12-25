package advisor;

import java.net.http.HttpResponse;
import java.util.HashMap;

public class Controller {

    private final String accessServer;
    private final String resourceServer;
    private int limit;
    private boolean isAuth = false;
    private int page = 1;

    Model model = new Model();
    View view = new View();

    public Controller(String accessServer, String resourceServer, int limit){
        this.accessServer = accessServer;
        this.resourceServer = resourceServer;
        this.limit = limit;
    }

    public void execute(String[] command) {

        if (command[0].equals("auth")) {
            if (!isAuth) {
                model.createHTTPServer(8090, accessServer);
                isAuth = model.makeAuthRequest(accessServer);
                System.out.println(isAuth ? "Success!" : "Failed");
            } else {
                System.out.println("Already Authorized");
            }
            return;
        }
        if (command[0].equals("exit")) {
            System.out.println("---GOODBYE!---");
            return;
        }
        if (isAuth) {
            String response;
            String uri;
            HttpResponse<String> res;
            switch(command[0]) {
                case "new":
                    response = model.makeAPIRequest(resourceServer + "/v1/browse/new-releases", limit);
                    view.getNewReleases(response);
                    break;
                case "featured":
                    response = model.makeAPIRequest(resourceServer + "/v1/browse/featured-playlists", limit);
                    view.getFeatured(response);
                    break;
                case "categories":
                    response = model.makeAPIRequest(resourceServer + "/v1/browse/categories", limit);
                    view.getCategories(response, Boolean.TRUE);
                    break;
                case "playlists":
                    response = model.makeAPIRequest(resourceServer + "/v1/browse/categories", limit);
                    HashMap<String, String>categories = view.getCategories(response, false);
                    response = model.makeAPIRequest(String.format(resourceServer + "/v1/browse/categories/%s/playlists", categories.get(command[1])), limit);
                    view.getPlaylists(response);
                    break;
                case "next":
                    page += 1;
                    if (page > limit) {
                        System.out.println("No more pages");
                        return;
                    }
                    uri = model.getPage("next");
                    res = model.makeAPIRequest(uri);
                    view.printPage(res);
                    break;
                case "prev":
                    page -= 1;
                    if (page < 1) {
                        System.out.println("No more pages");
                        return;
                    }
                    uri = model.getPage("previous");
                    res = model.makeAPIRequest(uri);
                    view.printPage(res);
                    break;
                default:
                    System.out.println("Enter a valid command\n");
            }
        } else {
            System.out.println("Provide access to the application");
        }
        System.out.println(String.format("---PAGE %d OF %d---",page,limit));
    }
}