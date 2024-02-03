package droid64.gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;

import org.junit.Assert;
import org.junit.Test;

import droid64.d64.DiskImageType;
import droid64.d64.FileType;
import droid64.d64.Utility;
import droid64.d64.ValidationError;

public class DiskPanelTest {

	private MainPanel mainPanel;

	@Test
	public void test() {
		DiskPanel diskPanel = createDiskPanel();

		diskPanel.basicViewFile();
		diskPanel.calcMd5Checksum();
		diskPanel.copyFile();
		diskPanel.deleteFile();
		diskPanel.doExternalProgram(new ExternalProgram(null,null,null,null, false));
		diskPanel.getCurrentImagePath();
		diskPanel.hexViewFile();
		diskPanel.imageViewFile();
		diskPanel.loadLocalDirectory(new File(""));
		diskPanel.moveFile(true);
		diskPanel.moveFile(false);
		diskPanel.openDiskImage(null, true);
		diskPanel.reloadDiskImage(true);
		diskPanel.renameDisk();
		diskPanel.renameFile();
		diskPanel.setActive(true);
		diskPanel.setDirectory(new File("."));
		diskPanel.setOtherDiskPanelObject(mainPanel.getRightDiskPanel());
		diskPanel.setRowHeight(10);
		diskPanel.setTableColors();
		diskPanel.showFile();
		diskPanel.sortFiles();
		diskPanel.unloadDisk();
		diskPanel.validateDisk();

		diskPanel.loadLocalDirectory(new File("NoSuchfile"));

		Assert.assertTrue("isActive", diskPanel.isActive());
		Assert.assertFalse("isImageLoaded", diskPanel.isImageLoaded());
		Assert.assertFalse("isZipFileLoaded", diskPanel.isZipFileLoaded());
		Assert.assertFalse("writableImageLoaded", diskPanel.isWritableImageLoaded());

		diskPanel.setActive(false);
		Assert.assertFalse("isActive", diskPanel.isActive());
		diskPanel.repairValidationErrors(new ArrayList<ValidationError.Error>());
	}

	@Test
	public void testCreate() throws IOException {
		Setting.load(new File("src/test/resources/droid64/gui/test.config"));
		DiskPanel diskPanel = createDiskPanel();

		createImageTest(getRenameResult(File.createTempFile("UnitTest_diskPanelTest", ".d64.gz"), DiskImageType.D64, true), diskPanel);
		createImageTest(getRenameResult(File.createTempFile("UnitTest_diskPanelTest", ".d67.gz"), DiskImageType.D67, true), diskPanel);
		createImageTest(getRenameResult(File.createTempFile("UnitTest_diskPanelTest", ".d71.gz"), DiskImageType.D71, true), diskPanel);
		createImageTest(getRenameResult(File.createTempFile("UnitTest_diskPanelTest", ".d80.gz"), DiskImageType.D80, true), diskPanel);
		createImageTest(getRenameResult(File.createTempFile("UnitTest_diskPanelTest", ".d81.gz"), DiskImageType.D81, true), diskPanel);
		createImageTest(getRenameResult(File.createTempFile("UnitTest_diskPanelTest", ".d82.gz"), DiskImageType.D82, true), diskPanel);
		createImageTest(getRenameResult(File.createTempFile("UnitTest_diskPanelTest", ".d88.gz"), DiskImageType.D88, true), diskPanel);
		createImageTest(getRenameResult(File.createTempFile("UnitTest_diskPanelTest", ".t64.gz"), DiskImageType.T64, true), diskPanel);

		createImageTest(getRenameResult(File.createTempFile("UnitTest_diskPanelTest", ".d64.gz"), DiskImageType.D64, false), diskPanel);
		createImageTest(getRenameResult(File.createTempFile("UnitTest_diskPanelTest", ".d67.gz"), DiskImageType.D67, false), diskPanel);
		createImageTest(getRenameResult(File.createTempFile("UnitTest_diskPanelTest", ".d71.gz"), DiskImageType.D71, false), diskPanel);
		createImageTest(getRenameResult(File.createTempFile("UnitTest_diskPanelTest", ".d80.gz"), DiskImageType.D80, false), diskPanel);
		createImageTest(getRenameResult(File.createTempFile("UnitTest_diskPanelTest", ".d81.gz"), DiskImageType.D81, false), diskPanel);
		createImageTest(getRenameResult(File.createTempFile("UnitTest_diskPanelTest", ".d82.gz"), DiskImageType.D82, false), diskPanel);
		createImageTest(getRenameResult(File.createTempFile("UnitTest_diskPanelTest", ".d88.gz"), DiskImageType.D88, false), diskPanel);
		createImageTest(getRenameResult(File.createTempFile("UnitTest_diskPanelTest", ".t64.gz"), DiskImageType.T64, false), diskPanel);
	}

	@Test
	public void testZip() throws Exception {
		File file = File.createTempFile("UnitTest_DiskPanelZip", ".zip");
		file.deleteOnExit();
		Utility.createNewZipFile(file, "test", getData(64));

		DiskPanel diskPanel = createDiskPanel();
		diskPanel.openDiskImage(file, true);
		diskPanel.loadLocalDirectory(file);
	}

	private void createImageTest(RenameResult result, DiskPanel diskPanel) {
		diskPanel.createNewDisk(result, new File(result.getFileName()));
		diskPanel.openDiskImage(new File(result.getFileName()), true);
		Assert.assertTrue("Image was not loaded: ", diskPanel.isImageLoaded());
		diskPanel.newFile(getNewFile("TEST"));
		Assert.assertEquals("validate failed "+diskPanel.getDiskImageType(), Integer.valueOf(0), diskPanel.validateDisk());
		diskPanel.isWritableImageLoaded();
		diskPanel.sortFiles();
	}

	private DiskPanel createDiskPanel() {
		JFrame frame = new JFrame();
		frame.setVisible(false);
		mainPanel = new MainPanel(frame);
		mainPanel.setButtonState();
		mainPanel.setPluginButtonLabel(1, "ONE");
		Assert.assertFalse("", GuiHelper.getLookAndFeels().isEmpty());
		DiskPanel diskPanel = mainPanel.getLeftDiskPanel();
		diskPanel.setVisible(false);
		return diskPanel;
	}

	private RenameResult getNewFile(String name) {
		RenameResult result = new RenameResult();
		result.setFileName(name);
		result.setFileType(FileType.PRG);
		return result;
	}

	private RenameResult getRenameResult(File file, DiskImageType imageType, boolean compressed) {
		file.deleteOnExit();
		RenameResult result = new RenameResult();
		result.setCompressedDisk(compressed);
		result.setCpmDisk(false);
		result.setDiskID("t12");
		result.setDiskName("test_"+imageType.id);
		result.setFileName(file.getAbsolutePath());
		result.setDiskType(imageType);
		return result;
	}

	private byte[] getData(int size) {
		byte[] data = new byte[size];
		for (int i=0; i<size; i++) {
			data[i] = (byte) ~i;
		}
		return data;
	}

}
