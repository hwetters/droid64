package droid64.gui;

import org.junit.Test;

import droid64.d64.DiskImageType;
import droid64.d64.FileType;

public class SearchPanelTest {

	@Test
	public void test() {
		new SearchPanel("title", null);
	}

	@Test
	public void testSearch() {
		SearchPanel search = new SearchPanel("title", null);
		search.search(FileType.PRG, DiskImageType.D64);
	}

}
