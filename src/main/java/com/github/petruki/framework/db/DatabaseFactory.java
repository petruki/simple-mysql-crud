package com.github.petruki.framework.db;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.petruki.framework.exceptions.DBConnectionException;

public class DatabaseFactory implements Closeable {
	
	private static final Logger logger = LogManager.getLogger(DatabaseFactory.class);
	
	private static DatabaseFactory instance;
	
	private static Connection connection;
	
	private DatabaseFactory() throws DBConnectionException {
		connectToDb();
	}
	
	private static void connectToDb() 
			throws DBConnectionException {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://localhost/mydb", "root", "");
		} catch (SQLException | ClassNotFoundException e) {
			throw new DBConnectionException("Failed to connect to DB", e);
		}
	}

	public static Connection getConnection() 
			throws DBConnectionException, SQLException {
		if (instance == null) {
			instance = new DatabaseFactory();
		} else {
			if (connection.isClosed()) {
				connectToDb();
			}
		}
		return connection;
	}

	@Override
	public void close() throws IOException {
		if (connection != null) {
			try {
				connection.close();
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}
}
