package com.mycompany.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.InputMismatchException;
import java.util.Scanner;

public class RequestServicerOk implements Runnable {

    private Socket clientSocket;
    private int contentLength;
    private int connectionCount;

    public RequestServicerOk(Socket clientSocket, int connectionCount){
        this.clientSocket = clientSocket;
        this.connectionCount = connectionCount;
        contentLength = 0;
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
        finally{
            scanner.close();
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

    private void printOutResponse(StringBuilder response){
        System.out.println("\nResponse to Send:");
        System.out.println(response.toString()+"\n");
        System.out.println("End of response to send\n");
    }

    public void run(){
        if(clientSocket == null){
            return;
        }

        Instant now = Instant.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss - MMM dd, yy").withZone(ZoneId.of("America/Chicago"));

        try{
            // Create read and write streams
            InputStreamReader clientInput = new InputStreamReader(clientSocket.getInputStream());
            BufferedReader clientInputReader = new BufferedReader(clientInput);
            PrintWriter clientWriter = new PrintWriter(clientSocket.getOutputStream());

            // initialize
            contentLength = 0;

            String headers = readHeaders(clientInputReader);
            String body = readBody(clientInputReader, contentLength);

            System.out.println(String.format("\nServicing request number %d", connectionCount));
            System.out.println(String.format("Connection on port %d opened", clientSocket.getPort()));
            System.out.println(String.format("\nRequest details:\n%s", formatter.format(now)));
            System.out.println(String.format("This string came from %s", clientSocket.getInetAddress().getHostName()));
            System.out.println(String.format("IP address of remote %s", clientSocket.getInetAddress().getHostAddress()));
            System.out.println(String.format("Headers size: %d bytes", headers.length()));
            System.out.println(String.format("Body size: %d bytes", body.length()));
            System.out.println(String.format("Port: %d\n\nThe HTTP request:", clientSocket.getPort()));
            System.out.print(headers);
            System.out.println("\r");
            System.out.println(body);
            System.out.println(String.format("End of the request\n"));

            // Write response
            StringBuilder response = new StringBuilder();
            response.append("HTTP/1.1 200 Ok\r\n");
            response.append("Access-Control-Allow-Origin: *\n");
            response.append("Access-Control-Allow-Headers: *\n");
            response.append("Access-Control-Allow-Methods: POST, GET, OPTIONS\n");
            response.append("Connection: close\r\n");
            response.append(String.format("Content-Length: %d\r\n", headers.length()+body.length()+2));
            response.append(String.format("Content-Type: %s\r\n", "text/plain"));
            response.append(String.format("\r\n%s\r\n%s", headers, body));

            printOutResponse(response);

            // Write response to client socket and flush
            clientWriter.print(response.toString());
            clientWriter.flush();

            clientInputReader.close();
            clientWriter.close();

            System.out.println(String.format("Connection on port %d closed", clientSocket.getPort()));
            System.out.println(String.format("End of request number %d", connectionCount));
            System.out.println("\n==================================================================");
        }
        catch(IOException e){
            System.out.println("There was an IOException:\n" + e.getMessage());
        }
    }
}