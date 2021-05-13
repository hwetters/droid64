package droid64.gui;

import java.io.File;

import javax.swing.JFrame;

import org.junit.Assert;
import org.junit.Test;

public class MainPanelTest {

	@Test
	public void test() {
		JFrame frame = new JFrame();
		frame.setVisible(false);
		MainPanel mainPanel = new MainPanel(frame);
		mainPanel.scanForD64Files(new File("."), null);
		mainPanel.setPluginButtonLabel(Integer.MAX_VALUE, "");
		Assert.assertNotNull("", mainPanel.getParent());
		MainPanel.getReleaseNotes();
		MainPanel.getManual();
	}
}
