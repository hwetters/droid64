package droid64.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DaoFactoryImplTest {

	private final static String CLASSNAME = "org.h2.Driver";
	private final static String URL = "jdbc:h2:mem:test;INIT=RUNSCRIPT FROM 'src/test/resources/droid64/db/setup_h2.sql'";
	private final static String USER = "unittest";
	private final static String PASSWORD = "secret";
	private final static int MAXROWS = 10;

	@Before
	public void setup() throws DatabaseException {
		DaoFactoryImpl.initialize(CLASSNAME, URL, USER, PASSWORD, MAXROWS, 0);
	}

	@Test
	public void testSetup() throws DatabaseException, SQLException {
		DaoFactoryImpl.testConnection(CLASSNAME, URL, USER, PASSWORD);
		DaoFactoryImpl.getConnection().close();
		DaoFactoryImpl.getConnection().close();

		Assert.assertFalse(new Value().toString().isEmpty());
		Assert.assertFalse(new DatabaseException("test").toString().isEmpty());
		Assert.assertFalse(new NotFoundException("test").toString().isEmpty());
		Assert.assertFalse(new SearchResultRow("", "", "", "", "", 0, "").toString().isEmpty());

		PreparedStatement stmt = DaoFactoryImpl.prepareStatement("SELECT COUNT(1) FROM disk");
		ResultSet rs = stmt.executeQuery();
		Assert.assertTrue(rs.next());
		Assert.assertEquals(0, rs.getInt(1));
	}

}
