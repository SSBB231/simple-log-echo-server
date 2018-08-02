package com.mycompany.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.InputMismatchException;
import java.util.Scanner;

public class SimpleLogEchoServer implements LogEchoServer{

    ServerSocket serverSocket;
    int contentLength = 0;
    int connectionCount;

    public SimpleLogEchoServer(){
        connectionCount = 0;
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

        if(clientSocket == null){
            return;
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());

        // Create read and write streams
        InputStreamReader clientInput = new InputStreamReader(clientSocket.getInputStream());
        BufferedReader clientInputReader = new BufferedReader(clientInput);
        PrintWriter clientWriter = new PrintWriter(clientSocket.getOutputStream());

        // initialize
        contentLength = 0;

        String headers = readHeaders(clientInputReader);
        String body = readBody(clientInputReader, contentLength);


        System.out.println(String.format("\n----------------------------------------------------------------\n%s", now.toString()));
        System.out.println(String.format("Request number: %d", connectionCount));
        System.out.println(String.format("Headers size: %d", headers.length()));
        System.out.println(String.format("Body size: %d", body.length()));
        System.out.println(String.format("Port: %d\n", clientSocket.getPort()));
        System.out.print(headers);
        System.out.println("\r");
        System.out.println(body);
        System.out.println(String.format("----------------------------------------------------------------\n"));

        // Write response
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 200 OK\r\n");
        response.append("Connection: close\r\n");
        response.append(String.format("Content-Length: %d\r\n", headers.length()+body.length()+2));
        response.append(String.format("Content-Type: %s\r\n", "text/plain"));
        response.append(String.format("\r\n%s\r\n%s", headers, body));


        clientWriter.print(response.toString());
        clientWriter.flush();

        clientInputReader.close();
        clientWriter.close();

        System.out.println(String.format("Connection on port %d closed", clientSocket.getPort()));
    }

    private int extractContentLength(String line){
        Scanner scanner = new Scanner(line);
        scanner.useDelimiter(" ");
        try {
            scanner.next();
            return scanner.nextInt();
        } catch (InputMismatchException e){
            return 0;
        }
    }

    private String readHeaders(BufferedReader reader) throws IOException{

        String line = reader.readLine();
        StringBuilder retval = new StringBuilder();

        // Read headers
        while(line != null && !line.isEmpty()){

            if(line.toLowerCase().startsWith("content-length: ")){
                contentLength = extractContentLength(line);
            }

            retval.append(String.format("%s\r\n", line));
            line = reader.readLine();
        }

        return retval.toString();
    }

    private String readBody(BufferedReader clientInputReader, int length) throws IOException{


        if(length <= 0){
            return "";
        }

        String line;

        char[] bodyBytes = new char[length];
        int bytesRead = clientInputReader.read(bodyBytes);

        if(bytesRead <= 0){
            return "";
        }

        line = new String(bodyBytes);

        return line;
    }
}
