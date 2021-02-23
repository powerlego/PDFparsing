package Scraping;

import Containers.Employees;
import Scraping.Dashboard.DashboardProcessing;
import Scraping.Login.LoginProcessing;
import Utils.Utils;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.javascript.SilentJavaScriptErrorListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The main class
 *
 * @author Nicholas Curl
 */
public class Scraper {

    /**
     * Utils instance
     */
    private final Utils utils = new Utils();

    /**
     * The main executing function
     *
     * @param args The command line arguments
     *
     * @throws IOException          If an IO problem occurs
     * @throws InterruptedException If the current thread is interrupted by another thread while it is waiting, then the
     *                              wait is ended and an InterruptedException is thrown.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        Scraper scraper = new Scraper();
        scraper.scrape();
    }

    /**
     * Wrapper function to run the PAFDownloader
     *
     * @throws IOException          If an IO problem occurs
     * @throws InterruptedException If the current thread is interrupted by another thread while it is waiting, then the
     *                              wait is ended and an InterruptedException is thrown.
     */
    public void scrape() throws IOException, InterruptedException {
        utils.clearScreen();
        Employees employees = new Employees();
        WebClient client = new WebClient();
        //Sets the Webclient options
        client.getOptions().setJavaScriptEnabled(true);
        client.getOptions().setThrowExceptionOnFailingStatusCode(false);
        client.getOptions().setPrintContentOnFailingStatusCode(false);
        client.setCssErrorHandler(new SilentCssErrorHandler());
        client.setJavaScriptErrorListener(new SilentJavaScriptErrorListener());
        LoginProcessing loginProcessing = new LoginProcessing();
        DashboardProcessing dashboardProcessing = new DashboardProcessing();
        //Hardcoded path to save the processed PAFs
        Path output = Paths.get("C:/HR/Paycom Data Scrape/PAFs/employees");
        if (output.toFile().exists()) {
            utils.deleteDirectory(output.toFile());
        }
        Files.createDirectories(output);
        //logs in
        loginProcessing.login(client);
        //processes the PAFs
        dashboardProcessing.finalDashboard(client, output, employees);
    }

}
