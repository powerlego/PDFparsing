import backend.Parsers;
import backend.Utils;
import org.apache.pdfbox.pdmodel.PDDocument;
import technology.tabula.Table;
import technology.tabula.writers.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * @author Nicholas Curl
 */
public class parser {

    public static void main(String[] args) throws IOException {
        Utils utils = new Utils();
        Path source = Paths.get("print.pdf");
        ;
        Path output = Paths.get("./employees");
        ;
        FileWriter fw = new FileWriter("src/out.csv");
        Scanner scanner = new Scanner(System.in);
        Parsers parsers = new Parsers();
        CSVWriter csvWriter = new CSVWriter();
        List<Table> tables = new LinkedList<>();
        boolean debug = false;

        if (args.length != 0) {
            if (args[0].equals("debug")) {
                debug = true;
            }
        } else {
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
            System.out.println("Location to store the parsed data (relative or absolute path):");
            input = scanner.nextLine();
            output = Paths.get(input.concat("/employees"));
        }
        if (output.toFile().exists()) {
            utils.deleteDirectory(output.toFile());
        }
        Files.createDirectories(output);

        PDDocument document = PDDocument.load(source.toFile());
        int numPages = document.getNumberOfPages();
        document.close();
        if (debug) {
            System.out.println("Enter Page Number:");
            int pageNum = Integer.parseInt(scanner.nextLine());
            parsers.parse(source, pageNum, tables, csvWriter, fw);
        } else {
            for (int i = 1; i < numPages + 1; i++) {
                tables = new LinkedList<>();
                String employeeName = parsers.processEmployeeName(source.toString(), i);
                System.out.print("Employee: " + employeeName + ", Page ");
                System.out.println(i);
                Path outputFilePath = output.resolve(employeeName + ".csv");
                if (outputFilePath.toFile().exists()) {
                    int dupNum = 1;
                    while (outputFilePath.toFile().exists()) {
                        outputFilePath = output.resolve(employeeName + "_" + dupNum + ".csv");
                        dupNum++;
                    }
                }
                fw = new FileWriter(outputFilePath.toFile());
                parsers.parse(source, i, tables, csvWriter, fw);
            }
        }
    }
}
