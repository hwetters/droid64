package droid64.db;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import droid64.d64.DiskImageType;
import droid64.d64.FileType;
import droid64.d64.Utility;

/**
 * MySQL implementation of DiskDao.
 * @author Henrik
 */
public class DiskDaoImpl implements DiskDao {

	private static final String SELECT = "SELECT ";
	private static final String COLUMN_NAMES = "diskId, label, filePath, fileName, updated, imagetype, errors, warnings, hostname";
	private static final String UPDATE_COLUMN_NAMES = "label=?, filePath=?, fileName=?, updated=?, imagetype=?, errors=?, warnings=?, hostname=?";
	private static final String AND_SPACE = "AND ";

	@Override
	public Stream<Disk> getAllDisks(boolean composite) throws DatabaseException {
		String sql = SELECT + COLUMN_NAMES + " FROM disk";
		try (var stmt = DaoFactoryImpl.prepareStatement(sql)) {
			ResultSet rs = stmt.executeQuery();
			return consumeRows(rs, composite);
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	@Override
	public Disk getDisk(long diskId) throws DatabaseException {
		String sql = SELECT + COLUMN_NAMES + " FROM disk WHERE diskid=?";
		ResultSet rs = null;
		try (var stmt = DaoFactoryImpl.prepareStatement(sql)){
			stmt.setLong(1, diskId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				return consumeRow(rs, false);
			} else {
				throw new NotFoundException("No such diskId ("+diskId+").");
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {/* ignore */}
			}
		}
	}

	@Override
	public Disk getDiskByFileName(String fileName) throws DatabaseException {
		String sql = SELECT + COLUMN_NAMES + " FROM disk WHERE filepath=? AND filename=?";
		File f = new File(fileName);
		File p = f.getAbsoluteFile().getParentFile();
		String path = p != null ? p.getAbsolutePath() : null;
		String file = f.getName();
		ResultSet rs = null;
		try (var stmt = DaoFactoryImpl.prepareStatement(sql)) {
			stmt.setString(1, path);
			stmt.setString(2, file);

			rs = stmt.executeQuery();
			if (rs.next()) {
				return consumeRow(rs, false);
			} else {
				throw new NotFoundException("No such disk ("+fileName+").");
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {/* ignore */}
			}
		}
	}

	@Override
	public void update(Disk disk) throws DatabaseException {
		if (disk == null) {
			throw new DatabaseException("Null argument.");
		}
		String sql = "UPDATE disk SET " + UPDATE_COLUMN_NAMES + " WHERE diskid=?";
		try (var stmt = DaoFactoryImpl.prepareStatement(sql)){
			stmt.setString(1, disk.getLabel());
			stmt.setString(2, disk.getFilePath());
			stmt.setString(3, disk.getFileName());
			stmt.setDate(4, new java.sql.Date(disk.getUpdated().getTime()));
			stmt.setInt(5, disk.getImageType().type);
			setInteger(stmt, 6, disk.getErrors());
			setInteger(stmt, 7, disk.getWarnings());
			stmt.setString(8, disk.getHostName());
			stmt.setLong(9, disk.getDiskId());
			if (1 != stmt.executeUpdate()) {
				throw new NotFoundException("DiskId "+disk.getDiskId()+" could not be updated.");
			}
		}  catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	@Override
	public void delete(Disk disk) throws DatabaseException {
		if (disk == null) {
			throw new DatabaseException("Null argument.");
		}
		String sql1 = "DELETE FROM diskfile WHERE diskid=?";
		try (var stmt = DaoFactoryImpl.prepareStatement(sql1)){
			stmt.setLong(1, disk.getDiskId());
			stmt.executeUpdate();
		}  catch (SQLException e) {
			throw new DatabaseException(e);
		}
		String sql2 = "DELETE FROM disk WHERE diskId=?";
		try (var stmt = DaoFactoryImpl.prepareStatement(sql2)){
			stmt.setLong(1, disk.getDiskId());
			if (1 != stmt.executeUpdate()) {
				throw new NotFoundException("Nothing was deleted");
			}
		}  catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	@Override
	public Stream<Disk> search(DiskSearchCriteria criteria) throws DatabaseException {
		Stream.Builder<Disk> builder = Stream.builder();

		String columns = "d.diskid, d.filepath, d.filename, d.label, df.fileid, df.name, df.filetype, df.size, df.fileNum, df.flags, d.updated, d.imagetype, d.errors, d.warnings, d.hostname, df.namebytes";
		var sqlBuf = new StringBuilder();
		sqlBuf.append(SELECT);
		if (DaoFactory.getLimitType() == DaoFactory.LimitType.FIRST) {
			sqlBuf.append("FIRST ? ");
		}
		sqlBuf.append(columns).append(" FROM diskfile df ");
		sqlBuf.append("JOIN disk d ON df.diskid = d.diskid ");
		buildSearchCriteria(criteria, sqlBuf);
		sqlBuf.append("ORDER BY d.filepath, d.filename, df.fileNum ");
		if (DaoFactory.getLimitType() == DaoFactory.LimitType.LIMIT) {
			sqlBuf.append("LIMIT ?");
		} else if (DaoFactory.getLimitType() == DaoFactory.LimitType.FETCH) {
			sqlBuf.append("FETCH FIRST ? ROWS ONLY");
		}
		String sql = sqlBuf.toString();
		ResultSet rs = null;
		try (var stmt = DaoFactoryImpl.prepareStatement(sql)) {
			setSearchCriterias(stmt, criteria);
			rs = stmt.executeQuery();
			while (rs.next()) {
				builder.add(consumeDiskComposite(rs));
			}
			return builder.build();
		}  catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {/* ignore */}
			}
		}
	}


	private void buildSearchCriteria(DiskSearchCriteria criteria, StringBuilder sqlBuf) {
		if (!criteria.hasCriteria()) {
			return;
		}
		String and = Utility.EMPTY;
		sqlBuf.append("WHERE ");
		if (!isStringNullOrEmpty(criteria.getFileName())) {
			sqlBuf.append("UPPER(df.name) LIKE ? ");
			and = AND_SPACE;
		}
		if (!isStringNullOrEmpty(criteria.getDiskLabel())) {
			sqlBuf.append(and).append("UPPER(d.label) LIKE ? ");
			and = AND_SPACE;
		}
		if (!isStringNullOrEmpty(criteria.getDiskPath())) {
			sqlBuf.append(and).append("UPPER(d.filepath) LIKE ? ");
			and = AND_SPACE;
		}
		if (!isStringNullOrEmpty(criteria.getDiskFileName())) {
			sqlBuf.append(and).append("UPPER(d.filename) LIKE ? ");
			and = AND_SPACE;
		}
		if (criteria.getFileSizeMin()!=null) {
			sqlBuf.append(and).append("df.size >= ? ");
			and = AND_SPACE;
		}
		if (criteria.getFileSizeMax()!=null) {
			sqlBuf.append(and).append("df.size <= ? ");
			and = AND_SPACE;
		}
		if (criteria.getFileType()!=null) {
			sqlBuf.append(and).append("df.filetype = ? ");
			and = AND_SPACE;
		}
		if (!isStringNullOrEmpty(criteria.getHostName())) {
			sqlBuf.append(and).append("UPPER(d.hostname) LIKE ? ");
			and = AND_SPACE;
		}
		if (criteria.getImageType()!=null) {
			sqlBuf.append(and).append("d.imagetype = ? ");
		}
	}

	private void setSearchCriterias (PreparedStatement stmt, DiskSearchCriteria criteria) throws SQLException {
		int idx = 1;
		if (DaoFactory.getLimitType() == DaoFactory.LimitType.FIRST) {
			stmt.setLong(idx++, DaoFactoryImpl.getMaxRows());
		}
		if (criteria.hasCriteria()) {
			if (!isStringNullOrEmpty(criteria.getFileName())) {
				stmt.setString(idx++, "%"+criteria.getFileName().toUpperCase()+"%");
			}
			if (!isStringNullOrEmpty(criteria.getDiskLabel())) {
				stmt.setString(idx++, "%"+criteria.getDiskLabel().toUpperCase()+"%");
			}
			if (!isStringNullOrEmpty(criteria.getDiskPath())) {
				stmt.setString(idx++, "%"+criteria.getDiskPath().toUpperCase()+"%");
			}
			if (!isStringNullOrEmpty(criteria.getDiskFileName())) {
				stmt.setString(idx++, "%"+criteria.getDiskFileName().toUpperCase()+"%");
			}
			if (criteria.getFileSizeMin()!=null) {
				stmt.setInt(idx++, criteria.getFileSizeMin());
			}
			if (criteria.getFileSizeMax()!=null) {
				stmt.setInt(idx++, criteria.getFileSizeMax());
			}
			if (criteria.getFileType()!=null) {
				stmt.setInt(idx++, criteria.getFileType().type);
			}
			if (!isStringNullOrEmpty(criteria.getHostName())) {
				stmt.setString(idx++, "%"+criteria.getHostName().toUpperCase()+"%");
			}
			if (criteria.getImageType()!=null) {
				stmt.setInt(idx++, criteria.getImageType().type);
			}
		}
		if (DaoFactory.getLimitType() == DaoFactory.LimitType.LIMIT || DaoFactory.getLimitType() == DaoFactory.LimitType.FETCH) {
			stmt.setLong(idx, DaoFactoryImpl.getMaxRows());
		}
	}

	private Disk consumeDiskComposite(ResultSet rs) throws SQLException {
		var disk = new Disk();
		var file = new DiskFile();
		disk.getDiskFiles().add(file);
		disk.setDiskId(rs.getLong(1));
		disk.setFilePath(rs.getString(2));
		disk.setFileName(rs.getString(3));
		disk.setLabel(rs.getString(4));
		file.setFileId(rs.getLong(5));
		file.setName(rs.getString(6));
		file.setFileType(FileType.get(rs.getInt(7)));
		file.setSize(rs.getInt(8));
		file.setFileNum(rs.getInt(9));
		file.setFlags(rs.getInt(10));
		var updated = rs.getTimestamp(11);
		disk.setUpdated(updated!=null ? new Date(updated.getTime()) : null);
		disk.setImageType(DiskImageType.get(rs.getInt(12)));
		disk.setErrors(getInteger(rs, 13));
		disk.setWarnings(getInteger(rs, 14));
		disk.setHostName(rs.getString(15));
		return disk;
	}

	@Override
	public void save(Disk disk) throws DatabaseException {
		if (disk == null) {
			return;
		}
		String sql = "SELECT d.diskid, d.filepath, d.filename, d.label, df.fileid, df.name, df.filetype, df.size, df.fileNum, df.flags, d.imagetype, d.errors, d.warnings, d.hostname, df.namebytes " +
				"FROM disk d " +
				"LEFT JOIN diskfile df ON df.diskid = d.diskid " +
				"WHERE d.filePath = ? AND d.filename = ? AND (UPPER(hostname) = ? OR hostname IS NULL)" +
				"ORDER BY d.filepath, d.filename, df.fileNum;\n ";
		ResultSet rs = null;
		try (var stmt = DaoFactoryImpl.prepareStatement(sql)) {
			int idx = 1;
			stmt.setString(idx++, disk.getFilePath());
			stmt.setString(idx++, disk.getFileName());
			stmt.setString(idx, disk.getHostName() != null ? disk.getHostName().toUpperCase() : Utility.EMPTY);
			rs = stmt.executeQuery();
			Disk oldDisk = null;
			while (rs.next()) {
				oldDisk = consumeSaveRs(rs, oldDisk);
			}
			if (oldDisk != null) {
				disk.setDiskId(oldDisk.getDiskId());
				disk.setUpdate();
				int longestList = disk.getDiskFiles().size() > oldDisk.getDiskFiles().size() ? disk.getDiskFiles().size() : oldDisk.getDiskFiles().size();
				int newFileCount = disk.getDiskFiles().size();
				int oldfileCount = oldDisk.getDiskFiles().size();
				for (int i = 0; i < longestList; i++) {
					if (i < newFileCount && i < oldfileCount) {
						DiskFile newFile = disk.getDiskFiles().get(i);
						DiskFile oldFile = oldDisk.getDiskFiles().get(i);
						newFile.setDiskId(disk.getDiskId());
						newFile.setFileId(oldFile.getFileId());
						newFile.setUpdate();
					} else if (i >= newFileCount) {
						DiskFile oldFile = oldDisk.getDiskFiles().get(i);
						oldFile.setDelete();
						disk.getDiskFiles().add(oldFile);
					} else {
						disk.getDiskFiles().get(i).setDelete();
						disk.getDiskFiles().get(i).setDiskId(disk.getDiskId());
					}
				}
			} else {
				disk.setInsert();
				for (DiskFile newFile : disk.getDiskFiles()) {
					newFile.setInsert();
				}
			}
			performSave(disk);
		}  catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {/* ignore */}
			}
		}
	}

