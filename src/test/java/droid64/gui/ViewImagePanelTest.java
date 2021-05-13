package droid64.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;

import org.junit.Test;
import org.mockito.Mockito;

public class ViewImagePanelTest {

	@Test
	public void test() throws IOException {
		MainPanel mainMock = Mockito.mock(MainPanel.class);
		JDialog dialogMock = Mockito.mock(JDialog.class);
		ViewImagePanel vip = new ViewImagePanel("title", mainMock);
		vip.setDialog(dialogMock);

		vip.show(new ArrayList<byte[]>(), new ArrayList<String>());
		vip.show((List<byte[]>)null, (List<String>)null);
		vip.show(new BufferedImage(4, 8, BufferedImage.TYPE_INT_RGB), "name");
	}

}
