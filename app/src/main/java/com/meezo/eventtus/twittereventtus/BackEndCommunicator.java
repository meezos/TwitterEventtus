package com.meezo.eventtus.twittereventtus;

/*
 * Created by mazenmahmoudarakji on 10/25/16.  Babbage.
 */

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.Semaphore;

class BackEndCommunicator {

    private final static String IP_ADDRESS="10.10.10.16";
    private final static int PORT_NUMBER=6783;

    private String reply;


    public void logIn(String userName) { sendToBackEnd("login:" + userName + '\n',false); }
    public void logOut(String userName) {
        sendToBackEnd("logout:" + userName + '\n',false);
    }
    public boolean isUserLoggedIn(String id) {
        String status=sendToBackEnd("status:" + id + '\n',true);
        return status!=null&&status.equals("yes");
    }

    private String sendToBackEnd(final String message,final boolean getReply) {

        reply=null;
        final Semaphore blockForReply = new Semaphore(1, true);
        blockForReply.drainPermits();

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    InetAddress serverAddress = InetAddress.getByName(IP_ADDRESS);
                    Socket clientSocket = new Socket(serverAddress, PORT_NUMBER);
                    DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                    outToServer.writeBytes(message);
                    if (getReply) {
                        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        reply = inFromServer.readLine();
                    }
                    clientSocket.close();

                } catch (IOException e) {
                    Log.e("evtw", "exception", e);
                }
                blockForReply.release();
            }
        };
        thread.start();
        if (getReply)
            try{blockForReply.acquire();}catch(Exception e){e.printStackTrace();Log.e("evtw", "exception", e);}

        return reply;
    }
}