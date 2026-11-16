import java.io.*;
import java.util.*;
import java.util.logging.*;

public class MarksImporter {

    private static final Logger LOGGER =
            Logger.getLogger(MarksImporter.class.getName());

    // Technical logs go to a file; users only see friendly console messages
    static {
        try {
            LOGGER.setUseParentHandlers(false);
            FileHandler fh = new FileHandler("import.log", true);
            fh.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fh);
        } catch (IOException e) {
            System.out.println("Warning: log file could not be created.");
        }
    }

    // Custom exception: unchecked, named after the business problem
    static class DuplicateStudentException extends RuntimeException {
        public DuplicateStudentException(String message) {
            super(message);
        }
    }

    // Record: log the exception object (type + cause + stack trace) to file
    static void record(String context, Exception e) {
        LOGGER.log(Level.WARNING, context + " | " + e.getClass().getName()
                + ": " + e.getMessage(), e);
    }

    // `throws IOException` = declaration: callers must be prepared (checked).
    // DECISION: skip an invalid record and continue with the rest.
    static void importMarks(String filename) throws IOException {
        Set<String> seen = new HashSet<>();

        // try-with-resources: the BufferedReader is closed automatically when
        // the block ends, whether it finishes normally or an exception occurs.
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            int lineNo = 0;
            while ((line = br.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) continue;
                try {
                    String[] parts = line.split(",");

                    // TODO 1: exactly 2 fields (fail fast)
                    if (parts.length != 2) {
                        throw new IllegalArgumentException(
                                "Expected 2 fields but found " + parts.length);
                    }
                    String id = parts[0].trim();

                    // TODO 2: parse mark and validate range 0..100
                    int mark = Integer.parseInt(parts[1].trim());
                    if (mark < 0 || mark > 100) {
                        throw new IllegalArgumentException(
                                "Mark " + mark + " is outside 0..100");
                    }

                    // TODO 3: detect repeated student ID
                    if (!seen.add(id)) {
                        throw new DuplicateStudentException(
                                "Student number already exists: " + id);
                    }

                    // TODO 4: display accepted record
                    System.out.println("Accepted: " + id + " -> " + mark);

                } catch (NumberFormatException e) { // specific before parent
                    record("Line " + lineNo, e);
                    System.out.println("Line " + lineNo
                            + " skipped: the mark is not a valid number.");
                } catch (IllegalArgumentException e) {
                    record("Line " + lineNo, e);
                    System.out.println("Line " + lineNo
                            + " skipped: invalid record (" + e.getMessage() + ").");
                } catch (DuplicateStudentException e) {
                    record("Line " + lineNo, e);
                    System.out.println("Line " + lineNo
                            + " skipped: student number already exists.");
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            importMarks("absent.txt");
        } catch (FileNotFoundException e) { // specific before IOException
            record("File problem", e);
            System.out.println("Marks file not found; check the file name.");
        } catch (IOException e) {
            record("I/O problem", e);
            System.out.println("The file could not be read. Please try again.");
        } catch (DuplicateStudentException e) {
            record("Duplicate", e);
            System.out.println("Duplicate student number found.");
        } catch (IllegalArgumentException e) {
            record("Invalid data", e);
            System.out.println("Invalid data found.");
        } catch (Exception e) { // final safety net only
            record("Unexpected", e);
            System.out.println("Something unexpected happened. Please contact support.");
        } finally {
            System.out.println("Import attempt finished");
        }
    }
}