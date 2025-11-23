package com.github.colozzacristian;

import java.io.IOException;
import org.junit.Test;

import static org.junit.Assert.*;
import static java.lang.String.format;
import io.github.cdimascio.dotenv.Dotenv;

public class SmtpUtilsTest {

    @Test
    public void TestSmtpAuthenticate() {
        
        String username = Dotenv.load().get("gmail_username");
        String app_password = Dotenv.load().get("gmail_app_password");
        try(SmtpConnection c = SmtpConnectionBuilder.connectSSL("smtp.gmail.com", 465, "example.com")) {
            
            SmtpSession session = c.createSession(username, app_password);
            session.sendMail(format("%s@gmail.com",username), format("%s@gmail.com",username), "Test", "<h1>Test email</h1> from SmtpUtilsTest");
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
            fail("IOException: " + e.getMessage());
        } 
    }

}