package droid64.gui;

import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import droid64.d64.D64;
import droid64.d64.DiskImageType;
import droid64.gui.BAMPanel.BamState;
import droid64.gui.BAMPanel.BamTrack;
import droid64.gui.BAMPanel.ColoredTableCellRenderer;

public class BAMPanelTest {


	@Test
	public void testBAMFrame() {
		MainPanel mainMock = Mockito.mock(MainPanel.class);
		JDialog dialogMock = Mockito.mock(JDialog.class);

		BamTrack[] bamTracks = new BamTrack[] {new BamTrack(1,0)};
		var consoleStream = new ConsoleStream(new JTextArea());

		BAMPanel bp = new BAMPanel(mainMock);
		bp.setDialog(dialogMock);
		bp.show("diskName", bamTracks, new D64(DiskImageType.D64, consoleStream), false);
	}


	@Test
	public void test() throws BadLocationException {
		MainPanel mainMock = Mockito.mock(MainPanel.class);
		var bp = new BAMPanel(mainMock);

		var table = new JTable(new DefaultTableModel(new Object[]{"Column1", "Column2"}, 2));
		var renderer = new ColoredTableCellRenderer(bp);
		Assert.assertNotNull(renderer.getTableCellRendererComponent(table, BamState.USED, false, false, 1, 1));
		Assert.assertNotNull(renderer.getTableCellRendererComponent(table, BamState.FREE, false, false, 1, 1));
		Assert.assertNotNull(renderer.getTableCellRendererComponent(table, BamState.RESERVED, false, false, 1, 1));
		Assert.assertNotNull(renderer.getTableCellRendererComponent(table, 1, false, false, 1, 0));
	}
}
