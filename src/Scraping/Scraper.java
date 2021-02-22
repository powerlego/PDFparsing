package Scraping;

import Containers.Employees;
import Scraping.Dashboard.DashboardProcessing;
import Scraping.Login.LoginProcessing;
import Utils.Utils;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.javascript.SilentJavaScriptErrorListener;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Nicholas Curl
 */
public class Scraper {

    private final Utils utils = new Utils();

    public Scraper() {
    }

    public static void main(String[] args) throws Exception {
        Scraper scraper = new Scraper();
        scraper.scrape();
    }

    public void scrape() throws Exception {
        Employees employees = new Employees();
        WebClient client = new WebClient();
        client.getOptions().setJavaScriptEnabled(true);
        client.getOptions().setThrowExceptionOnFailingStatusCode(false);
        client.getOptions().setPrintContentOnFailingStatusCode(false);
        client.setCssErrorHandler(new SilentCssErrorHandler());
        client.setJavaScriptErrorListener(new SilentJavaScriptErrorListener());
        LoginProcessing loginProcessing = new LoginProcessing();
        DashboardProcessing dashboardProcessing = new DashboardProcessing();
        Path output = Paths.get("Y:/HR/Paycom Data Scrape/PAFs/employees");

        if (output.toFile().exists()) {
            utils.deleteDirectory(output.toFile());
        }
        Files.createDirectories(output);

        loginProcessing.login(client);
        dashboardProcessing.finalDashboard(client, output, employees);
    }

}
