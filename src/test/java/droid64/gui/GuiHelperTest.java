package droid64.gui;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.junit.Assert;
import org.junit.Test;

public class GuiHelperTest {

	@Test
	public void test() {
		GuiHelper.setLocation(new JFrame(), 2, 3);
		GuiHelper.setLocation(new JFrame(), 0.25f, 0.50f);
		GuiHelper.setSize(new JFrame(), 2, 3);
		GuiHelper.setSize(new JFrame(), 0.33f, 0.75f);
		Assert.assertNotNull("",GuiHelper.getNumField(1, 5, 3, 1));
		GuiHelper.hierarchyListenerResizer(new JFrame());
		Assert.assertNotNull("", GuiHelper.toString(new IllegalArgumentException("")));
		GuiHelper.setDefaultFonts();

		Assert.assertNotNull("", GuiHelper.addMenuItem(new JMenu(), "x", 'x', x -> String.valueOf(x)));
		Assert.assertNotNull("", GuiHelper.addMenuItem(new JMenu(), "x", (Character) null, x -> String.valueOf(x)));
		Assert.assertNotNull("", GuiHelper.addMenuItem(new JPopupMenu(), "x", 'x', x -> String.valueOf(x)));
		Assert.assertNotNull("", GuiHelper.addMenuItem(new JPopupMenu(), "x", (Character) null, x -> String.valueOf(x)));

		Assert.assertNotNull("", GuiHelper.getClassNames(java.sql.Driver.class));

		var cb = new JComboBox<Pair>(new Pair[] {
				new Pair(1,"A"),new Pair(2,"B"),new Pair(3,"C")
		});

		GuiHelper.setSelected(cb, p->false);
		Assert.assertEquals("none",-1, cb.getSelectedIndex());
		GuiHelper.setSelected(cb, p->true);
		Assert.assertEquals("one",0, cb.getSelectedIndex());
		GuiHelper.setSelected(cb, p->p.getNum()==2);
		Assert.assertEquals("two",1, cb.getSelectedIndex());
	}
}
