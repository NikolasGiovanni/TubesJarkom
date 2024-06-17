package server;


import com.microsoft.sqlserver.jdbc.SQLServerException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author ASUS
 */
public class Server {
    public static final int PORT_NUM = 5000;
    
    public static void main(String[] args){
        
    }
    
    private Connection connectdb() {
        try {
            //String url = "jdbc:sqlserver://192.168.1.18:1433;databaseName=Tubes;encrypt=true;trustServerCertificate=true"; 
            //ini gabungan dari semua parameter diatas
            
            DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
            
            String driver = "jdbc:sqlserver://"; //pastiin driver .jar nya udah masuk di classpath projectnya
            String ServerName = "LAPTOP-JKUOFJAR\\SQLEXPRESS:1433;"; //(nama mesinnya(laptop)\\nama instancenya)
            String Database = "databaseName=TubesJarkom;"; //opsional ga diisi juga tetep bisa establish koneksinya
            String config ="encrypt=true;trustServerCertificate=true;loginTimeout=5;integratedSecurity=true"; //konfigurasi tambahan
            String url = driver + ServerName + Database + config; //url database
            String user  = "sa"; //ganti username
            String password = "tubesMIBD"; //ganti password
            
            Connection conn = DriverManager.getConnection(url, user, password); //mencoba connect ke database
            
            return conn; //mengembnalikan nilai connectionnya
            
        } catch (SQLServerException e) {//jika ada error pada sqlnya
            System.out.println(e.getMessage());
            return null;
        } catch (SQLException e) {//jika ada error pada sqlnya
            System.out.println(e.getMessage());
            return null;
        }
    }

    public boolean addUser(String username, String password, String namaTampilan) throws InterruptedException{
        final Server server = new Server();
        final Object lock = new Object();
        final AtomicBoolean canInsert = new AtomicBoolean(false);

        Thread t0 = new Thread() {
            @Override
            public void run() {
                Connection conn = server.connectdb();

                String checkQuery = "SELECT * FROM UserChat WHERE Username =?";
                String insertQuery = "INSERT INTO UserChat (Username, Password, NamaTampilan) VALUES (?,?,?)";

                synchronized (lock) {
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                         PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {

                        checkStmt.setString(1, username);

                        try (ResultSet rs = checkStmt.executeQuery()) {
                            if (rs.next()) {
                                // User already exists
                                canInsert.set(false);
                                return;
                            }
                        }

                        // Insert new user
                        insertStmt.setString(1, username);
                        insertStmt.setString(2, password);
                        insertStmt.setString(3, namaTampilan);

                        insertStmt.executeUpdate();
                        canInsert.set(true);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        canInsert.set(false);
                    }
                }
            }
        };
        t0.start();

        // Wait for the thread to finish
        t0.join();

        return canInsert.get();
    }

    public boolean login(String username, String password) throws InterruptedException {
        final Server server = new Server();
        final AtomicBoolean isValid = new AtomicBoolean(false);

        Thread t0 = new Thread() {
            @Override
            public void run() {
                Connection conn = server.connectdb();

                String query = "SELECT Username, Password FROM UserChat WHERE Username =? AND Password COLLATE Latin1_General_CS_AS =?";

                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, username);
                    stmt.setString(2, password);

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            isValid.set(true);
                        } else {
                            isValid.set(false);
                        }
                    }
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                    isValid.set(false);
                }
            }
        };
        t0.start();

        // Wait for the thread to finish
        t0.join();

        return isValid.get();
    }
}
