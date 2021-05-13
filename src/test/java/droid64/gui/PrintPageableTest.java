package droid64.gui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

import javax.swing.JFrame;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class PrintPageableTest {

	@Test
	public void test1() throws Exception  {
		PrintPageable pp = new PrintPageable("text".getBytes(), false, "title", new MainPanel(new JFrame()));
		pp.getNumberOfPages();
		pp.getPageFormat(1);
		pp.getPrintable(1);
	    Graphics gMock = Mockito.mock(Graphics.class);
		Printable p = pp.getPrintable(0);
		Assert.assertEquals(Printable.NO_SUCH_PAGE, p.print(gMock, new PageFormat(), 0));
	}

	@Test
	public void test2() throws Exception  {
		String[] strings = {"line1", "line2"};
		PrintPageable pp = new PrintPageable(strings, "title", true, false, new MainPanel(new JFrame()));
		pp.getNumberOfPages();
		pp.getPageFormat(1);
		pp.getPrintable(1);
		new PrintPageable("aaa\r\nbbb\nccc", "title", true, false, new MainPanel(new JFrame()));
		new PrintPageable(new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB), "title", new MainPanel(new JFrame()));

	    Graphics gMock = Mockito.mock(Graphics.class);
		Printable p = pp.getPrintable(0);
		Assert.assertEquals(Printable.NO_SUCH_PAGE, p.print(gMock, new PageFormat(), 0));

	}

	@Test
	public void test3() throws Exception  {
		PrintPageable pp = new PrintPageable("text", "title", true, false, new MainPanel(new JFrame()));
	    Graphics gMock = Mockito.mock(Graphics.class);
		Printable p = pp.getPrintable(0);
		Assert.assertEquals(Printable.NO_SUCH_PAGE, p.print(gMock, new PageFormat(), 0));
	}

	@Test
	public void test4() throws Exception  {
		PrintPageable pp = new PrintPageable(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "title", new MainPanel(new JFrame()));
		Printable p = pp.getPrintable(0);
	    Graphics gMock = Mockito.mock(Graphics.class);
		Assert.assertEquals(Printable.NO_SUCH_PAGE, p.print(gMock, new PageFormat(), 0));
	}

}
