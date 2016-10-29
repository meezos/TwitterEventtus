import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;


class BackEndServer {

    private final static String PATH_TO_FILES = "/Users/mazenmahmoudarakji/Desktop/BackEndServer";
    private final static int PORT_NUMBER = 6777;
    private final static String LOGIN = "login";
    private final static String LOGOUT = "logout";
    private final static String STATUS = "status";

    public static void main(String argv[]) throws Exception {
        ServerSocket welcomeSocket = new ServerSocket(PORT_NUMBER);
        System.out.println("ok meezo");

        try {
            while (true) {
                Socket connectionSocket = welcomeSocket.accept();
                Thread thread = new Thread(new Session(connectionSocket));
                thread.start();
            }
        } catch (Exception e) {
        }
        welcomeSocket.close();
    }

    static class Session implements Runnable {
        Socket connectionSocket;

        Session(Socket connectionSocket) {
            this.connectionSocket = connectionSocket;
        }

        public void run() {
            BufferedReader inFromClient = null;
            DataOutputStream outToClient = null;
            try {
                inFromClient =
                        new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (true) {

                try {
                    String clientRequest = inFromClient.readLine();
                    System.out.println("Received: " + clientRequest);
                    String screenName = clientRequest.substring(clientRequest.indexOf(":") + 1);

                    File screenNameFile = new File(PATH_TO_FILES + File.separator + screenName);

                    if (clientRequest.startsWith(LOGIN)) {
                        boolean createdFile = false;
                        while (!createdFile) {
                            try {
                                screenNameFile.createNewFile();
                                createdFile = true;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    } else if (clientRequest.startsWith(STATUS)) {
                        String reply;
                        if (screenNameFile.exists())
                            reply = "yes" + '\n';
                        else
                            reply = "no" + '\n';
                        System.out.println(" status reply is " + reply);
                        outToClient.writeBytes(reply);

                    } else if (clientRequest.startsWith(LOGOUT)) {
                        boolean deletedFile = false;
                        if (screenNameFile.exists())
                            while (!deletedFile)
                                if (screenNameFile.delete())
                                    deletedFile = true;

                        connectionSocket.close();
                        break;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
