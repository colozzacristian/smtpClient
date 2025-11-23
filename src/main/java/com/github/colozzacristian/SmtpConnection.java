package com.github.colozzacristian;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.net.Socket;
import java.util.Base64;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import java.io.Closeable;

import static  java.lang.String.format;

public class SmtpConnection implements Closeable{

    SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();

    Socket socket;
    String smtpServer;
    int port;

    BufferedReader reader;
    BufferedWriter writer;
    String localHostName;

    private SmtpSession session;

    /**
     * Upgrades the current SMTP connection to use SSL/TLS via the STARTTLS command.
     *
     * @return SmtpConnection The upgraded SMTP connection.
     * @throws IOException If there is an I/O error during the upgrade process.
     */
    public SmtpConnection upgradeToSSL() throws IOException {
        if (socket instanceof SSLSocket) {
            throw new IllegalStateException("Connection is already SSL");
        }

        writer.write("STARTTLS\r\n");
        writer.flush();
        SmtpResponse response = new SmtpResponse(reader.readLine());
        if (response.getCode() != 220) {
            throw new IOException(format("Failed to start TLS: %s", response.getMessage()));
        }

        socket = (SSLSocket) factory.createSocket(socket, smtpServer, port, false);
        ((SSLSocket)socket).startHandshake();

        if (socket == null) {
            throw new IOException("Failed to create SSL socket");
        }

        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        if (reader == null || writer == null) {
            throw new IOException("Failed to create readers an writers");
        }

        this.helo(); // Re-send HELO after upgrading to SSL ( mailtrap documentation)

        return this;
    }

    
    SmtpConnection(Socket socket, String smtpServer, int port, BufferedReader reader, BufferedWriter writer, String localHostName) {
        this.socket = socket;
        this.smtpServer = smtpServer;
        this.port = port;
        this.reader = reader;
        this.writer = writer;
        this.localHostName = localHostName;
    }

    /**
     * Sends the HELO command to the SMTP server with the specified domain.
     * @param domain The domain to use in the HELO command.
     * @return SmtpResponse
     * @throws IOException If there is an I/O error during communication.
     */
    public SmtpResponse helo(String domain) throws IOException {
        this.localHostName = domain;
        writer.write(format("HELO %s\r\n", domain));
        writer.flush();
        return new SmtpResponse(reader.readLine());
    }

    /**
     * Sends the HELO command to the SMTP server using the local host name used when the connection was established.
     * @return SmtpResponse
     * @throws IOException If there is an I/O error during communication.
     */
    public SmtpResponse helo() throws IOException {
  
        writer.write(format("HELO %s \r\n", localHostName));
        writer.flush();
        return new SmtpResponse(reader.readLine());
    }

    /**
     * Closes the SMTP connection by sending the QUIT command to the server.
     * @throws SmtpException If the server does not accept the QUIT command.
     * @throws IOException If there is an I/O error during communication.
     */
    public void close() throws SmtpException,IOException{
        SmtpResponse response;
        writer.write("QUIT\r\n");
        writer.flush();
        response = new SmtpResponse(reader.readLine());
        if(response.getCode() != 221){
            throw new SmtpException(response, "Error closing the connection");
        }
        this.session = null;
        if(!socket.isClosed()){
        socket.close();
        reader.close();
        writer.close();
        }
    }

    /**
     * Creates a new SMTP session with authentication using the provided username and password.
     *
     * @param user     The username for authentication.
     * @param password The password for authentication.
     * @return SmtpSession The created SMTP session.
     * @throws IOException   If there is an I/O error during communication.
     * @throws SmtpException If the authentication fails.
     */
    public SmtpSession createSession(String user, String password) throws IOException, SmtpException {

        SmtpResponse response;
        String b64psswd = Base64.getEncoder().encodeToString(password.getBytes());;
        String b64user = Base64.getEncoder().encodeToString(user.getBytes());
        
        writer.write("AUTH LOGIN\r\n");
        writer.flush();
        response = new SmtpResponse(reader.readLine());
        if (response.getCode() != 334){
            throw new SmtpException(response, response.getMessage());
        }
        
        writer.write(format("%s\r\n", b64user));
        writer.flush();
        response = new SmtpResponse(reader.readLine());
        if (response.getCode() != 334){
            throw new SmtpException(response, response.getMessage());
        }

        writer.write(format("%s\r\n", b64psswd));
        writer.flush();
        response = new SmtpResponse(reader.readLine());
        if (response.getCode() != 235){
            throw new SmtpException(response, response.getMessage());
        }

        if (this.session == null){
            this.session = new SmtpSession(this);
        }else{
            this.session.setConnection(this);
        }

        return this.session;
    }
    
}
