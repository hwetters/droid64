package droid64.gui;

import javax.swing.JDialog;

import org.junit.Test;
import org.mockito.Mockito;

public class TextViewPanelTest {

	@Test
	public void test() {
		MainPanel mainMock = Mockito.mock(MainPanel.class);
		JDialog dialogMock = Mockito.mock(JDialog.class);
		TextViewPanel txt = new TextViewPanel(mainMock);
		txt.setDialog(dialogMock);
		txt.show(new byte[] {0,1,2,3,4,5,6,7,8,9,10, 32, 66, 120,-100, -128, 127,-60}, "title", "name");
		txt.show("message", "title", "name", "text/text");
		txt.show((byte[]) null, "title", "name");
	}

}