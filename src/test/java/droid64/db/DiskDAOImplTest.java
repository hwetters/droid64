package droid64.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import droid64.d64.DiskImageType;
import droid64.d64.FileType;
import droid64.d64.Utility;

public class DiskDAOImplTest {

	private final static String CLASSNAME = "org.h2.Driver";
	private final static String URL = "jdbc:h2:mem:test;INIT=RUNSCRIPT FROM 'src/test/resources/droid64/db/setup_h2.sql';DB_CLOSE_DELAY=-1;TRACE_LEVEL_FILE=4;TRACE_LEVEL_SYSTEM_OUT=3";
	private final static String USER = "unittest";
	private final static String PASSWORD = "secret";
	private final static int MAXROWS = 10;
	private DiskDaoImpl impl = null;

	@Before
	public void setup() throws DatabaseException {
		DaoFactoryImpl.initialize(CLASSNAME, URL, USER, PASSWORD, MAXROWS, 0);
		impl = new  DiskDaoImpl();
	}

	@Test
	public void testToString() {
		Assert.assertFalse(new Disk().toString().isEmpty());
		Assert.assertFalse(new DiskFile().toString().isEmpty());
		Assert.assertFalse(new DiskFile().getFileTypeString().isEmpty());
		Assert.assertFalse(new DiskSearchCriteria().toString().isEmpty());
		new Disk().setDiskFiles(new ArrayList<DiskFile>());
	}

	@Test
	public void testSave() throws DatabaseException, SQLException {
		Assert.assertEquals(0, impl.getAllDisks(false).collect(Collectors.toList()).size());

		Disk disk = createDisk(1L, 3);
		disk.setInsert();
		impl.save(disk);
		Assert.assertEquals(1, impl.getAllDisks(false).collect(Collectors.toList()).size());
		disk.setUpdate();
		impl.save(disk);
		Assert.assertEquals(1, impl.getAllDisks(false).collect(Collectors.toList()).size());
		disk.setDelete();
		impl.save(disk);
		impl.save(null);
	}

	@Test(expected=NotFoundException.class)
	public void testUpdateFail() throws DatabaseException {
		impl.update(createDisk(99L, 3));
	}

	@Test(expected=NotFoundException.class)
	public void testDeleteFail() throws DatabaseException {
		Disk disk = new Disk();
		disk.setDiskId(99L);
		impl.delete(disk);
	}

	@Test(expected=NotFoundException.class)
	public void testGetFail() throws DatabaseException {
		impl.getDisk(99L);
	}

	@Test
	public void testSearch() throws DatabaseException {
		DiskSearchCriteria criteria = new DiskSearchCriteria();
		criteria.setDiskFileName("diskfilename");
		criteria.setDiskLabel("label");
		criteria.setDiskPath("/some/path");
		criteria.setFileName("filename");
		criteria.setFileSizeMax(10);
		criteria.setFileSizeMin(1);
		criteria.setFileType(FileType.PRG);
		criteria.setImageType(DiskImageType.D64);
		criteria.setHostName(Utility.getHostName());
		Assert.assertTrue(criteria.hasCriteria());
		Assert.assertEquals(0, impl.search(criteria).collect(Collectors.toList()).size());

		Disk disk = createDisk(1L, 3);
		disk.setInsert();
		impl.save(disk);
		Assert.assertEquals(1, impl.getAllDisks(false).collect(Collectors.toList()).size());

		Assert.assertFalse("tostring:", disk.toString().isEmpty());
		criteria.setDiskFileName(disk.getFileName());
		criteria.setDiskLabel(disk.getLabel());
		criteria.setDiskPath(disk.getFilePath());
		criteria.setFileName(disk.getDiskFiles().get(0).getName());
		criteria.setImageType(disk.getImageType());
		criteria.setHostName(disk.getHostName());
		Assert.assertEquals(1, impl.search(criteria).collect(Collectors.toList()).size());
	}

	@Test(expected=NotFoundException.class)
	public void testGetDiskByFileNameFail() throws DatabaseException {
		impl.getDiskByFileName("filename");
	}

	private Disk createDisk(long diskId, int fileCount) {
		Disk disk = new Disk();
		disk.setFileName("filename");
		disk.setDiskId(diskId);
		disk.setFilePath("/some/where");
		disk.setWarnings(1);
		disk.setErrors(1);
		disk.setImageType(DiskImageType.D64);
		disk.setUpdated(new Date());
		disk.setLabel("label");

		for (int i=0; i<fileCount; i++) {
			DiskFile diskfile = new DiskFile();
			diskfile.setFileId(diskId != 0 ? diskId*1000 + i : 0);
			diskfile.setDiskId(diskId);
			diskfile.setName(String.format("file%03d", i));
			diskfile.setFileType(FileType.PRG);
			diskfile.setSize(i+1);
			disk.getDiskFiles().add(diskfile);
		}

		return disk;
	}

}
