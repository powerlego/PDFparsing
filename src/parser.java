import backend.Parsers;
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
        Path source;
        Path output;
        FileWriter fw = new FileWriter("src/out.csv");
        Scanner scanner = new Scanner(System.in);
        Parsers parsers = new Parsers();
        CSVWriter csvWriter = new CSVWriter();
        List<Table> tables = new LinkedList<>();
        boolean debug = false;
        if (args[0].equals("debug")) {
            source = Paths.get("print.pdf");
            output = Paths.get("./employees");
            debug = true;
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
                Path outputFilePath = output.resolve(employeeName + ".csv");
                fw = new FileWriter(outputFilePath.toFile());
                parsers.parse(source, i, tables, csvWriter, fw);
            }
        }
    }
}
