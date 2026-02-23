public class SimulationResult {

    private String mode;
    private int totalSamples;
    private int successCount;
    private int failureCount;
    private long totalTimeMs;
    private int totalRetries;

    public SimulationResult(String mode, int totalSamples) {
        this.mode = mode;
        this.totalSamples = totalSamples;
        this.successCount = 0;
        this.failureCount = 0;
        this.totalTimeMs = 0;
        this.totalRetries = 0;
    }

    public synchronized void registerSuccess(int retries) {
        successCount++;
        totalRetries += retries;
    }

    public synchronized void registerFailure(int retries) {
        failureCount++;
        totalRetries += retries;
    }

    public void setTotalTime(long totalTimeMs) {
        this.totalTimeMs = totalTimeMs;
    }

    public double getAverageRetries(){
      if(totalSamples == 0){
        return 0;
      }
      return (double) totalRetries / totalSamples;
    }

    public double getSuccessPercentage(){
      return (successCount * 100.0) / totalSamples;
    }

    public double getFailurePercentage(){
      return (failureCount * 100.0) / totalSamples;
    }

    public void printSummary() {
        System.out.println("==============================");
        System.out.println("Simulacion: " + mode);
        System.out.println("Tiempo total: " + totalTimeMs + "ms");
        System.out.println("Exitosas: " + successCount + " (" + String.format("%.1f", getSuccessPercentage()) + "%)");
        System.out.println("Fallidas: " + failureCount + " (" + String.format("%.1f", getFailurePercentage()) + "%)");
        System.out.println("Promedio de reintentos: " + getAverageRetries());
        System.out.println("==============================");
    }
}