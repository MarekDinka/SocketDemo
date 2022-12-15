package sk.uniba.fmph.Burnie;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            Client client = new Client();
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
