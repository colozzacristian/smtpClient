package it.eforhum;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;

public class SmtpConnectionBuilder {
    

    /**
     * Establishes a plain SMTP connection to the specified server and port.
     *
     * @param smtpServer    The SMTP server address.
     * @param port          The port number to connect to.
     * @param localHostName The local host name to use in the HELO command.
     * @return SmtpConnection The established SMTP connection.
     * @throws IOException If there is an I/O error during connection.
     */
    public static SmtpConnection connect(String smtpServer, int port, String localHostName) throws IOException {
        Socket socket = new Socket(smtpServer, port);

        return createConnection(socket,smtpServer, localHostName);
    }

    /**
     * Establishes an SSL/TLS SMTP connection to the specified server and port.
     *
     * @param smtpServer    The SMTP server address.
     * @param port          The port number to connect to.
     * @param localHostName The local host name to use in the HELO command.
     * @return SmtpConnection The established SMTP connection.
     * @throws IOException If there is an I/O error during connection.
     */
    public static SmtpConnection connectSSL(String smtpServer, int port, String localHostName) throws IOException {
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        Socket socket = (SSLSocket) factory.createSocket(smtpServer, port);
        
        return createConnection(socket, smtpServer, localHostName);
    }

    private static SmtpConnection createConnection(Socket socket, String smtpServer, String localHostName) throws IOException {
        SmtpResponse response;
        if(socket == null){
            throw new IOException("Failed to create socket");

        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        if ( reader == null || writer == null) {
            throw new IOException("Failed to create readers an writers");
        }

        response = new SmtpResponse(reader.readLine());
        if (response.getCode() != 220) {
            throw new IOException(String.format("Failed to connect to SMTP server: %s", response.getMessage()));
        }

        writer.write(String.format("HELO %s\r\n", localHostName));
        writer.flush();
        response = new SmtpResponse(reader.readLine());
        if (response.getCode() != 250) {
            throw new IOException(String.format("HELO command not accepted by server: %s", response.getMessage()));
        }

        
        return new SmtpConnection(socket, smtpServer, socket.getPort(), reader, writer, localHostName);
    }
}
