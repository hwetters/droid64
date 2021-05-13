package droid64.gui;

import org.junit.Assert;
import org.junit.Test;

public class ShowHelpPanelTest {

	@Test
	public void test() {
		new ShowHelpPanel();
		Assert.assertFalse(ShowHelpPanel.getAbout().isEmpty());
	}

}
