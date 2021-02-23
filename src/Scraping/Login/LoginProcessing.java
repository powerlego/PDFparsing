package Scraping.Login;

import Utils.Utils;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import java.io.IOException;
import java.util.Scanner;


/**
 * Logs the user into Paycom using their credentials
 *
 * @author Nicholas Curl
 */
public class LoginProcessing {

    /**
     * Utils instance
     */
    private final Utils utils = new Utils();

    /**
     * Constructor for class
     */
    public LoginProcessing() {
    }

    /**
     * Logs into Paycom
     *
     * @param client The Webclient
     *
     * @throws IOException          If an IO problem occurs
     * @throws InterruptedException If the current thread is interrupted by another thread while it is waiting, then the
     *                              wait is ended and an InterruptedException is thrown.
     */
    public void login(WebClient client) throws IOException, InterruptedException {
        //Paycom client log-in page
        String loginPage = "https://www.paycomonline.net/v4/cl/cl-login.php";
        HtmlPage page = client.getPage(loginPage);
        HtmlForm form = page.getFormByName("frmClLogin");
        HtmlTextInput clientcode = form.getInputByName("clientcode");
        HtmlTextInput username = form.getInputByName("username");
        HtmlPasswordInput password = form.getInputByName("password");
        HtmlSubmitInput login = form.getInputByName("login");
        Scanner scanner = new Scanner(System.in);
        //Prompts user to enter login credentials
        System.out.println("Enter Login Credentials:");
        clientcode.type("0JE77");
        System.out.print("Username: ");
        username.type(scanner.next());
        System.out.print("Password: ");
        password.type(scanner.next());
        //Clears the screen to hide entered information
        utils.clearScreen();
        HtmlPage securityPage = login.click();
        //Checks to see if login was successful
        if (!securityPage.getUrl().toString().contains("security/security-question/login")) {
            System.out.println("Incorrect login credentials. Please wait until prompted to try again.");
            this.login(client);
        }
        HtmlForm securityForm = securityPage.getForms().get(0);
        HtmlPasswordInput fistSecurityQuestion = securityForm.getInputByName("firstSecurityQuestion");
        HtmlPasswordInput secondSecurityQuestion = securityForm.getInputByName("secondSecurityQuestion");
        HtmlButton continueButton = securityForm.getButtonByName("continue");
        //Prompts the user to answer security questions
        System.out.println("Answer Security Questions:");
        String firstQuestion = fistSecurityQuestion.getAttribute("aria-label").strip();
        System.out.println(firstQuestion);
        String firstAnswer = scanner.next();
        fistSecurityQuestion.type(firstAnswer);
        System.out.println(secondSecurityQuestion.getAttribute("aria-label").strip());
        String secondAnswer = scanner.next();
        secondSecurityQuestion.type(secondAnswer);
        //Clears the screen to hide entered information
        utils.clearScreen();
        HtmlPage homePage = continueButton.click();
        //Checks to see if fully logged in
        if (homePage.getUrl().toString().contains("cl-menu.php")) {
            System.out.println("Login in successful.  Please let the program continue running.");
        } else {
            System.out.println("Login unsuccessful. Please wait until prompted to try again");
            this.login(client);
        }

    }
}
