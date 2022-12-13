import sk.uniba.fmph.Controller.ControllerCommunicationHandler;
import sk.uniba.fmph.Server;

public class Main {
    public static void main(String[] args) {
        ControllerCommunicationHandler.getInstance().requestControllerIps();
        Server.getInstance().begin();
    }
}
