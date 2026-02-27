import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class App {

    private static final String CONFIG_PATH = "./src/config.json";
    private static final String LOG_PATH = "simulation.log";

    public static void main(String[] args) {

        Config config = Config.load(CONFIG_PATH);
        Logger logger = Logger.getInstance(LOG_PATH);
        PoolManager poolManager = new PoolManager(config);

        System.out.println("Iniciando simulador de conexiones...");
        System.out.println("Muestras: " + config.getSampleCount());
        System.out.println("Tamaño del pool: " + config.getPoolSize());

        // Simulacion RAW
        logger.logSeparator("SIMULACION RAW - " + config.getSampleCount() + " hilos");
        SimulationResult rawResult = runSimulation(SimulationMode.RAW, config, poolManager, logger);
        logger.logSummary(rawResult);
        rawResult.printSummary();

        // Simulacion POOLED
        logger.logSeparator("SIMULACION POOLED - " + config.getSampleCount() + " hilos");
        SimulationResult pooledResult = runSimulation(SimulationMode.POOLED, config, poolManager, logger);
        logger.logSummary(pooledResult);
        pooledResult.printSummary();

        // Analisis comparativo final
        printComparison(rawResult, pooledResult);
        logger.logSeparator("ANALISIS COMPARATIVO");
        logComparison(rawResult, pooledResult, logger);

        // Cierre de recursos
        poolManager.shutdown();
        logger.close();

        System.out.println("Simulacion finalizada");
    }

    private static SimulationResult runSimulation(SimulationMode mode, Config config, PoolManager poolManager, Logger logger) {

        int sampleCount = config.getSampleCount();
        SimulationResult result = new SimulationResult(mode.toString(), sampleCount);

        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(sampleCount);

        for (int i = 0; i < sampleCount; i++) {
            String sampleId = String.format("Sample-%04d", i + 1);
            ConnectionThread thread = new ConnectionThread(
                sampleId, mode, config, poolManager, result, logger, startLatch
            );
            executor.submit(thread);
        }

        long startTime = System.currentTimeMillis();
        startLatch.countDown();
        executor.shutdown();

        try {
            boolean finished = executor.awaitTermination(
                config.getTimeoutSeconds(), TimeUnit.SECONDS
            );
            if (!finished) {
                System.err.println("TIMEOUT: La simulacion " + mode + " fue detenida por exceder el tiempo limite");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.err.println("Simulacion interrumpida: " + e.getMessage());
            executor.shutdownNow();
        }

        long endTime = System.currentTimeMillis();
        result.setTotalTime(endTime - startTime);

        return result;
    }

    private static void printComparison(SimulationResult raw, SimulationResult pooled) {
        String winner = pooled.getSuccessCount() > raw.getSuccessCount() ? "POOLED" : "RAW";

        System.out.println();
        System.out.println("--- ANALISIS COMPARATIVO ---");
        System.out.println();
        System.out.println("Tiempo total:");
        System.out.println("  RAW:    " + raw.getTotalTimeMs() + "ms");
        System.out.println("  POOLED: " + pooled.getTotalTimeMs() + "ms");
        System.out.println();
        System.out.println("Exitosas:");
        System.out.println("  RAW:    " + raw.getSuccessCount() + " (" + String.format("%.1f", raw.getSuccessPercentage()) + "%)");
        System.out.println("  POOLED: " + pooled.getSuccessCount() + " (" + String.format("%.1f", pooled.getSuccessPercentage()) + "%)");
        System.out.println();
        System.out.println("Fallidas:");
        System.out.println("  RAW:    " + raw.getFailureCount() + " (" + String.format("%.1f", raw.getFailurePercentage()) + "%)");
        System.out.println("  POOLED: " + pooled.getFailureCount() + " (" + String.format("%.1f", pooled.getFailurePercentage()) + "%)");
        System.out.println();
        System.out.println("Promedio de reintentos:");
        System.out.println("  RAW:    " + String.format("%.2f", raw.getAverageRetries()));
        System.out.println("  POOLED: " + String.format("%.2f", pooled.getAverageRetries()));
        System.out.println();
        System.out.println("Mejor metodo: " + winner);
        System.out.println();
    }

    private static void logComparison(SimulationResult raw, SimulationResult pooled, Logger logger) {
        logger.logSummary(raw);
        logger.logSummary(pooled);
        String winner = pooled.getSuccessCount() > raw.getSuccessCount() ? "POOLED" : "RAW";
        logger.log("COMPARACION", "SISTEMA", "Mejor metodo: " + winner, 0);
    }
}
