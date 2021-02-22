package Scraping.Login;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import java.util.Scanner;


/**
 * @author Nicholas Curl
 */
public class LoginProcessing {
    private boolean debug;

    public LoginProcessing(boolean debug) {
        this.debug = debug;
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
        if (debug) {
            clientcode.type("0JE77");
            username.type("NCurl1");
            password.type("PoWerlego0805!");
        } else {
            System.out.println("Enter Login Credentials:");
            System.out.print("Client Code: ");
            clientcode.type(scanner.next());
            System.out.print("Username: ");
            username.type(scanner.next());
            System.out.print("Password: ");
            password.type(scanner.next());
        }
        HtmlPage securityPage = login.click();
        HtmlForm securityForm = securityPage.getForms().get(0);
        HtmlPasswordInput fistSecurityQuestion = securityForm.getInputByName("firstSecurityQuestion");
        HtmlPasswordInput secondSecurityQuestion = securityForm.getInputByName("secondSecurityQuestion");
        HtmlButton continueButton = securityForm.getButtonByName("continue");
        if (debug) {
            fistSecurityQuestion.type(securityQuestions(fistSecurityQuestion.getAttribute("aria-label").strip()));
            secondSecurityQuestion.type(securityQuestions(secondSecurityQuestion.getAttribute("aria-label").strip()));
        } else {
            System.out.println("Answer Security Questions:");
            System.out.println(fistSecurityQuestion.getAttribute("aria-label").strip());
            String firstAnswer = scanner.nextLine();
            fistSecurityQuestion.type(firstAnswer);
            System.out.println(secondSecurityQuestion.getAttribute("aria-label").strip());
            String secondAnswer = scanner.nextLine();
            secondSecurityQuestion.type(secondAnswer);
        }
        HtmlPage homePage = continueButton.click();
    }

    private String securityQuestions(String question) {
        String answer;
        switch (question) {
            case "Name of first employer?":
                answer = "Julie";
                break;
            case "Favorite hobby in high school?":
                answer = "Programming";
                break;
            case "High school mascot?":
                answer = "Trevian";
                break;
            case "Name of first pet?":
                answer = "Casper";
                break;
            case "Father's birthplace?":
                answer = "Indiana";
                break;
            default:
                answer = "";
                break;
        }
        return answer;
    }
}
