package droid64.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * MySQL implementation of the DaoFactory
 * @author Henrik
 *
 */
public class DaoFactoryImpl extends DaoFactory {

	private static Connection connection = null;
	private static final int CONNECTION_TIMEOUT = 30;

	private static String jdbcDriver = null;
	private static String jdbcUrl = null;
	private static String jdbcUser = null;
	private static String jdbcPassword = null;
	private static long maxRows = 25L;
	private static boolean initialized = false;

	@Override
	public DiskDao getDiskDao() {
		return new DiskDaoImpl();
	}

	/** Initialize the connection
	 *
	 * @param className class to use
	 * @param url connection URL
	 * @param user user name
	 * @param password password
	 * @param maxRows maximum number of rows to return
	 * @param jdbcLimitType the type of syntax for limit the number of returned rows
	 * @throws DatabaseException when error
	 */
	public static void initialize(String className, String url, String user, String password, long maxRows, int jdbcLimitType) throws DatabaseException {

		switch (jdbcLimitType) {
		case 1:
			limitType = LimitType.FIRST;
			break;
		case 2:
			limitType = LimitType.FETCH;
			break;
		case 0:
		default:
			limitType = LimitType.LIMIT;
			break;
		}
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {}	//NOSONAR
			connection = null;
		}
		try {
			initialized = false;
			if (className==null || className.trim().isEmpty()) {
				throw new DatabaseException("No driver class");
			}
			if (url==null || url.trim().isEmpty()) {
				throw new DatabaseException("No URL");
			}
			Class.forName (className);
			if (user!=null && !user.isEmpty()) {
				connection = DriverManager.getConnection(url, user, password);
			} else {
				connection = DriverManager.getConnection(url );
			}
			jdbcDriver = className;
			jdbcUrl = url;
			jdbcUser = user;
			jdbcPassword = password;
			DaoFactoryImpl.maxRows = maxRows;
			initialized = true;
		} catch (ClassNotFoundException | SQLException e) {
			throw new DatabaseException(e);
		}
	}

	/**
	 * Stand alone static method used to test a connection
	 * @param className name of JDBC driver class
	 * @param url of data source
	 * @param user name of user
	 * @param password the password to use
	 * @return true if connection was successful
	 */
	public static String testConnection(String className, String url, String user, String password) {
		if (className == null || className.isEmpty()) {
			return "No JDBC driver class name specified.";
		}
		if (url == null || url.isEmpty()) {
			return "No JDBC URL specified.";
		}
		try {
			Class.forName (className);
		} catch (Exception e) {	//NOSONAR
			return "Failed to load JDBC driver. " + e.getMessage();
		}
		try (var con = getConnection(url, user, password)) {
			if (con.isValid(10)) {
				return "OK";
			} else {
				return "Got connection, but validation of it failed.";
			}
		} catch (Exception e) {	//NOSONAR
			return "Failed to open connection. "+e.getMessage();
		}
	}

	private static Connection getConnection(String url, String user, String password) throws SQLException {
		if (user!=null && !user.isEmpty()) {
			return DriverManager.getConnection(url, user, password);
		} else {
			return DriverManager.getConnection(url);
		}
	}

	/**
	 * Get a connection. If current connection is not valid a new connection will be made.<br>
	 * Requires that {@link #initialize(String, String, String, String, long, int)} has been called before.
	 * @return Connection
	 * @throws DatabaseException when error
	 */
	public static Connection getConnection() throws DatabaseException {
		if (!initialized) {
			throw new DatabaseException("Database factory not initilized");
		}
		try {
			if (connection == null || !connection.isValid(CONNECTION_TIMEOUT)) {
				connect();
			}
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			connection.setAutoCommit(true);
			return connection;
		} catch (ClassNotFoundException | SQLException e) {
			throw new DatabaseException(e);
		}
	}

	private static void connect() throws DatabaseException, ClassNotFoundException, SQLException {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {}	//NOSONAR
		}
		connection = null;
		if (jdbcDriver==null || jdbcDriver.trim().isEmpty()) {
			throw new DatabaseException("No driver class");
		}
		if (jdbcUrl==null || jdbcUrl.trim().isEmpty()) {
			throw new DatabaseException("No URL");
		}
		Class.forName ("com.mysql.cj.jdbc.Driver");
		if (jdbcUser!=null && !jdbcUser.isEmpty()) {
			connection = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
		} else {
			connection = DriverManager.getConnection(jdbcUrl);
		}
	}

	/**
	 * Get prepared statement.
	 * @param sql String with SQL statement
	 * @return PreparedStatement
	 * @throws DatabaseException when error
	 */
	public static PreparedStatement prepareStatement(String sql) throws DatabaseException {
		try {
			return getConnection().prepareStatement(sql);
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	/**
	 * Get prepared statement.
	 * @param sql String with SQL statement
	 * @param autoGeneratedKeys if true primary keys will be auto generated
	 * @return PreparedStatement
	 * @throws DatabaseException when error
	 */
	public static PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws DatabaseException {
		try {
			return getConnection().prepareStatement(sql, autoGeneratedKeys);
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	public static long getMaxRows() {
		return maxRows;
	}

}
