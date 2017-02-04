package loganalyzer;

import java.util.ArrayList;
import java.util.List;

public class Error {

    String exceptionMessage;
    String stacktrace;
    List<Integer> lineNumbers = new ArrayList<>();

    public Error(String exceptionMessage, String stacktrace, int lineNumber) {
        this.exceptionMessage = exceptionMessage;
        this.stacktrace = stacktrace;
        lineNumbers.add(lineNumber);
    }

    public void addLineNumber(int currentLine) {
        lineNumbers.add(currentLine);
    }
}
