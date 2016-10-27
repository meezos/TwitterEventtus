package com.meezo.eventtus.twittereventtus;

/*
 * Created by mazenmahmoudarakji on 10/25/16.  Babbage.
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
    private final static int PORT_NUMBER = 6783;

    private static HashMap<String,ClientThread> clientThreads =new HashMap<>();

    public static void logIn(String screenName) {
        ClientThread clientThread = new ClientThread();
        clientThread.sendMessage("login:"+screenName);
        clientThreads.put(screenName,clientThread);
        Thread thread = new Thread(clientThread);
        thread.start();
    }

    public static boolean isUserLoggedIn(String screenName,String userToCheckScreenName) {
        ClientThread clientThread = clientThreads.get(screenName);
        clientThread.sendMessage("status:" + userToCheckScreenName + '\n');
        String status = clientThread.receiveMessage();

        return status != null && status.equals("yes");
    }

    public static void logOut(String screenName) {
        ClientThread clientThread = clientThreads.get(screenName);
        clientThread.sendMessage("logout:" + screenName + '\n');
    }

    private static class ClientThread implements Runnable {
        BufferedReader reader;
        BufferedWriter writer;

        ClientThread() {
            try {
                PipedReader pr = new PipedReader();
                PipedWriter pw = new PipedWriter();
                pw.connect(pr);
                reader = new BufferedReader(pr);
                writer = new BufferedWriter(pw);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("evtw", "exception", e);
            }
        }

        public void run() {

            try {
                InetAddress serverAddress = InetAddress.getByName(IP_ADDRESS);
                Socket clientSocket = new Socket(serverAddress, PORT_NUMBER);
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

                while(true){
                    String message= reader.readLine();
                    outToServer.writeBytes(message+'\n');
                    String screenName = message.substring(0,message.indexOf(":"));

                    if(message.startsWith("status")){
                        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        String reply = inFromServer.readLine();
                        writer.write(reply+"\n");
                    }

                    else if(message.startsWith("logout")){
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

        private void sendMessage(String message){
            try {
                writer.write(message + '\n');
            }
            catch (IOException e) {
                e.printStackTrace();
                Log.e("evtw", "exception", e);
            }
        }

        private String receiveMessage(){
            String ret=null;
            try {
                ret=reader.readLine();
            }
            catch (IOException e) {
                e.printStackTrace();
                Log.e("evtw", "exception", e);
            }
            return ret;
        }
    }
}