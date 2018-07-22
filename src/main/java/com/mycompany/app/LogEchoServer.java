package com.mycompany.app;

import java.io.IOException;
import java.net.Socket;

public interface LogEchoServer {
    void go(int port) throws IOException;
    Socket acceptConnection() throws IOException;
    void serviceConnection(Socket clientSocket) throws IOException;

}
