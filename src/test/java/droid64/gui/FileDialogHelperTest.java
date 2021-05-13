package droid64.gui;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class FileDialogHelperTest {

	@Test
	public void testGetDefaultFile() {
		Assert.assertNull(FileDialogHelper.getDefaultFile(null, new String[]{".txt"}));
		Assert.assertNull(FileDialogHelper.getDefaultFile("", new String[]{".txt"}));
		Assert.assertEquals(new File("defName"), FileDialogHelper.getDefaultFile("defName", null));
		Assert.assertEquals(new File("defName"), FileDialogHelper.getDefaultFile("defName", new String[]{}));
		Assert.assertEquals(new File("defName.txt"), FileDialogHelper.getDefaultFile("defName", new String[]{".txt"}));
		Assert.assertEquals(new File("defName.png"), FileDialogHelper.getDefaultFile("defName.png", new String[]{".txt", ".png"}));
	}

	@Test
	public void testGetDiskImageFileFilter() throws IOException {
		Setting.load(new File("src/test/resources/droid64/gui/test.config"));
		Assert.assertNotNull(FileDialogHelper.getDiskImageFileFilter());
		Assert.assertEquals("Disk images", FileDialogHelper.getDiskImageFileFilter().getDescription());
		Assert.assertFalse("Bad Ext", FileDialogHelper.getDiskImageFileFilter().accept(new File("test.XXX")));
		Assert.assertTrue("D64 Ext", FileDialogHelper.getDiskImageFileFilter().accept(new File("test.d64")));
		Assert.assertTrue("Directory", FileDialogHelper.getDiskImageFileFilter().accept(new File(".")));
	}

	@Test
	public void testGetFileFilter() {
		Assert.assertNotNull(FileDialogHelper.getFileFilter("", new String[] {}));
		Assert.assertEquals("Empty Desciption", "", FileDialogHelper.getFileFilter("", new String[] {}).getDescription());
		Assert.assertEquals("Test Desciption", "TEST", FileDialogHelper.getFileFilter("TEST", new String[] {}).getDescription());
		Assert.assertTrue("No Ext", FileDialogHelper.getFileFilter("TEST", new String[] {}).accept(new File("test.XXX")));
		Assert.assertFalse("Wrong Ext", FileDialogHelper.getFileFilter("TEST", new String[] {".aaa"}).accept(new File("test.XXX")));
		Assert.assertTrue("OK Ext", FileDialogHelper.getFileFilter("TEST", new String[] {".aaa", ".bbb", ".ccc"}).accept(new File("test.bbb")));
		Assert.assertTrue("Directory", FileDialogHelper.getFileFilter("TEST", new String[] {".aaa", ".bbb", ".ccc"}).accept(new File(".")));
		Assert.assertTrue("Null ext", FileDialogHelper.getFileFilter("TEST", new String[] {}).accept(new File("test.xx")));
		Assert.assertTrue("Null ext", FileDialogHelper.getFileFilter("TEST", null).accept(new File("test.xx")));
	}

	@Test
	public void test() {
		JFileChooser chooserMock = Mockito.mock(JFileChooser.class);
		FileDialogHelper.setChoosers(chooserMock, chooserMock, chooserMock);

		FileDialogHelper.openFontFileDialog("title", ".", "defaultName", new String[0]);
		FileDialogHelper.openFontFileDialog("title", ".", null, new String[0]);
		FileDialogHelper.openFontFileDialog("title", null, null, new String[0]);
		FileDialogHelper.openFontFileDialog(null, null, null, new String[0]);

		FileDialogHelper.openImageFileDialog(new File("."), "defaultName", false);
		FileDialogHelper.openImageFileDialog(new File("."), "defaultName", true);
		FileDialogHelper.openImageFileDialog(new File("."), null, false);
		FileDialogHelper.openImageFileDialog((File) null, null, false);

		FileDialogHelper.openTextFileDialog(new File("."), "defaultName", false);
		FileDialogHelper.openTextFileDialog(new File("."), "defaultName", true);
		FileDialogHelper.openTextFileDialog(new File("."), null, false);
		FileDialogHelper.openTextFileDialog((File)null, null, false);

		FileDialogHelper.openTextFileDialog("title", new File("."), "defaultName", false,  new String[0]);
		FileDialogHelper.openTextFileDialog("title", new File("."), "defaultName", true,  new String[0]);
		FileDialogHelper.openTextFileDialog("title", new File("."), null, true,  new String[0]);
		FileDialogHelper.openTextFileDialog(null, new File("."), null, true,  new String[0]);
		Assert.assertEquals("null", FileDialogHelper.openTextFileDialog("title", null, null, true,  new String[0]));
	}
}
