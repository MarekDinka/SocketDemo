package Burnie;

public class Main {

    public static void main(String[] args) {
        try {
            ClientHandler ch = new ClientHandler();
            while (true) {
                System.out.println("Hallo:");
                System.out.println(ch.getNumberOfProjects());
                System.out.println(ch.getNumberOfControllers());
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            System.out.println("END");
            e.printStackTrace();
        }
    }
}
