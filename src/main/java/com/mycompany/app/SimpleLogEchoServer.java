package com.mycompany.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;

public class SimpleLogEchoServer implements LogEchoServer{

    ServerSocket serverSocket;
    int contentLength = 0;

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
        return serverSocket.accept();
    }

    public void serviceConnection(Socket clientSocket) throws IOException{

        if(clientSocket == null){
            return;
        }

        // Create read and write streams
        InputStreamReader clientInput = new InputStreamReader(clientSocket.getInputStream());
        BufferedReader clientInputReader = new BufferedReader(clientInput);
        PrintWriter clientWriter = new PrintWriter(clientSocket.getOutputStream());

        // initialize
        contentLength = 0;

        String headers = readHeaders(clientInputReader);
        String body = readBody(clientInputReader, contentLength);

        System.out.print(headers);
        System.out.println("\r");
        System.out.println(body);

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

        final int MEGABYTE = 1000000000;

        if(length <= 0 || length >= 2*MEGABYTE){
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
