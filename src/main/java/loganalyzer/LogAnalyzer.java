package loganalyzer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LogAnalyzer {

    public static void main(String[] args) throws URISyntaxException, IOException {
        System.out.println("LogAnalyzer analyzes file");

        File file = new File(LogAnalyzer.class.getResource("/agentservice-1-ypgly.log").toURI());

        List<String> lines = Files.readAllLines(file.toPath());

        int errorCount = 0;

        Map<String, Error> errors = new HashMap<>();

        int currentLine = 0;
        while (currentLine < lines.size()) {
            if (lines.get(currentLine).contains("ERROR")) {
                errorCount++;

                currentLine++;
                if (currentLine >= lines.size()) {
                    break;
                }
                if (!lines.get(currentLine).equals("")) {
                    Error noExceptionError = errors.get("no Exception");
                    if (noExceptionError == null) {
                        errors.put("no Exception", new Error("[no exception]", "[no stack trace]", currentLine - 1));
                    } else {
                        noExceptionError.addLineNumber(currentLine - 1);
                    }
                    continue;
                }

                currentLine ++;
                String exception = lines.get(currentLine);
                Error error = errors.get(exception);
                if (error == null) {
                    int exceptionLine = currentLine;
                    StringBuilder stacktrace = new StringBuilder();
                    String line = lines.get(currentLine);
                    while (!"".equals(line) && currentLine < exceptionLine + 150 && currentLine + 1 < lines.size()) {
                        stacktrace.append(line).append("\n");
                        currentLine++;
                        line = lines.get(currentLine);
                    }

                    errors.put(exception, new Error(exception, stacktrace.toString(), exceptionLine + 1));
                } else {
                    error.addLineNumber(currentLine + 1);
                }
            }
            currentLine++;
        }

        System.out.println("Errors: " + errorCount);
        errors.forEach((k, v) -> {
            System.out.println(v.lineNumbers.size() + ": " + k);
            System.out.println("Occurrences: " + v.lineNumbers.stream().map(Object::toString).collect(Collectors.joining
                    (", ")));
            System.out.println(v.stacktrace);
            System.out.println();
        });
    }
}
