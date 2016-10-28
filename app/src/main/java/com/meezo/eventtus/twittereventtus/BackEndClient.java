package com.meezo.eventtus.twittereventtus;

/*
 Created by mazenmahmoudarakji on 10/25/16.  Babbage.
 */


import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;


class BackEndClient {


    private final static String IP_ADDRESS = "10.10.10.16";
    private final static int PORT_NUMBER = 6777;

    private static HashMap<String, ClientThread> clientThreads = new HashMap<>();


    public static void logIn(String screenName) {
        ClientThread clientThread = new ClientThread();
        clientThread.sendMessage("login:" + screenName);
        clientThreads.put(screenName, clientThread);
        Thread thread = new Thread(clientThread);
        thread.start();
    }

    public static boolean isUserLoggedIn(String screenName, String userToCheckScreenName) {
        String result;
        ClientThread clientThread = clientThreads.get(screenName);
        clientThread.sendMessage("status:" + userToCheckScreenName);
        result = clientThread.receiveMessage();

        return result != null && result.equals("yes");
    }

    public static void logOut(String screenName) {
        ClientThread clientThread = clientThreads.get(screenName);
        if (clientThread != null)
            clientThread.sendMessage("logout:" + screenName);
    }

    private static class ClientThread implements Runnable {
        BufferedReader readerThreadIn;
        BufferedWriter writerThreadIn;

        BufferedReader readerThreadOut;
        BufferedWriter writerThreadOut;

        ClientThread() {
            try {
                PipedReader pr = new PipedReader();
                PipedWriter pw = new PipedWriter();
                pw.connect(pr);
                PipedReader pr1 = new PipedReader();
                PipedWriter pw1 = new PipedWriter();
                pw1.connect(pr1);

                readerThreadIn = new BufferedReader(pr);
                writerThreadIn = new BufferedWriter(pw);

                readerThreadOut = new BufferedReader(pr1);
                writerThreadOut = new BufferedWriter(pw1);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("evtw", "exception", e);
            }
        }

        public void run() {
            System.out.println("running thread now now");
            try {
                InetAddress serverAddress = InetAddress./*getLocalHost();*/ getByName(IP_ADDRESS);
                Socket clientSocket = new Socket(serverAddress, PORT_NUMBER);

                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                while (true) {
                    String message = readerThreadIn.readLine();
                    System.out.println(" message received was  " + message);
                    outToServer.writeBytes(message + '\n');
                    outToServer.flush();
                    String screenName = message.substring(message.indexOf(":") + 1);

                    if ( message.startsWith("status")) {
                        String reply = inFromServer.readLine();
                        writerThreadOut.write(reply + "\n");
                        writerThreadOut.flush();
                    }

                    if (message.startsWith("logout")) {
                        clientThreads.remove(screenName);
                        clientSocket.close();
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("evtw", "exception", e);
            }
        }

        private void sendMessage(String message) {
            try {
                System.out.println(" writing message now  " + message);
                writerThreadIn.write(message + '\n');
                writerThreadIn.flush();


            } catch (IOException e) {
                e.printStackTrace();
                Log.e("evtw", "exception", e);
            }
        }

        private String receiveMessage() {
            String ret = null;
            try {
                ret = readerThreadOut.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("evtw", "exception", e);
            }
            return ret;
        }
    }
}