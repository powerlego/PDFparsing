package Scraping;

import Containers.Employees;
import Scraping.Dashboard.DashboardProcessing;
import Scraping.Login.LoginProcessing;
import Utils.Utils;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.javascript.SilentJavaScriptErrorListener;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Nicholas Curl
 */
public class Scraper {

    private final Utils utils = new Utils();

    public Scraper() {
        CookieHandler.setDefault(new CookieManager());
    }

    public static void main(String[] args) throws Exception {
        Scraper scraper = new Scraper();
        //checks the command line argument
        if (args.length != 0) {
            if (args[0].equals("debug")) {
                scraper.scrape(true);
            }
        } else {
            scraper.scrape(false);
        }
    }

    public void scrape(boolean debug) throws Exception {
        Employees employees = new Employees();
        WebClient client = new WebClient();
        client.getOptions().setJavaScriptEnabled(true);
        client.getOptions().setThrowExceptionOnFailingStatusCode(false);
        client.getOptions().setPrintContentOnFailingStatusCode(false);
        client.setCssErrorHandler(new SilentCssErrorHandler());
        client.setJavaScriptErrorListener(new SilentJavaScriptErrorListener());
        LoginProcessing loginProcessing = new LoginProcessing(debug);
        DashboardProcessing dashboardProcessing = new DashboardProcessing();
        Path output = Paths.get("./employees");
        if (output.toFile().exists()) {
            utils.deleteDirectory(output.toFile());
        }
        Files.createDirectories(output);

        loginProcessing.login(client);
        dashboardProcessing.finalDashboard(client, output, employees);
        //dashboardProcessing.finalDashboard(response);
    }

}
