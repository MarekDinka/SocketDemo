package sk.uniba.fmph;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            Client client = new Client();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
