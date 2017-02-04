package loganalyzer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LogAnalyzer {

    public static void main(String[] args) throws URISyntaxException, IOException {
        String path = "c:/dev/proj/loganalyzer/logs";

        List<Path> filePaths = Files.find(Paths.get(path), 1, (path_, attr) -> String.valueOf(path_).endsWith(".log")
        ).filter(Files::isRegularFile).collect(Collectors.toList());

        StringBuilder summary = new StringBuilder();


        for (Path filePath : filePaths) {

            String filename = filePath.toString();
            String headline = "LogAnalyzer analyzes file '" + filename + "'";
            System.out.println(headline);
            summary.append("===========================================\n");
            summary.append(filename.substring(filename.lastIndexOf('\\') + 1)).append("\n");
            summary.append("===========================================\n");

            File file = new File("/" + filename);

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
                            errors.put("no Exception", new Error("[no exception]", "[no stack trace]", currentLine -
                                    1));

                        } else {
                            noExceptionError.addLineNumber(currentLine - 1);
                        }
                        continue;
                    }

                    currentLine++;
                    String exception = lines.get(currentLine);
                    Error error = errors.get(exception);
                    if (error == null) {
                        int exceptionLine = currentLine;
                        StringBuilder stacktrace = new StringBuilder();
                        String line = lines.get(currentLine);
                        while (!"".equals(line) && currentLine + 1 < lines.size()) {
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

            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename + "-summary.txt"))) {
                writer.write("Log analysis from " + filename + "\n\n");
                writer.write("Errors: " + errorCount + "\n");
                errors.forEach((k, v) -> {
                    try {
                        writer.write(v.lineNumbers.size() + ": " + k + "\n");
                        writer.write("Occurrences: " + v.lineNumbers.stream().map(Object::toString).collect
                                (Collectors.joining(", ")) + "\n");
                        writer.write(v.stacktrace + "\n\n");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            summary.append("Errors: ").append(errorCount).append("\n");
            errors.forEach((k, v) -> {
                summary.append(v.lineNumbers.size()).append(": ").append(k).append("\n");
            });
            summary.append("\n");
        }

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path + "/Log-analysis-summary.txt"))) {
            writer.append(summary);
        }

        System.out.println(summary.toString());

    }
}
