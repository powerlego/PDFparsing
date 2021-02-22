package Scraping.Login;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import java.util.Scanner;


/**
 * @author Nicholas Curl
 */
public class LoginProcessing {

    public LoginProcessing() {
    }


    public void login(WebClient client) throws Exception {
        String loginPage = "https://www.paycomonline.net/v4/cl/cl-login.php";
        HtmlPage page = client.getPage(loginPage);
        HtmlForm form = page.getFormByName("frmClLogin");
        HtmlTextInput clientcode = form.getInputByName("clientcode");
        HtmlTextInput username = form.getInputByName("username");
        HtmlPasswordInput password = form.getInputByName("password");
        HtmlSubmitInput login = form.getInputByName("login");
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter Login Credentials:");
        clientcode.type("0JE77");
        System.out.print("Username: ");
        username.type(scanner.next());
        System.out.print("Password: ");
        password.type(scanner.next());
        HtmlPage securityPage = login.click();
        HtmlForm securityForm = securityPage.getForms().get(0);
        HtmlPasswordInput fistSecurityQuestion = securityForm.getInputByName("firstSecurityQuestion");
        HtmlPasswordInput secondSecurityQuestion = securityForm.getInputByName("secondSecurityQuestion");
        HtmlButton continueButton = securityForm.getButtonByName("continue");
        System.out.println("Answer Security Questions:");
        String firstQuestion = fistSecurityQuestion.getAttribute("aria-label").strip();
        System.out.println(firstQuestion);
        String firstAnswer = scanner.next();
        fistSecurityQuestion.type(firstAnswer);
        System.out.println(secondSecurityQuestion.getAttribute("aria-label").strip());
        String secondAnswer = scanner.next();
        secondSecurityQuestion.type(secondAnswer);
        HtmlPage homePage = continueButton.click();
    }


}
