import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import io.github.cdimascio.dotenv.Dotenv;

import it.eforhum.emailModul.utils.SmtpUtils;

public class SmtpUtilsTest {

    @Test
    public void TestSmtpAuthenticate() {
        SmtpUtils smtpUtils = new SmtpUtils();
        String username = Dotenv.load().get("gmail_username");
        String app_password = Dotenv.load().get("gmail_app_password");
        try {
            smtpUtils.connectImlicit("smtp.gmail.com", 465)
            .helo("example.com")
            .authenticate(username, app_password)
            .sendMail(String.format("%s@gmail.com",username), "Test", "Test email from SmtpUtilsTest");
            smtpUtils.quit();
        } catch (IOException e) {
            e.printStackTrace();
            fail("IOException: " + e.getMessage());
        } finally {
            try {
                smtpUtils.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test(expected = IllegalStateException.class)
    public void TestUpgradeToSSLWhenAlreadySSL() throws IOException {
        SmtpUtils smtpUtils = new SmtpUtils();
        try {
            smtpUtils.connectImlicit("smtp.gmail.com", 465)
            .upgradeToSSL();
        } finally {
            smtpUtils.close();
        }
    }
}