	private Disk consumeSaveRs(ResultSet rs, Disk oldDisk) throws SQLException {
		Disk disk;
		if (oldDisk == null) {
			disk = new Disk();
			disk.setClean();
			disk.setDiskId(rs.getLong(1));
			disk.setFilePath(rs.getString(2));
			disk.setFileName(rs.getString(3));
			disk.setLabel(rs.getString(4));
			disk.setImageType(DiskImageType.get(rs.getInt(11)));
			disk.setErrors(getInteger(rs, 12));
			disk.setWarnings(getInteger(rs, 13));
			disk.setHostName(rs.getString(14));
		} else {
			disk = oldDisk;
		}
		long fileId = rs.getLong(5);
		if (fileId != 0L) {
			DiskFile oldFile = new DiskFile();
			oldFile.setClean();
			oldFile.setFileId(fileId);
			oldFile.setDiskId(rs.getLong(1));
			oldFile.setName(rs.getString(6));
			oldFile.setFileType(FileType.get(rs.getInt(7)));
			oldFile.setSize(rs.getInt(8));
			oldFile.setFileNum(rs.getInt(9));
			oldFile.setFlags(rs.getInt(10));
			oldFile.setNameAsBytes(getBytes(rs, 15));
			disk.getDiskFiles().add(oldFile);
		}
		return disk;
	}

