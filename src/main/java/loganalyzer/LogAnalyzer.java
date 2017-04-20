package loganalyzer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LogAnalyzer {

    public static void main(String[] args) throws URISyntaxException, IOException {
        String path = "c:/dev/logs";

        List<Path> filePaths = Files.find(Paths.get(path), 2, (path_, attr) -> String.valueOf(path_).contains(".log")
        ).filter(Files::isRegularFile).collect(Collectors.toList());

        StringBuilder summary = new StringBuilder();
        summary.append("Datum");
        summary.append("|");
        summary.append("Datei");
        summary.append("|");
        summary.append("Anzahl");
        summary.append("|");
        summary.append("Vorkommen");
        summary.append("|");
        summary.append("Error / Exception");
        summary.append("|");
        summary.append("Einstufung");
        summary.append("|");
        summary.append("Status");
        summary.append("\n");

        for (Path filePath : filePaths) {

            String filename = filePath.toString();
            String headline = "LogAnalyzer analyzes file '" + filename + "'";
            System.out.println(headline);

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
                        String errorLine = lines.get(currentLine - 1);
                        String errorString = errorLine.substring(errorLine.indexOf(":  ") + 3);
                        // replace similar error strings with an unique error string to group them together
                        if (errorString.contains("Datei") && errorString.contains("wurde nicht gefunden")) {
                            errorString = "Datei [Dateiname] wurde nicht gefunden";
                        }
                        Error noExceptionError = errors.get(errorString);
                        if (noExceptionError == null) {
                            errors.put(errorString, new Error(errorString, "[no stack trace]", currentLine));
                        } else {
                            noExceptionError.addLineNumber(currentLine);
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
                        if (!exception.contains("Broken pipe")) {
                            errors.put(exception, new Error(exception, stacktrace.toString(), exceptionLine + 1));
                        } else {
                            errorCount--;
                        }
                    } else {
                        error.addLineNumber(currentLine + 1);
                    }
                }
                currentLine++;
            }

            // Summary-File for each log-file
//            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename + "-summary.txt"))) {
//                writer.write("Log analysis from " + filename + "\n\n");
//                writer.write("Errors: " + errorCount + "\n");
//                errors.forEach((k, v) -> {
//                    try {
//                        writer.write(v.lineNumbers.size() + ": " + k + "\n");
//                        writer.write("Occurrences: " + v.lineNumbers.stream().map(Object::toString).collect
//                            (Collectors.joining(", ")) + "\n");
//                        writer.write(v.stacktrace + "\n\n");
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                });
//            }

            if (errorCount > 0) {
                // Summary file as TXT file
                //                summary.append("===========================================\n");
                //                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                //                summary.append(sdf.format(file.lastModified())).append(" ");
                //                summary.append(filename.substring(filename.lastIndexOf('\\') + 1)).append("\n");
                //                summary.append("===========================================\n");
                //                summary.append("Errors: ").append(errorCount).append("\n");
                //                errors.forEach((k, v) -> summary.append(v.lineNumbers.size()).append(": ").append(k).append("\n"));
                //                summary.append("\n");

                // Summary file as CSV file
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                String datum = sdf.format(file.lastModified());
                String dateiname = filename.substring(filename.lastIndexOf('\\') + 1);
                errors.forEach((errorString, error) -> {
                    summary.append(datum);
                    summary.append("|");
                    summary.append(dateiname);
                    summary.append("|");
                    summary.append(error.lineNumbers.size());
                    summary.append("|");
                    summary.append(error.lineNumbers.stream().limit(15).map(String::valueOf).sorted().collect(Collectors.joining(",")));
                    summary.append("|");
                    summary.append(errorString);
                    summary.append("|");
                    summary.append("|");
                    summary.append("\n");
                });
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path + "/Log-analysis-summary.csv"))) {
            writer.append(summary);
        }
    }
}
