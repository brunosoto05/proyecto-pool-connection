import com.google.gson.Gson;
import java.io.FileReader;
import java.io.IOException;

public class Config {

    // Estos son los mismos atributos del config.json
    private String host;
    private int port;
    private String database;
    private String user;
    private String password;
    private String query;
    private int sampleCount;
    private int maxRetries;
    private int poolSize;
    private int timeoutSeconds;

    /* Lee el archivo config.json y lo convierte en un objeto Config. Gson hace todo lo demas mapea cada campo del JSON a cada atributo de cada uno.*/
    private Config() {}
    public static Config load(String filePath) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, Config.class);
        } catch (IOException e) {
            System.err.println("Error leyendo el archivo de configuración: " + e.getMessage());
            System.exit(1);
            return null;
        }
    }

    public String getConnectionUrl() {
        return "jdbc:postgresql://" + host + ":" + port + "/" + database;
    }

    public String getUser(){
      return user;
    }
    public String getPassword(){
      return password; 
    }
    public String getQuery(){ 
      return query; 
    }
    public int getSampleCount(){
      return sampleCount; 
    }
    public int getMaxRetries(){
      return maxRetries; 
    }
    public int getPoolSize(){
      return poolSize; 
    }
    public int getTimeoutSeconds(){
      return timeoutSeconds; 
    }
}