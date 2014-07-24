package de.bluekiwi.labs.sio.statistics;

import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionListener.class);
//    private Connection con = null;
//    private ComboPooledDataSource cpds = null;

    public void contextInitialized(ServletContextEvent e) {
        Properties properties = new Properties();

        // Prepare connection to DB.
        try {
            // use this and nothing else to get application.properties
            InputStream in = this.getClass().getClassLoader()
                    .getResourceAsStream("application.properties");
            properties.load(in);
            in.close();

//            Class.forName("com.mysql.jdbc.Driver");

            String jdbcUrl = "jdbc:mysql://"
                    + properties.getProperty("mysql.host") + ":"
                    + properties.getProperty("mysql.port") + "/"
                    + properties.getProperty("mysql.db") + "?user="
                    + properties.getProperty("mysql.user") + "&password="
                    + properties.getProperty("mysql.password");
            
//            cpds = new ComboPooledDataSource();
//            
//            cpds.setDriverClass("com.mysql.jdbc.Driver");
//            cpds.setJdbcUrl(jdbcUrl);
//            cpds.setUser(properties.getProperty("mysql.user"));
//            cpds.setPassword(properties.getProperty("mysql.password"));
//            cpds.setMinPoolSize(3);
//            cpds.setMaxPoolSize(10);
//            cpds.setMaxStatements(200);
//            cpds.setMaxIdleTime(28000); // SELECT @@GLOBAL.wait_timeout;
//            cpds.setPreferredTestQuery("SELECT 1");
//            cpds.setTestConnectionOnCheckout(true);
            
//            con = DriverManager.getConnection(jdbcUrl);
            
            e.getServletContext().setAttribute("jdbcUrl", jdbcUrl);
            logger.info("JDBC URL created.");
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
    
    public void contextDestroyed(ServletContextEvent e) {
//        try {
//            con.close();
//            logger.info("DB connection closed.");
//        } catch (SQLException e1) {
//            e1.printStackTrace();
//        }
    }

}
