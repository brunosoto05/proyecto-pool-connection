/*import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;*/

public class PoolManager {
  
  /*public Connection get() {
    if (pool.isEmpty()) {
        System.out.println("No hay conexionesen el pool");
        return null;
    }
    activeConnections++;
    return pool.remove(pool.size() -1)

}

public void release(Connection connection) {
    if (connection != null) {
        pool.add(connection);
        activeConnections--;
        // Evaluar si hay que ajustar el tamaño del pool
        if (pool.size() > config.getPoolSize()) {
            shrink();
        } else if (activeConnections >= pool.size()) {
            grow();
        }
    }
}

public void grow() {

    try {
        Connection newConn = DriverManager.getConnection(
            config.getConnectionUrl(),
            config.getUser(),
            config.getPassword()
        );
        pool.add(newConn);
        System.out.println("Pool creció. Tamaño actual: " + pool.size());
    } catch (SQLException e) {
        System.err.println("No se pudo hacer crecer el pool: " + e.getMessage());
    }
}

public void shrink() {

    if (!pool.isEmpty()) {
        try {
            Connection conn = pool.remove(pool.size() - 1);
            conn.close();
            System.out.println("Pool redujo. Tamaño actual: " + pool.size());
        } catch (SQLException e) {
            System.err.println("No se pudo reducir el pool: " + e.getMessage());
        }
    }
}*/
}
