package droid64.db;

/**
 * Abstract DAO factory.
 * @author Henrik
 */
public abstract class DaoFactory {

	public enum LimitType {LIMIT, FIRST, FETCH}
	private static final String [] LIMIT_TYPE_NAMES = {"LIMIT", "FIRST", "FETCH FIRST"};
	protected static LimitType limitType = LimitType.LIMIT;

	/**
	 * Get <code>Disk</code> DAO
	 * @return DiskDao
	 */
	public abstract DiskDao getDiskDao();

	/**
	 * Static method get get the MySQL DAO factory implementation
	 * @return DaoFactory
	 */
	public static DaoFactory getDaoFactory() {
		return new DaoFactoryImpl();
	}

	public static LimitType getLimitType() {
		return limitType;
	}

	public static void setLimitType(LimitType limitType) {
		DaoFactory.limitType = limitType;
	}

	public static String[] getLimitNames() {
		return LIMIT_TYPE_NAMES;
	}

}
