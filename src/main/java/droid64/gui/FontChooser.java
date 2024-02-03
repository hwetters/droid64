package droid64.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

public class FontChooser extends JPanel {

	private static final long serialVersionUID = 1L;

	/** Preview text string */
	private static final String PREVIEW_TEXT = "The quick brown fox jumps over the lazy dog.";
	/** List of font sizes */
	private static final Integer[] FONT_SIZES = { 8, 10, 11, 12, 14, 16, 18, 20, 24, 28, 36, 40, 48, 60, 72, 80, 88, 96, 112 };
	/** Index of default font size */
	private static final int DEFAULT_SIZE_INDEX = 4;
	/** Selected font */
	private Font resultFont;
	/** Available font names */
	private final List<String> fontList = Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
	/** Font name model */
	private final DefaultListModel<String> fontNameModel = new DefaultListModel<>();
	/** Font name list */
	private final JList<String> fontNameList = new JList<>(fontNameModel);
	/** Font size list */
	private final JList<Integer> fontSizeList = new JList<>(FONT_SIZES);
	/** Bold check box */
	private final JCheckBox boldBox = new JCheckBox("Bold");
	/** Italic check box */
	private final JCheckBox italicBox = new JCheckBox("Italic");
	/** Preview selected font */
	private final JTextArea previewArea = new JTextArea(PREVIEW_TEXT);

	private final Frame owner;
	private final String title;

	public FontChooser(final Frame owner, final String title) {
		this.owner = owner;
		this.title = title;

		fontList.forEach(fontNameModel::addElement);
		fontNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fontSizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		previewArea.setEditable(false);
		previewArea.setSize(200, 50);
		previewArea.setLineWrap(true);
		previewArea.setWrapStyleWord(true);
		previewArea.setBackground(getBackground());
		var border = new CompoundBorder(
				BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)),
				new EmptyBorder(8, 8, 8, 8));
		previewArea.setBorder(border);

		var topRight = new JPanel(new GridLayout(1, 2));
		topRight.add(new JScrollPane(fontSizeList), BorderLayout.WEST);
		topRight.add(new JScrollPane(drawFontAttrPanel()), BorderLayout.EAST);

		var panel = new JPanel(new BorderLayout());
		panel.add(new JScrollPane(fontNameList), BorderLayout.CENTER);
		panel.add(topRight, BorderLayout.EAST);
		panel.add(new JScrollPane(previewArea), BorderLayout.SOUTH);

		setLayout(new BorderLayout());
		add(panel, BorderLayout.CENTER);

		fontNameList.addListSelectionListener(event -> updatePreviewFont());
		fontSizeList.addListSelectionListener(event -> updatePreviewFont());
		addHierarchyListener(e -> GuiHelper.hierarchyListenerResizer(SwingUtilities.getWindowAncestor(this)));
	}

	public Font show(Font currentFont) {
		boldBox.setSelected(currentFont != null && (currentFont.getStyle() & Font.BOLD) != 0);
		italicBox.setSelected(currentFont != null && (currentFont.getStyle() & Font.ITALIC) != 0);

		int nameIdx = 0;
		int sizeIdx = DEFAULT_SIZE_INDEX;
		if (currentFont != null) {
			int fn = fontList.indexOf(currentFont.getName());
			if (fn >= 0) {
				nameIdx = fn;
			}
			for (int i=0; i < FONT_SIZES.length; i++) {
				if (FONT_SIZES[i].equals(currentFont.getSize())) {
					sizeIdx = i;
					break;
				}
			}
		}
		fontNameList.setSelectedIndex(nameIdx);
		fontSizeList.setSelectedIndex(sizeIdx);
		updatePreviewFont();

		if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(owner, this, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
			return resultFont;
		}
		return null;
	}

	private JPanel drawFontAttrPanel() {
		boldBox.setMnemonic('b');
		boldBox.addItemListener(event -> updatePreviewFont());
		italicBox.setMnemonic('i');
		italicBox.addItemListener(event -> updatePreviewFont());
		var fontAttrPanel = new JPanel();
		fontAttrPanel.setLayout(new BoxLayout(fontAttrPanel, BoxLayout.Y_AXIS));
		fontAttrPanel.add(boldBox);
		fontAttrPanel.add(italicBox);
		return fontAttrPanel;
	}

	/**
	 * Update preview field with the current font
	 */
	private void updatePreviewFont() {
		int nameIdx = fontNameList.getSelectedIndex();
		var resultName = fontNameList.getSelectedValue();
		fontNameList.ensureIndexIsVisible(nameIdx);

		int resultSize = Optional.ofNullable(fontSizeList.getSelectedValue()).orElse(12);
		int attrs = boldBox.isSelected() ? Font.BOLD : Font.PLAIN;
		if (italicBox.isSelected()) {
			attrs |= Font.ITALIC;
		}
		resultFont = new Font(resultName, attrs, resultSize);
		previewArea.setFont(resultFont);
	}
}
