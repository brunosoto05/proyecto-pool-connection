import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    private static Logger instance;
    private PrintWriter writer;
    private static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern("HH:mm:ss");

    private Logger(String logFilePath) {
        // hay que decidir si el archivo log se sobreescribe cada vez
        // o si se acumula con cada ejecucion (append true/false)
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

    public synchronized void log(String sampleId, String status, int retries) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String line = "[" + timestamp + "] " + sampleId + " → " + status + " (reintentos: " + retries + ")";
        writer.println(line);
        writer.flush();
    } 

    public void close() {
        if (writer != null) {
            writer.close();
        }
    }
}