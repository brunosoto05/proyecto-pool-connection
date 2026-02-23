import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;

public class ConnectionThread implements Runnable {

    private String sampleId;
    private SimulationMode mode;
    private Config config;
    private PoolManager poolManager;
    private SimulationResult result;
    private Logger logger;
    private CountDownLatch startLatch;

    public ConnectionThread(String sampleId, SimulationMode mode, Config config, PoolManager poolManager, SimulationResult result, Logger logger, CountDownLatch startLatch) {
        this.sampleId = sampleId;
        this.mode = mode;
        this.config = config;
        this.poolManager = poolManager;
        this.result = result;
        this.logger = logger;
        this.startLatch = startLatch;
    }

    @Override
    public void run() {
        try {
            startLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        if (mode == SimulationMode.RAW) {
            runRaw();
        } else {
            runPooled();
        }
    }

    private void runRaw() {
        int retries = 0;
        boolean success = false;

        while (retries <= config.getMaxRetries() && !success) {
            Connection conn = null;
            Statement stmt = null;
            ResultSet rs = null;
            try {
                conn = DriverManager.getConnection(
                    config.getConnectionUrl(),
                    config.getUser(),
                    config.getPassword()
                );
                stmt = conn.createStatement();
                rs = stmt.executeQuery(config.getQuery());
                success = true;
                result.registerSuccess(retries);
                logger.log(sampleId, "RAW", "EXITOSA", retries);
            } catch (SQLException e) {
                retries++;
                if (retries > config.getMaxRetries()) {
                    result.registerFailure(retries);
                    logger.log(sampleId, "RAW", "FALLIDA", retries);
                }
            } finally {
                closeResources(rs, stmt, conn);
            }
        }
    }

    private void runPooled() {
        int retries = 0;
        boolean success = false;

        while (retries <= config.getMaxRetries() && !success) {
            Connection conn = null;
            Statement stmt = null;
            ResultSet rs = null;
            try {
                conn = poolManager.get();
                if (conn == null) {
                    retries++;
                    if (retries > config.getMaxRetries()) {
                        result.registerFailure(retries);
                        logger.log(sampleId, "POOLED", "FALLIDA", retries);
                    }
                    continue;
                }
                stmt = conn.createStatement();
                rs = stmt.executeQuery(config.getQuery());
                success = true;
                result.registerSuccess(retries);
                logger.log(sampleId, "POOLED", "EXITOSA", retries);
            } catch (SQLException e) {
                retries++;
                if (retries > config.getMaxRetries()) {
                    result.registerFailure(retries);
                    logger.log(sampleId, "POOLED", "FALLIDA", retries);
                }
            } finally {
                if (rs != null) {
                    try { rs.close(); } catch (SQLException e) { }
                }
                if (stmt != null) {
                    try { stmt.close(); } catch (SQLException e) { }
                }
                if (conn != null) {
                    poolManager.release(conn);
                }
            }
        }
    }

    private void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException e) { }
        }
        if (stmt != null) {
            try { stmt.close(); } catch (SQLException e) { }
        }
        if (conn != null) {
            try { conn.close(); } catch (SQLException e) { }
        }
    }
}