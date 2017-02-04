package loganalyzer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;

public class LogAnalyzer {

    public static void main(String[] args) throws URISyntaxException, IOException {
        System.out.println("LogAnalyzer analyzes file");

        File file = new File(LogAnalyzer.class.getResource("/test.log").toURI());

        List<String> lines = Files.readAllLines(file.toPath());

        int errorCount = 0;

        int currentLine = 0;
        while (currentLine < lines.size()) {
            if (lines.get(currentLine).contains("ERROR")) {
                errorCount++;
            }
            currentLine++;
        }

        System.out.println("Errors: " + errorCount);

    }
}
