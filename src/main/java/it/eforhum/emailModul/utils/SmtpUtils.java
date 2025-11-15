package it.eforhum.emailModul.utils;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.util.Base64;

public class SmtpUtils {

    private String smtpServer;
    private int port;

  
    private Socket socket;
    private String clientDomain;
    private String username;

    private BufferedReader reader;
    private BufferedWriter writer;

    public SmtpUtils connect(String smtpServerN, int portN) throws IOException {
        socket = new Socket(smtpServerN, portN);
        this.smtpServer = smtpServerN;
        this.port = portN;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        System.out.println(reader.readLine());
        return this;
    }

    public SmtpUtils connectImlicit(String smtpServerN, int portN) throws IOException {
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        socket = (SSLSocket) factory.createSocket(smtpServerN, portN);
        this.smtpServer = smtpServerN;
        this.port = portN;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        System.out.println(reader.readLine());
        return this;
    }

    public SmtpUtils upgradeToSSL() throws IOException {
        if (socket instanceof SSLSocket) {
            throw new IllegalStateException("Connection is already SSL");
        }

        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();

        writer.write("STARTTLS\r\n");
        writer.flush();
        System.out.println(reader.readLine());

        socket = (SSLSocket) factory.createSocket(socket, smtpServer, port, false);
        ((SSLSocket)socket).startHandshake();
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        return this;
    }

    public SmtpUtils helo(String domain) throws IOException {
        writer.write(String.format("HELO %s \r\n", domain));
        writer.flush();
        System.out.println(reader.readLine());
        this.clientDomain = domain;
        return this;
    }

    public SmtpUtils authenticate(String user, String password) throws IOException {
        if (clientDomain == null) {
            throw new IllegalStateException("HELO must be sent before AUTHENTICATE");
        }
        writer.write("AUTH LOGIN\r\n");
        writer.flush();
        System.out.println(reader.readLine());
        this.username = user;
        writer.write(Base64.getEncoder().encodeToString(user.getBytes()) + "\r\n");
        writer.flush();
        System.out.println(reader.readLine());

        writer.write(Base64.getEncoder().encodeToString(password.getBytes()) + "\r\n");
        writer.flush();
        System.out.println(reader.readLine());
        return this;
    }

    public SmtpUtils sendMail(String rcpt,String subject,String body) throws IOException {
        if ( username == null ) {
            throw new IllegalStateException("HELO and AUTHENTICATE must be sent before MAIL FROM");
        }
        writer.write(String.format("MAIL FROM:<%s@%s>\r\n", username, clientDomain));
        writer.flush();
        System.out.println(reader.readLine());

        writer.write(String.format("RCPT TO:<%s>\r\n", rcpt));
        writer.flush();
        System.out.println(reader.readLine());
        writer.write("DATA\r\n");
        writer.flush();
        try {
            if(Integer.parseInt(reader.readLine().substring(0, 3)) != 354) {
                throw new IOException("DATA command not accepted by server");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        writer.write(String.format("From: %s@%s\r\n", username, clientDomain));
        writer.write(String.format("Subject: %s\r\n", subject));
        writer.write(String.format("To: %s\r\n", rcpt));

        writer.write(String.format("%s\r\n.\r\n", body));
        writer.flush();
        System.out.println(reader.readLine());

        return this;
    }

    public boolean noop() throws IOException {
        writer.write("NOOP\r\n");
        writer.flush();
        return Integer.parseInt(reader.readLine().substring(0,3)) == 250;
    }

    public void quit() throws IOException {
        writer.write("QUIT\r\n");
        writer.flush();
        reader.readLine();
        close();
    }

    public void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
            reader.close();
            writer.close();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (socket != null && !socket.isClosed()) {
            socket.close();
            reader.close();
            writer.close();
        }
    }

}
