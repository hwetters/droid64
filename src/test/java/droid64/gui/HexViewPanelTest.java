package droid64.gui;

import javax.swing.JTextArea;

import org.junit.Test;

import droid64.d64.CbmException;
import droid64.d64.D64;

public class HexViewPanelTest {

	@Test
	public void test() throws CbmException {
		var consoleStream = new ConsoleStream(new JTextArea());
		new HexViewPanel("top", null, "filename", new byte[] {1,2,3,4}, 4, false);
		new HexViewPanel ("top", null, 1, 1, new D64(new byte[174848], consoleStream));
	}

}
