package com.mycompany.app;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        LogEchoServer server = new SimpleLogEchoServer();
        try {
            int port = Integer.parseInt(System.getenv("PORT"));
            server.go(port);
        }
        catch (NumberFormatException e){
            System.out.println("PORT not a number. Ending program");
        }
        catch (IOException e) {
            System.out.println("Could not start server. Details ahead:");
            e.printStackTrace();
        }
    }
}
