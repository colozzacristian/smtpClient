package com.github.colozzacristian;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;

public class SmtpSession implements Closeable {

    private SmtpConnection connection;

    private BufferedReader reader;
    private BufferedWriter writer;

    SmtpSession(SmtpConnection connection) {
        this.connection = connection;
        this.reader = connection.reader;
        this.writer = connection.writer;
    }


    void setConnection(SmtpConnection connection) {
        this.connection = connection;
        this.reader = connection.reader;
        this.writer = connection.writer;
    }

    /**
     * Sends an email using the current SMTP session.
     *
     * @param sender  The email address of the sender.
     * @param rcpt    The email address of the recipient.
     * @param subject The subject of the email.
     * @param body    The body content of the email.
     * @return SmtpResponse The server's response after sending the email.
     * @throws SmtpException If any SMTP command is not accepted by the server.
     * @throws IOException   If there is an I/O error during communication.
     */
    public SmtpResponse sendMail(String sender, String rcpt, String subject, String body)
            throws SmtpException, IOException {

        SmtpResponse response;

        writer.write(String.format("MAIL FROM:<%s>\r\n", sender));
        writer.flush();
        response = new SmtpResponse(reader.readLine());
        if (response.getCode() != 250) {
            throw new SmtpException(response, "MAIL FROM command not accepted by server");
        }

        writer.write(String.format("RCPT TO:<%s>\r\n", rcpt));
        writer.flush();
        response = new SmtpResponse(reader.readLine());
        if (response.getCode() != 250) {
            throw new SmtpException(response, "RCPT TO command not accepted by server");
        }

        writer.write("DATA\r\n");
        writer.flush();
        response = new SmtpResponse(reader.readLine());
        if (response.getCode() != 354) {
            throw new SmtpException(response, "DATA command not accepted by server");
        }

        writer.write(String.format("From: %s\r\n", sender));
        writer.write(String.format("Subject: %s\r\n", subject));
        writer.write(String.format("To: %s\r\n", rcpt));
        writer.write("Content-Type: text/html; charset=UTF-8\r\n");
        writer.write(String.format("%s\r\n.\r\n", body));
        writer.flush();
        response = new SmtpResponse(reader.readLine());
        if (response.getCode() != 250) {
            throw new SmtpException(response, "Message not accepted by server");
        }

        return response;
    }

    /**
     * Resets the current SMTP session by sending the RSET command to the server.
     * It clears the current mail transaction state, may close the Session in some servers (Still to test).
     * @return
     * @throws SmtpException
     * @throws IOException
     */
    public SmtpResponse reset() throws SmtpException, IOException {

        SmtpResponse response;
        writer.write("RSET\r\n");
        writer.flush();
        response = new SmtpResponse(reader.readLine());
        if (response.getCode() != 250) {
            throw new SmtpException(response, "Error resetting the session");
        }

        return response;
    }

    /**
     * Closes the SMTP session by sending a HELO command to reset the session state.
     * It doesn't close the underlying connection.
     * @throws SmtpException
     * @throws IOException
     */
    public void close() throws SmtpException, IOException {

        SmtpResponse response = connection.helo();
        if (response.getCode() != 250) {
            throw new SmtpException(response, "Error resetting the session");
        }

        this.connection = null;
        this.reader = null;
        this.writer = null;
    }

}
