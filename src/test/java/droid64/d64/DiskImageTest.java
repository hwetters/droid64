package droid64.d64;

import java.io.File;
import java.util.Arrays;

import javax.swing.JTextArea;

import org.junit.Assert;
import org.junit.Test;

import droid64.gui.ConsoleStream;

/**<pre style='font-family:sans-serif;'>
 * Created on 21.06.2004
 *
 *   droiD64 - A graphical file manager for D64 files
 *   Copyright (C) 2004 Wolfram Heyer
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *   eMail: wolfvoz@users.sourceforge.net
 *   http://droid64.sourceforge.net
 *
 * @author henrik
 * </pre>
 */
public class DiskImageTest extends DiskImageBaseTest {

	@Override
	@Test
	public void testToString() {
		Assert.assertFalse(new CbmBam(1,1).toString().isEmpty());

		Assert.assertFalse(new CbmTrack(1,1,1).toString().isEmpty());

		Assert.assertFalse(new CbmException("test").toString().isEmpty());

		Assert.assertFalse(new DirEntry(new CbmFile(), 1).toString().isEmpty());
		Assert.assertFalse(new DirEntry(new CpmFile(), 1).toString().isEmpty());
	}

	@Override
	public void testBlankNewImage() throws Exception {
	}

	@Override
	public void testImportExportSizes() throws Exception {
	}

	@Override
	public void testImportExportNumFiles() throws Exception {
	}

	@Test
	public void testSequence() throws Exception {
		final byte[] data = generateRandom(65534);
		var consoleStream = new ConsoleStream(new JTextArea());
		byte[] copy =
				test(new D88(consoleStream), getTempFile(".d88"),
				test(new D82(consoleStream), getTempFile(".d82"),
				test(new D81(consoleStream), getTempFile(".d81"),
				test(new D80(consoleStream), getTempFile(".d80"),
				test(new D71(consoleStream), getTempFile(".d71"),
				test(new D67(consoleStream), getTempFile(".d67"),
				test(new D64(consoleStream), getTempFile(".d64"), data)))))));

		Assert.assertTrue(compareData(data, copy));
	}

	private byte[] test(DiskImage img, File imgFileName, final byte[] orgData) throws Exception {
		byte[] data = Arrays.copyOf(orgData, orgData.length);
		Assert.assertTrue("Create image ", img.saveNewImage(imgFileName, "UNIT TEST", "01"));
		addFileToImage(img, "TEST", data);
		CbmFile copyFile = img.findFile("TEST", FileType.PRG);
		byte[] copy = img.getFileData(copyFile.getDirPosition());
		Assert.assertTrue(compareData(data, copy));
		return copy;
	}

}
