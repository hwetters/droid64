package droid64.gui;

import java.io.File;

import javax.swing.JFileChooser;

import org.junit.Assert;
import org.junit.Test;

public class FilePathPanelTest {

	@Test
	public void test() {
		new FilePathPanel(new File("."), JFileChooser.FILES_ONLY, null);
		FilePathPanel fpp = new FilePathPanel(new File("."), JFileChooser.FILES_ONLY, c-> {});
		fpp.setPath("/");
		Assert.assertEquals("", "/", fpp.getPath() );
	}
}