import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConnectionPool {

    private static ConnectionPool instance;
    private List<Connection> connections;
    private Config config;

    private ConnectionPool(Config config) {
        this.config = config;
        this.connections = new ArrayList<>();
        initializePool();
    }

    public static synchronized ConnectionPool getInstance(Config config) {
        if (instance == null) {
            instance = new ConnectionPool(config);
        }
        return instance;
    }

    private void initializePool() {
        System.out.println("Inicializando pool con " + config.getPoolSize() + " conexiones...");
        for (int i = 0; i < config.getPoolSize(); i++) {
            try {
                Connection conn = DriverManager.getConnection(
                    config.getConnectionUrl(),
                    config.getUser(),
                    config.getPassword()
                );
                connections.add(conn);
            } catch (SQLException e) {
                System.err.println("Error al crear conexion " + i + ": " + e.getMessage());
            }
        }
        System.out.println("Pool inicializado con " + connections.size() + " conexiones");
    }

    public synchronized Connection getConnection() {
        if (connections.isEmpty()) {
            return null;
        }
        return connections.remove(connections.size() - 1);
    }

    public synchronized void returnConnection(Connection connection) {
        if (connection != null) {
            connections.add(connection);
        }
    }

    public synchronized void addConnection(Connection connection) {
        if (connection != null) {
            connections.add(connection);
        }
    }

    public synchronized Connection removeConnection() {
        if (connections.isEmpty()) {
            return null;
        }
        return connections.remove(connections.size() - 1);
    }

    public synchronized int getAvailableCount() {
        return connections.size();
    }

    public synchronized void shutdownPool() {
        System.out.println("Cerrando pool de conexiones...");
        for (Connection conn : connections) {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexion: " + e.getMessage());
            }
        }
        connections.clear();
        instance = null;
        System.out.println("Pool cerrado correctamente");
    }
}