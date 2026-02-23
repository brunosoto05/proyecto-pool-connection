import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PoolManager {

    private ConnectionPool pool;
    private Config config;
    private int maxPoolSize;
    private int minPoolSize;

    public PoolManager(Config config) {
        this.config = config;
        this.pool = ConnectionPool.getInstance(config);
        this.maxPoolSize = config.getPoolSize() * 2;
        this.minPoolSize = config.getPoolSize() / 2;
    }

    public Connection get() {
        Connection conn = pool.getConnection();
        if (conn == null) {
            System.out.println("Pool vacio, intentando crecer...");
            grow();
            conn = pool.getConnection();
        }
        return conn;
    }

    public void release(Connection connection) {
        if (connection != null) {
            pool.returnConnection(connection);
            if (pool.getAvailableCount() > maxPoolSize) {
                shrink();
            }
        }
    }

    public void grow() {
        int currentSize = pool.getAvailableCount();
        if (currentSize >= maxPoolSize) {
            System.out.println("El pool ya está en su tamaño maximo (" + maxPoolSize + ")");
            return;
        }
        int growAmount = config.getPoolSize() / 10;
        if (growAmount < 1) growAmount = 1;

        System.out.println("Creciendo pool en " + growAmount + " conexiones...");
        int added = 0;
        for (int i = 0; i < growAmount; i++) {
            try {
                Connection newConn = DriverManager.getConnection(
                    config.getConnectionUrl(),
                    config.getUser(),
                    config.getPassword()
                );
                pool.addConnection(newConn);
                added++;
            } catch (SQLException e) {
                System.err.println("Error al crear conexion en grow(): " + e.getMessage());
            }
        }
        System.out.println("Pool creció " + added + " conexiones, total disponible: " + pool.getAvailableCount());
    }

    public void shrink() {
        int currentSize = pool.getAvailableCount();
        if (currentSize <= minPoolSize) {
            System.out.println("El pool ya está en su tamaño minimo (" + minPoolSize + ")");
            return;
        }
        int shrinkAmount = config.getPoolSize() / 10;
        if (shrinkAmount < 1) shrinkAmount = 1;

        System.out.println("Reduciendo pool en " + shrinkAmount + " conexiones...");
        int removed = 0;
        for (int i = 0; i < shrinkAmount; i++) {
            Connection conn = pool.removeConnection();
            if (conn != null) {
                try {
                    conn.close();
                    removed++;
                } catch (SQLException e) {
                    System.err.println("Error al cerrar conexion en shrink(): " + e.getMessage());
                }
            }
        }
        System.out.println("Pool redujo " + removed + " conexiones, total disponible: " + pool.getAvailableCount());
    }

    public void shutdown() {
        pool.shutdownPool();
    }
}