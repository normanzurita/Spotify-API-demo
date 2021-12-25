package advisor;

import java.util.Scanner;

public class Main {

    // Default values to be replaced by tests.
    private static String accessServer = "https://accounts.spotify.com";
    private static String resourceServer = "https://api.spotify.com";
    private static int page = 5;

    public static void main(String[] args) {

        if (args.length == 6 && args[0].equals("-access") && args[2].equals("-resource") && args[4].equals("-page")) {
            accessServer = args[1];
            resourceServer = args[3];
            page = Integer.valueOf(args[5]);
        }

        Controller controller = new Controller(accessServer, resourceServer, page);
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            String[] input = scanner.nextLine().split(" ", 2);
            controller.execute(input);
        }
    }
}