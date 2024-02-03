package droid64.gui;

public enum ActionButton {

	// Buttons are put into GUI in numerical order, left to right, and then next
	// row.

	LOAD_DISK_BUTTON(        1, Resources.DROID64_BUTTON_LOADDISK,      'l', false),
	BAM_BUTTON(              2, Resources.DROID64_BUTTON_SHOWBAM,       'b', false),
	UP_BUTTON(               3, Resources.DROID64_BUTTON_UP,            'U', false),
	COPY_FILE_BUTTON(        4, Resources.DROID64_BUTTON_COPY,          'c', false),
	MD5_BUTTON(              5, Resources.DROID64_BUTTON_MD5,           '5', false),
	VIEW_TEXT_BUTTON(        6, Resources.DROID64_BUTTON_VIEWTEXT,      't', false),
	PLUGIN_1_BUTTON(         7, "",                                     '1', true),
	HIDE_CONSOLE_BUTTON(     8, Resources.DROID64_BUTTON_HIDECONSOLE,   'e', false),
	UNLOAD_DISK_BUTTON(      9, Resources.DROID64_BUTTON_UNLOADDISK,    'u', false),
	VALIDATE_DISK_BUTTON(   10, Resources.DROID64_BUTTON_VALIDATE,      'v', false),
	DOWN_BUTTON(            11, Resources.DROID64_BUTTON_DOWN,          'D', false),
	NEW_FILE_BUTTON(        12, Resources.DROID64_BUTTON_NEWFILE,       'w', false),
	MKDIR_BUTTON(           13, Resources.DROID64_BUTTON_MKDIR,         'k', false),
	VIEW_IMAGE_BUTTON(      14, Resources.DROID64_BUTTON_VIEWIMAGE,     'm', false),
	PLUGIN_2_BUTTON(        15, "",                                     '2', true),
	SEARCH_BUTTON(          16, Resources.DROID64_BUTTON_SEARCH,        's', false),
	NEW_DISK_BUTTON(        17, Resources.DROID64_BUTTON_NEWDISK,       'n', false),
	RENAME_DISK_BUTTON(     18, Resources.DROID64_BUTTON_RENAME,        'r', false),
	SORT_FILES_BUTTON(      19, Resources.DROID64_BUTTON_SORT,          'S', false),
	RENAME_FILE_BUTTON(     20, Resources.DROID64_BUTTON_RENAME,        'r', false),
	DELETE_FILE_BUTTON(     21, Resources.DROID64_BUTTON_DELETE,        'd', false),
	VIEW_HEX_BUTTON(        22, Resources.DROID64_BUTTON_VIEWHEX,       'h', false),
	PLUGIN_3_BUTTON(        23, "",                                     '3', true),
	SETTINGS_BUTTON(        24, Resources.DROID64_BUTTON_SETTINGS,      'S', false),
	MIRROR_BUTTON(          25, Resources.DROID64_BUTTON_MIRROR,        'v', false),
	PLUGIN_5_BUTTON(        26, "",                                     '5', true),
	PLUGIN_6_BUTTON(        27, "",                                     '6', true),
	PLUGIN_7_BUTTON(        28, "",                                     '7', true),
	PLUGIN_8_BUTTON(        29, "",                                     '8', true),
	VIEW_BASIC_BUTTON(      30, Resources.DROID64_BUTTON_VIEWBASIC,     'B', false),
	PLUGIN_4_BUTTON(        31, "",                                     '4', true),
	EXIT_BUTTON(            32, Resources.DROID64_BUTTON_EXIT,          'x', false);

	final int id;
	final String label;
	final Character mnemonic;
	final boolean isPlugin;

	/** Hidden constructor
	 *
	 * @param id
	 * @param label
	 * @param mnemonic
	 * @param isPlugin
	 */
	private ActionButton(int id, String label, Character mnemonic, boolean isPlugin) {
		this.id = id;
		this.label = label;
		this.mnemonic = mnemonic;
		this.isPlugin = isPlugin;
	}

	public int getId() {
		return id;
	}

	public boolean isPlugin() {
		return isPlugin;
	}

	public String getLabel() {
		return label;
	}

	public Character getMnemonic() {
		return mnemonic;
	}
}
