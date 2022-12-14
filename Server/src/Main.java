import sk.uniba.fmph.Burnie.Controller.ControllerCommunicationHandler;
import sk.uniba.fmph.Burnie.Server;

public class Main {
    public static void main(String[] args) {
        ControllerCommunicationHandler.getInstance().requestControllerIps();
        Server.getInstance().begin();
    }
}
