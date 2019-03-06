package com.mycompany.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sound.sampled.SourceDataLine;

public class SimpleLogEchoServer implements LogEchoServer{

    ServerSocket serverSocket;
    int contentLength = 0;
    int connectionCount;
    ExecutorService executor;

    public SimpleLogEchoServer(){
        connectionCount = 0;
        executor = Executors.newCachedThreadPool();
    }

    private void resetConnectionCount(){
        connectionCount = 0;
    }

    private void incrementConnectionCount(){
        if(connectionCount+1 == Integer.MAX_VALUE){
            resetConnectionCount();
        }

        connectionCount++;
    }

    public void go(int port) throws IOException{

        serverSocket = new ServerSocket(port);
        System.out.println(String.format("Started server on port %d", port));

        while(true){
            Socket clientSocket;
            try{
                clientSocket = acceptConnection();
                serviceConnection(clientSocket);
            }catch(IOException e){
                System.out.println("Oops! Something happened. Connection ended.");
            }
        }
    }

    public Socket acceptConnection() throws IOException {
        Socket clientSocket = serverSocket.accept();
        incrementConnectionCount();
        return clientSocket;
    }

    public void serviceConnection(Socket clientSocket) throws IOException{
        Runnable requestServicer = new RequestServicer(clientSocket, connectionCount);
        executor.execute(requestServicer);
    }
}
