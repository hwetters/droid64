package droid64.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;

import org.junit.Test;
import org.mockito.Mockito;

import droid64.d64.ValidationError;

public class ValidationPanelTest {

	@Test
	public void test() {
		JDialog dialogMock = Mockito.mock(JDialog.class);
		DiskPanel diskPanelMock = Mockito.mock(DiskPanel.class);

		List<ValidationError> list = new ArrayList<>();
		list.add(ValidationError.Error.ERROR_TOO_MANY.getError(1, 1));
		ValidationPanel vp = new ValidationPanel(null);
		vp.setDialog(dialogMock);
		vp.parseErrors(list);
		vp.show(list, diskPanelMock);
		vp.show(new ArrayList<>(), diskPanelMock);
		vp.show(null, diskPanelMock);
	}

}
