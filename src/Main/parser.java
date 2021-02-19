package Main;

import Containers.Employee;
import Containers.IDReport;
import Containers.PAF;
import Parsers.IDReportProcessing;
import Parsers.PAFProcessing;
import Parsers.Parsers;
import Utils.Utils;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.pdfbox.pdmodel.PDDocument;
import technology.tabula.writers.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * The main class
 *
 * @author Nicholas Curl
 */
public class parser {

    /**
     * The main function
     *
     * @param args Command line arguments
     *
     * @throws IOException            Exception for invalid file
     * @throws ParseException         Exception for invalid date parsing
     * @throws CsvValidationException Exception from reading a csv file
     */
    public static void main(String[] args) throws IOException, ParseException, CsvValidationException {
        Utils utils = new Utils();
        Path source = Paths.get("print.pdf");
        Path output = Paths.get("./employees");
        FileWriter fw = new FileWriter("src/out.csv");
        PAFProcessing pafp = new PAFProcessing();
        IDReportProcessing idrp;
        Scanner scanner = new Scanner(System.in);
        Parsers parsers = new Parsers();
        CSVWriter csvWriter = new CSVWriter();
        List<PAF> pafs = new LinkedList<>();
        TreeMap<String, Employee> employees = new TreeMap<>();
        boolean debug = false;
        //checks the command line argument
        if (args.length != 0) {
            if (args[0].equals("debug")) {
                debug = true;
            }
        } else {  //if not in debug mode run normally
            String input;
            while (true) {
                System.out.println("Location of pdf to parse (relative or absolute path): ");
                input = scanner.nextLine();
                if (!(input.contains(".pdf"))) {
                    System.out.println("Please provide a pdf file");
                } else {
                    break;
                }
            }
            source = Paths.get(input);
            System.out.println("Folder to store the parsed data (relative or absolute path):");
            input = scanner.nextLine();
            output = Paths.get(input.concat("/employees"));
        }

        if (output.toFile().exists()) {
            utils.deleteDirectory(output.toFile());
        }
        Files.createDirectories(output);
        File reportFile = null;
        if (!debug) {
            System.out.println("Folder containing the PAF Transaction ID Report (relative or absolute path):");
            String idLocation = scanner.nextLine();
            System.out.println("Name of the report:");
            String reportName = scanner.nextLine();
            List<File> reports = utils.findReports(reportName, Paths.get(idLocation).toFile());
            System.out.println("Select report containing IDs (type the number and press enter): ");
            boolean selected = false;
            int selection = 0;
            while (!selected) {
                int selectionNum = 0;
                for (File report : reports) {
                    Path reportPath = Paths.get(report.getAbsolutePath());
                    reportPath = Paths.get(idLocation).toAbsolutePath().relativize(reportPath);
                    String format = "%-10s%s%n";
                    System.out.printf(format, "\t" + selectionNum + ":", reportPath.toString());
                    selectionNum++;
                }
                try {
                    selection = Integer.parseInt(scanner.nextLine());
                    try {
                        reportFile = reports.get(selection);
                        selected = true;
                    } catch (Exception e) {
                        if (e instanceof IndexOutOfBoundsException) {
                            System.out.println("Please enter a valid number.");
                        } else {
                            System.out.println(e.toString());
                        }
                    }
                } catch (Exception e) {
                    if (e instanceof NumberFormatException) {
                        System.out.println("Please enter a number.");
                    } else {
                        System.out.println(e.toString());
                    }
                }
            }
        } else {
            reportFile = Paths.get("20210218144034_PAF Parsing_63ec46f9.csv").toFile();
        }
        idrp = new IDReportProcessing(reportFile);
        IDReport report = idrp.processReport();
        PDDocument document = PDDocument.load(source.toFile());
        int numPages = document.getNumberOfPages();
        document.close();
        if (debug) {
            System.out.println("Enter Page Number:");
            int pageNum = Integer.parseInt(scanner.nextLine());
            parsers.parse(source, pageNum, csvWriter, fw, pafp, report);
        } else { //main execution for non-debug mode
            for (int i = 1; i < numPages + 1; i++) {
                String employeeName = parsers.processEmployeeName(source.toString(), i);
                System.out.print("Employee: " + employeeName + ", Page ");
                System.out.println(i);
                Files.createDirectories(output.resolve(employeeName + "/"));
                Path outputFilePath = output.resolve(employeeName + "/" + employeeName + ".csv");
                if (outputFilePath.toFile().exists()) {
                    int dupNum = 1;
                    while (outputFilePath.toFile().exists()) {
                        outputFilePath = output.resolve(employeeName + "/" + employeeName + "_" + dupNum + ".csv");
                        dupNum++;
                    }
                }
                fw = new FileWriter(outputFilePath.toFile());
                PAF paf = parsers.parse(source, i, csvWriter, fw, pafp, report);
                pafs.add(paf);
                if (employees.containsKey(employeeName)) {
                    employees.get(employeeName).addPAF(paf);
                } else {
                    String code = report.getEmployeeCode(employeeName);
                    Employee employee = new Employee(employeeName, code);
                    employee.addPAF(paf);
                    employees.put(employeeName, employee);
                }
            }
        }
    }
}
