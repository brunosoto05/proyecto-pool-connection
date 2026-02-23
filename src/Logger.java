import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    private static Logger instance;
    private PrintWriter writer;
    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private Logger(String logFilePath) {
        try {
            writer = new PrintWriter(new FileWriter(logFilePath, true));
        } catch (IOException e) {
            System.err.println("No se pudo crear el archivo log: " + e.getMessage());
            System.exit(1);
        }
    }

    public static Logger getInstance(String logFilePath) {
        if (instance == null) {
            instance = new Logger(logFilePath);
        }
        return instance;
    }

    public synchronized void log(String sampleId, String mode, String status, int retries) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String line = "[" + timestamp + "] [" + mode + "] " + sampleId +
                      " → " + status + " (reintentos: " + retries + ")";
        writer.println(line);
        writer.flush();
    }

    public synchronized void logSeparator(String title) {
        writer.println();
        writer.println("=== " + title + " ===");
        writer.println();
        writer.flush();
    }

    public synchronized void logSummary(SimulationResult result) {
        writer.println("--- RESUMEN " + result.getMode() + " ---");
        writer.println("Tiempo total: " + result.getTotalTimeMs() + "ms");
        writer.println("Exitosas: " + result.getSuccessCount() +
                       " (" + String.format("%.1f", result.getSuccessPercentage()) + "%)");
        writer.println("Fallidas: " + result.getFailureCount() +
                       " (" + String.format("%.1f", result.getFailurePercentage()) + "%)");
        writer.println("Promedio de reintentos: " +
                       String.format("%.2f", result.getAverageRetries()));
        writer.println();
        writer.flush();
    }

    public void close() {
        if (writer != null) {
            writer.println("Fin de ejecucion: " +
                          LocalDateTime.now().format(FORMATTER) + " ===");
            writer.flush();
            writer.close();
        }
    }
}