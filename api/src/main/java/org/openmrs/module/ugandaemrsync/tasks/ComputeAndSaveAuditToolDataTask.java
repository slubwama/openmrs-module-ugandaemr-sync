package org.openmrs.module.ugandaemrsync.tasks;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;

public class ComputeAndSaveAuditToolDataTask extends AbstractTask {
    Log log = LogFactory.getLog(ComputeAndSaveAuditToolDataTask.class);
    @Override
    public void execute() {

        String createTableQuery;
        try {
            File file = FileUtils.toFile(getClass().getClassLoader().getResource("scripts/create_audit_tool_tables.sql"));
            executeSqlScript(file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        ugandaEMRSyncService.purgeExpiredFHIRResource(new Date());
    }

    public void executeSqlScript(File file) throws Exception {

        Reader reader = new BufferedReader(new FileReader(file));
        log.info("Running script from file: " + file.getCanonicalPath());
        ScriptRunner sr = new ScriptRunner(sqlConnection());
        sr.setAutoCommit(true);
        sr.setStopOnError(true);
        sr.runScript(reader);
        log.info("Done.");
    }

    public static Connection sqlConnection() throws SQLException, ClassNotFoundException {

        Properties props = new Properties();
        props.setProperty("driver.class", "com.mysql.jdbc.Driver");
        props.setProperty("driver.url", Context.getRuntimeProperties().getProperty("connection.url"));
        props.setProperty("user", Context.getRuntimeProperties().getProperty("connection.username"));
        props.setProperty("password", Context.getRuntimeProperties().getProperty("connection.password"));
        return getDatabaseConnection(props);
    }

    public static Connection getDatabaseConnection(Properties props) throws ClassNotFoundException, SQLException {

        String driverClassName = props.getProperty("driver.class");
        String driverURL = props.getProperty("driver.url");
        String username = props.getProperty("user");
        String password = props.getProperty("password");
        Class.forName(driverClassName);
        return DriverManager.getConnection(driverURL, username, password);
    }
}