	/**
	 * Save disk
	 *
	 * @param disk Disk
	 * @throws DatabaseException
	 */
	private void performSave(Disk disk) throws DatabaseException {
		try (var conn = DaoFactoryImpl.getConnection()) {
			conn.setAutoCommit(false);
			if (disk.isInsert()) {
				insertDisk(disk, conn);
			} else if (disk.isUpdate()) {
				updateDisk(disk, conn);
			} else if (disk.isDelete()) {
				deleteDisk(disk, conn);
			} else {
				if( disk.getDiskFiles().stream().noneMatch(df -> !df.isClean())) {
					return;
				}
			}
			if (!disk.isDelete()) {
				for (DiskFile file : disk.getDiskFiles()) {
					saveDiskFile(file, conn);
				}
			}
			commit(conn);
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} catch (Exception ex) {
			throw new DatabaseException(String.valueOf(disk), ex);
		}
	}

	private void commit(Connection conn) throws DatabaseException {
		try {
			conn.commit();
		} catch (SQLException e) {
			rollback("Commit failed.", e, conn);
		}
	}

	private void insertDisk(Disk disk, Connection conn) throws DatabaseException {
		String sqlDisk = "INSERT INTO disk(label, filepath, filename, updated, imagetype, errors, warnings, hostname) VALUES(?,?,?,?,?,?,?,?)";
		ResultSet generatedKeys = null;
		try (var stmt = conn.prepareStatement(sqlDisk, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setString(1, disk.getLabel());
			stmt.setString(2, disk.getFilePath());
			stmt.setString(3, disk.getFileName());
			stmt.setTimestamp(4, new Timestamp(new Date().getTime()));
			stmt.setInt(5, disk.getImageType().type);
			setInteger(stmt, 6, disk.getErrors());
			setInteger(stmt, 7, disk.getWarnings());
			stmt.setString(8, disk.getHostName());
			int rows = stmt.executeUpdate();
			if (rows == 0) {
				conn.rollback();
				throw new DatabaseException("Failed to insert new disk.");
			}
			generatedKeys = stmt.getGeneratedKeys();
			if (generatedKeys.next()) {
				disk.setDiskId(generatedKeys.getLong(1));
			} else {
				conn.rollback();
				throw new DatabaseException("Failed to insert disk, no diskId obtained.");
			}
			disk.setClean();
			disk.getDiskFiles().forEach(file -> {
				file.setDiskId(disk.getDiskId());
				file.setInsert();
			});
		} catch (SQLException e1) {
			rollback("Insert failed. "+ e1.getMessage(), e1, conn);
		} finally {
			if (generatedKeys != null) {
				try {
					generatedKeys.close();
				} catch (SQLException e) {/* ignore */}
			}
		}
	}

	private void updateDisk(Disk disk, Connection conn) throws DatabaseException {
		String sqlDisk = "UPDATE disk SET label=?,filepath=?,filename=?,updated=?,imageType=?,errors=?,warnings=?,hostname=? WHERE diskid=?";
		try (var stmt = conn.prepareStatement(sqlDisk)) {
			stmt.setString(1, disk.getLabel());
			stmt.setString(2, disk.getFilePath());
			stmt.setString(3, disk.getFileName());
			stmt.setTimestamp(4, new Timestamp(new Date().getTime()));
			stmt.setInt(5, disk.getImageType().type);
			setInteger(stmt, 6, disk.getErrors());
			setInteger(stmt, 7, disk.getWarnings());
			stmt.setString(8, disk.getHostName());
			stmt.setLong(9, disk.getDiskId());
			int rows = stmt.executeUpdate();
			if (rows == 0) {
				conn.rollback();
				throw new DatabaseException("Failed to update disk.");
			}
			disk.setClean();
		} catch (SQLException e1) {
			rollback("Update failed. " + e1.getMessage(), e1, conn);
		}
	}

	private void deleteDisk(Disk disk, Connection conn) throws DatabaseException {
		String sqlFile = "DELETE FROM diskfile WHERE diskid=?";
		String sqlDisk = "DELETE FROM disk WHERE diskid=?";
		try (var stmt1 = conn.prepareStatement(sqlFile); PreparedStatement stmt2 = conn.prepareStatement(sqlDisk)) {
			stmt1.setLong(1, disk.getDiskId());
			stmt1.executeUpdate();
			stmt2.setLong(1, disk.getDiskId());
			stmt2.executeUpdate();
		} catch (SQLException e1) {
			rollback("Delete failed. "+ e1.getMessage(), e1, conn);
		}
	}

	private void saveDiskFile(DiskFile file, Connection conn) throws DatabaseException {
		int idx = 1;
		if (file.isInsert()) {
			String sql = "INSERT INTO diskfile(diskid,name,filetype,size,filenum,flags,namebytes) VALUES (?,?,?,?,?,?,?)";
			try (var stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
				stmt.setLong(idx++, file.getDiskId());
				stmt.setString(idx++, file.getName());
				stmt.setInt(idx++, file.getFileType().type);
				stmt.setInt(idx++, file.getSize());
				stmt.setInt(idx++, file.getFileNum());
				stmt.setInt(idx++, file.getFlags());
				stmt.setBytes(idx++, file.getNameAsBytes());
				stmt.executeUpdate();
			} catch (SQLException  e) {
				rollback("Insert failed. " + e.getMessage()+" : "+file, e, conn);
			}
		} else if (file.isUpdate()) {
			String sql = "UPDATE diskfile SET name=?,filetype=?,size=?,filenum=?,flags=?,namebytes=? WHERE diskid=? AND fileid=?";
			try (var stmt = conn.prepareStatement(sql)) {
				stmt.setString(idx++, file.getName());
				stmt.setInt(idx++, file.getFileType()!=null?file.getFileType().type:0);
				stmt.setInt(idx++, file.getSize());
				stmt.setInt(idx++, file.getFileNum());
				stmt.setInt(idx++, file.getFlags());
				stmt.setBytes(idx++, file.getNameAsBytes());
				stmt.setLong(idx++, file.getDiskId());
				stmt.setLong(idx++, file.getFileId());
				stmt.executeUpdate();
			} catch (SQLException e) {
				rollback("Update failed. "+ e.getMessage(), e, conn);
			}
		} else if (file.isDelete()) {
			String sql = "DELETE FROM diskfile WHERE diskid=? AND fileid=?";
			try (var stmt = conn.prepareStatement(sql)) {
				stmt.setLong(idx++, file.getDiskId());
				stmt.setLong(idx++, file.getFileId());
				stmt.executeUpdate();
			} catch (SQLException e) {
				rollback("Delete failed. " + e.getMessage(), e, conn);
			}
		}
		file.setClean();
	}

	private void rollback(String message, Exception ex, Connection connection) throws DatabaseException {
		try {
			connection.rollback();
		} catch (SQLException e1) {} //NOSONAR
		throw new DatabaseException(message, ex);
	}


	/**
	 * Convert ResultSet to a Stream of Disk.
	 * @param rs ResultSet
	 * @param composite load composite
	 * @return Stream of Disk
	 * @throws SQLException
	 * @throws DatabaseException
	 */
	private Stream<Disk> consumeRows(ResultSet rs, boolean composite) throws SQLException, DatabaseException {
		Stream.Builder<Disk> builder = Stream.builder();
		while (rs.next()) {
			builder.add(consumeRow(rs, composite));
		}
		return builder.build();
	}

	/**
	 * Convert one ResultSet to a Disk
	 * @param rs ResultSet
	 * @param composite load composite
	 * @return Disk
	 * @throws SQLException
	 * @throws DatabaseException
	 */
	private Disk consumeRow(ResultSet rs, boolean composite) throws SQLException, DatabaseException {
		var vo = new Disk();
		vo.setDiskId(rs.getLong(1));
		vo.setLabel(rs.getString(2));
		vo.setFilePath(rs.getString(3));
		vo.setFileName(rs.getString(4));
		vo.setUpdated(new java.util.Date(rs.getDate(5).getTime()));
		vo.setImageType(DiskImageType.get(rs.getInt(6)));
		vo.setErrors(getInteger(rs, 7));
		vo.setWarnings(getInteger(rs, 8));
		vo.setHostName(rs.getString(9));

		if (composite) {
			vo.setDiskFiles(getFiles(vo));
		}
		return vo;
	}

	private List<DiskFile> getFiles(Disk disk) throws SQLException, DatabaseException {
		final String sql = SELECT + " diskId, fileid, name, filetype, size, fileNum, flags, namebytes FROM diskfile WHERE diskId=? ORDER BY fileid";
		try (var stmt = DaoFactoryImpl.prepareStatement(sql)) {
			stmt.setLong(1, disk.getDiskId());
			try (ResultSet rs = stmt.executeQuery()) {
				List<DiskFile> list = new ArrayList<>();
				while (rs.next()) {
					list.add(consumeFileRow(rs));
				}
				return list;
			}
		}
	}

	private DiskFile consumeFileRow(ResultSet rs) throws SQLException {
		var vo = new DiskFile();
		vo.setDiskId(rs.getLong(1));
		vo.setFileId(rs.getLong(2));
		vo.setName(rs.getString(3));
		vo.setFileType(FileType.get(rs.getInt(4)));
		vo.setSize(rs.getInt(5));
		vo.setFileNum(rs.getInt(6));
		vo.setFlags(rs.getInt(7));
		vo.setNameAsBytes(getBytes(rs, 8));
		return vo;
	}


	/**
	 * Test if string is null or empty.
	 * @param str String
	 * @return true if trimmed String is empty.
	 */
	private boolean isStringNullOrEmpty(String str) {
		return str==null || str.trim().isEmpty();
	}

	private Integer getInteger(ResultSet rs, int col) throws SQLException {
		int value = rs.getInt(col);
		return rs.wasNull() ? null : value;
	}

	private void setInteger(PreparedStatement stmt, int col, Integer value) throws SQLException {
		if (value == null) {
			stmt.setNull(col, java.sql.Types.INTEGER);
		} else {
			stmt.setInt(col,  value.intValue());
		}
	}

	private byte[] getBytes(ResultSet rs, int col) throws SQLException {
		byte[] bytes = rs.getBytes(col);
		return rs.wasNull() ? null : bytes;
	}

}
