package droid64.gui;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JPanel;

import droid64.DroiD64;
import droid64.cfg.ParameterType;
import droid64.d64.DiskImageType;
import droid64.d64.Utility;

public enum Setting {
	// The settings definitions
	ASK_QUIT("ask_quit",                         ParameterType.BOOLEAN,          Boolean.TRUE),
	BOOKMARK_BAR("bookmark_bar",                 ParameterType.BOOLEAN,          Boolean.TRUE),
	BORDER_ACTIVE("color_border_active",         ParameterType.COLOR,            Color.RED),
	BORDER_INACTIVE("color_border_inactive",     ParameterType.COLOR,            Color.GRAY),
	COLOUR("colour",                             ParameterType.INTEGER,          Integer.valueOf(0)),
	DEFAULT_IMAGE_DIR("default_image_dir",       ParameterType.FILE,             System.getProperty("user.home")),
	DEFAULT_IMAGE_DIR2("default_image_dir2",     ParameterType.FILE,             System.getProperty("user.home")),
	DIR_BG("color_dir_bg",                       ParameterType.COLOR,            new Color( 64, 64, 224)),
	DIR_FG("color_dir_fg",                       ParameterType.COLOR,            new Color(160,160, 255)),
	DIR_CPM_BG("color_dir_cpm_bg",               ParameterType.COLOR,            new Color( 16,  16,  16)),
	DIR_CPM_FG("color_dir_cpm_fg",               ParameterType.COLOR,            new Color(192, 255, 192)),
	DIR_LOCAL_BG("color_dir_local_bg",           ParameterType.COLOR,            new JPanel().getBackground()),
	DIR_LOCAL_FG("color_dir_local_fg",           ParameterType.COLOR,            new JPanel().getForeground()),
	EXT_REMOVAL("ext_removal",                   ParameterType.STRING_LIST,      Arrays.asList(".prg;.seq".split(Setting.DELIM))),
	FONT_SIZE("font_size",                       ParameterType.INTEGER,          Integer.valueOf(10)),
	HIDECONSOLE("hide_console",                  ParameterType.BOOLEAN,          Boolean.FALSE),
	LOCAL_FONT_SIZE("local_font_size",           ParameterType.INTEGER,          Integer.valueOf(10)),
	WINDOW("window",                             ParameterType.STRING,           Utility.EMPTY),
	JDBC_DRIVER("jdbc_driver",                   ParameterType.STRING,           Setting.JDBC_DEFAULT_DRIVER),
	JDBC_URL("jdbc_url",                         ParameterType.STRING,           Setting.JDBC_DEFAULT_URL),
	JDBC_USER("jdbc_user",                       ParameterType.STRING,           Setting.DROID64),
	JDBC_PASS("jdbc_password",                   ParameterType.STRING,           Setting.JDBC_DEFAULT_PASS),
	JDBC_LIMIT_TYPE("jdbc_limit_type",           ParameterType.INTEGER,          Integer.valueOf(0)),
	EXCLUDED_IMAGE_FILES("excluded_image_files", ParameterType.STRING,           null),
	MAX_ROWS("max_rows",                         ParameterType.INTEGER,          Integer.valueOf(25)),
	ROW_HEIGHT("row_height",                     ParameterType.INTEGER,          Integer.valueOf(10)),
	LOCAL_ROW_HEIGHT("local_row_height",         ParameterType.INTEGER,          Integer.valueOf(10)),
	USE_DB("use_database",                       ParameterType.BOOLEAN,          Boolean.FALSE),
	LOOK_AND_FEEL("look_and_feel",               ParameterType.INTEGER,          Integer.valueOf(0)),
	PLUGIN_COMMAND("plugin_command",             ParameterType.INDEXED_STRING,   Utility.makeList( "d64copy", "x64", "128", "cbmctrl", "xpet", "x64", "x64" )),
	PLUGIN_ARGUMENTS("plugin_arguments",         ParameterType.INDEXED_STRING,   Utility.makeList( "{Image} 8", Setting.VICE_PLUGIN_ARGS, Setting.VICE_PLUGIN_ARGS, Setting.VICE_PLUGIN_ARGS, "-drive8type {DriveType} -model 8032 {ImageFiles}", "-fs8convertp00 {Files}", "-fs8 {Image}" )),
	PLUGIN_DESCRIPTION("plugin_description",     ParameterType.INDEXED_STRING,   Utility.makeList( "Transfer this disk image to a real floppy.", "Invoke VICE 64 emulator with this disk image", "Invoke VICE 128 emulator with this disk image", "List files using OpenCBM", "VICE PET emulator", "VICE program", "VICE FS" )),
	PLUGIN_LABEL("plugin_label",                 ParameterType.INDEXED_STRING,   Utility.makeList( "d64copy", "VICE 64", "VICE 128", "CBM dir", "VICE PET", "VICE prg", "VICE fs" )),
	PLUGIN_FORK("plugin_fork",                   ParameterType.INDEXED_STRING,   Utility.makeList( "true", "true", "true", "true", "true", "true", "true" )),
	FILE_EXT_D64("file_ext_d64",                 ParameterType.STRING_LIST,      Arrays.asList(".d64".split(Setting.DELIM)) ),
	FILE_EXT_D67("file_ext_d67",                 ParameterType.STRING_LIST,      Arrays.asList(".d67".split(Setting.DELIM)) ),
	FILE_EXT_D71("file_ext_d71",                 ParameterType.STRING_LIST,      Arrays.asList(".d71".split(Setting.DELIM)) ),
	FILE_EXT_D81("file_ext_d81",                 ParameterType.STRING_LIST,      Arrays.asList(".d81".split(Setting.DELIM)) ),
	FILE_EXT_T64("file_ext_t64",                 ParameterType.STRING_LIST,      Arrays.asList(".t64".split(Setting.DELIM)) ),
	FILE_EXT_D80("file_ext_d80",                 ParameterType.STRING_LIST,      Arrays.asList(".d80".split(Setting.DELIM)) ),
	FILE_EXT_D82("file_ext_d82",                 ParameterType.STRING_LIST,      Arrays.asList(".d82".split(Setting.DELIM)) ),
	FILE_EXT_D88("file_ext_d88",                 ParameterType.STRING_LIST,      Arrays.asList(".d88".split(Setting.DELIM)) ),
	FILE_EXT_LNX("file_ext_lnx",                 ParameterType.STRING_LIST,      Arrays.asList(".lnx".split(Setting.DELIM)) ),
	FILE_EXT_D64_GZ("file_ext_d64_gz",           ParameterType.STRING_LIST,      Arrays.asList(".d64.gz".split(Setting.DELIM)) ),
	FILE_EXT_D67_GZ("file_ext_d67_gz",           ParameterType.STRING_LIST,      Arrays.asList(".d67.gz".split(Setting.DELIM)) ),
	FILE_EXT_D71_GZ("file_ext_d71_gz",           ParameterType.STRING_LIST,      Arrays.asList(".d71.gz".split(Setting.DELIM)) ),
	FILE_EXT_D81_GZ("file_ext_d81_gz",           ParameterType.STRING_LIST,      Arrays.asList(".d81.gz".split(Setting.DELIM)) ),
	FILE_EXT_T64_GZ("file_ext_t64_gz",           ParameterType.STRING_LIST,      Arrays.asList(".t64.gz".split(Setting.DELIM)) ),
	FILE_EXT_D80_GZ("file_ext_d80_gz",           ParameterType.STRING_LIST,      Arrays.asList(".d80.gz".split(Setting.DELIM)) ),
	FILE_EXT_D82_GZ("file_ext_d82_gz",           ParameterType.STRING_LIST,      Arrays.asList(".d82.gz".split(Setting.DELIM)) ),
	FILE_EXT_D88_GZ("file_ext_d88_gz",           ParameterType.STRING_LIST,      Arrays.asList(".d88.gz".split(Setting.DELIM)) ),
	FILE_EXT_LNX_GZ("file_ext_lnx_gz",           ParameterType.STRING_LIST,      Arrays.asList(".lnx.gz".split(Setting.DELIM)) ),
	SYS_FONT("sys_font",                         ParameterType.FONT,             new JPanel().getFont()),
	CONSOLE_FONT("console_font",                 ParameterType.FONT,             new JPanel().getFont()),
	CBM_FONT("cbm_font",                         ParameterType.FONT,             (Font) null);

	// Setting attributes
	public final String id;
	public final ParameterType type;
	protected final Object defaultValue;
	protected Object value = null;

	// Constants - file formatting
	public static final String DELIM = ";";
	private static final String LF = "\n";
	private static final String EQ = "=";
	private static final String DOT = ".";
	private static final String STRING_LIST_SPLIT_EXPR = "\\s*[;]\\s*";
	// For convenience
	private static final String USER_HOME = System.getProperty("user.home");
	private static final String VICE_PLUGIN_ARGS = "-drive8type {DriveType} {ImageFiles}";
	private static final String JDBC_DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";
	private static final String JDBC_DEFAULT_URL = "jdbc:mysql://localhost:3306/droid64";
	private static final String JDBC_DEFAULT_PASS = "uridium";
	private static final String DROID64 = "droid64";
	/** Default name of settings file (without path). */
	private static final String DEFAULT_SETTING_FILE_NAME = ".droiD64.cfg";
	/** Default name of bookmark settings file (without path). */
	private static final String DEFAULT_BOOKMARK_FILE_NAME = ".droid64_book.xml";
	public static final int MAX_PLUGINS = 8;
	/**
	 * Constructor
	 * @param id the id of the parameter
	 * @param type the type of the parameter
	 * @param defaultValue the default value
	 */
	private Setting(String id, ParameterType type, Object defaultValue) {
		this.id = id;
		this.type = type;
		this.defaultValue = defaultValue;
		reset();
	}
	public static Stream<Setting> getParameters(Predicate<Setting> predicate) {
		return Arrays.stream(Setting.values()).filter(predicate);
	}
	/**
	 * @return value of default value if no value has yet been assigned
	 */
	private Object get() {
		return value != null ? value : defaultValue;
	}
	public Boolean getBoolean() {
		return (Boolean) get();
	}
	public Integer getInteger() {
		return (Integer) get();
	}
	public String getString() {
		return (String) get();
	}
	public Color getColor() {
		return (Color) get();
	}
	public Font getFont() {
		Object obj = get();
		if (obj instanceof Font) {
			return (Font) obj;
		} else if (type == ParameterType.FONT && defaultValue == null && value == null) {
			value = getCommodoreFont();
			return (Font) value;
		}
		return null;
	}
	public File getFile() {
		Object obj = get();
		if (obj instanceof String) {
			return new File((String)obj);
		} else if (obj instanceof File) {
			return (File)obj;
		}
		return (File) get();
	}
	@SuppressWarnings("unchecked")
	public List<String> getList() {
		return (List<String>) get();
	}
	/**
	 * Assign value to the setting
	 * @param value the value to set
	 */
	protected void set(Object value) {
		switch(type) {
		case STRING_LIST:
			if (value instanceof List<?>) {
				this.value = Utility.cloneList((List<?>)value);
			} else if (value instanceof String) {
				this.value = this.parseStringList((String) value);
			} else if (value instanceof String[]) {
				this.value = Arrays.asList(value);
			} else {
				this.value = new ArrayList<String>();
			}
			break;
		case INDEXED_STRING:
			this.value = value instanceof List<?> ? Utility.cloneList((List<?>)value): new ArrayList<String>();
			break;
		case COLOR:
			if (value instanceof Color) {
				this.value = value;
			} else if (value instanceof String) {
				this.value = parseColor((String) value);
			} else if (value instanceof Integer) {
				this.value = new Color((Integer) value);
			} else {
				this.value = defaultValue;
			}
			break;
		case FONT:
			this.value = value instanceof Font ? value : defaultValue;
			break;
		case BOOLEAN:
			if (value instanceof Boolean || value == null) {
				this.value = Boolean.TRUE.equals(value);
			} else if (value instanceof String) {
				this.value = parseBoolean((String)value);
			} else if (value instanceof Integer) {
				this.value = ((Integer)value).intValue() == 0;
			} else {
				this.value = defaultValue;
			}
			break;
		case STRING:
			if (value instanceof String) {
				this.value = value;
			} else if (value == null) {
				this.value = defaultValue;
			} else {
				this.value = String.valueOf(value);
			}
			break;
		case INTEGER:
			if (value instanceof Integer) {
				this.value = value;
			} else if (value instanceof String) {
				this.value = parseInteger((String) value);
			} else {
				this.value = defaultValue;
			}
			break;
		case FILE:
			if (value instanceof File) {
				this.value = value;
			} else if (value instanceof Path) {
				this.value = ((Path)value).toFile();
			} else if (value instanceof String) {
				this.value = new File((String)value);
			} else {
				this.value = defaultValue;
			}
			break;
		default:
			this.value = value;
			break;
		}
	}
	/** Reset parameter to default value */
	public void reset() {
		set(defaultValue);
	}
	public static void resetAll() {
		for (Setting s:	Setting.values()) {
			s.reset();
		}
	}
	/**
	 * Parse string and assign to setting value
	 * @param string the string to parse
	 */
	public void parse(String string) {
		switch(type) {
		case STRING_LIST:
			this.value = parseStringList(string);
			break;
		case INDEXED_STRING:
			parseIndexedString(string);
			break;
		case FONT:
			this.value = parseFont(string);
			break;
		case COLOR:
			this.value = parseColor(string);
			break;
		case BOOLEAN:
			this.value = parseBoolean(string);
			break;
		case INTEGER:
			this.value = parseInteger(string);
			break;
		case FILE:
			this.value = parseFile(string);
			break;
		case STRING:
			this.value = string;
			break;
		}
	}
	protected File parseFile(String string) {
		return string != null ? new File(string) : (File) defaultValue;
	}
	/**
	 * @param string the string to parse
	 * @return the parsed integer
	 */
	protected Integer parseInteger(String string) {
		try {
			return string != null ? Integer.valueOf(string.trim()) : (Integer) defaultValue;
		} catch (NumberFormatException e) {
			return (Integer) defaultValue;
		}
	}
	/**
	 * @param string add the string to the list of strings
	 */
	@SuppressWarnings("unchecked")
	protected void parseIndexedString(String string) {
		if (this.value == null) {
			this.value = new ArrayList<String>();
		}
		((List<String>) this.value).add(string);
	}
	protected boolean parseBoolean(String string) {
		if (string == null) {
			return false;
		} else {
			return "yes".equals(string.trim()) ? Boolean.TRUE : Boolean.valueOf(string.trim());
		}
	}
	protected Color parseColor(String str) {
		if (str == null) {
			return null;
		}
		String[] split = str.trim().split("\\s*[,;.]\\s*");
		if (split.length >= 3) {
			int r = Integer.parseInt(split[0].trim()) & 0xff;
			int g = Integer.parseInt(split[1].trim()) & 0xff;
			int b = Integer.parseInt(split[2].trim()) & 0xff;
			return new Color(r, g, b);
		}
		return null;
	}
	@SuppressWarnings("unchecked")
	protected List<String> parseStringList(String value) {
		if (Utility.isEmpty(value)) {
			return new ArrayList<>((List<String>)defaultValue);
		} else {
			List<String> list = new ArrayList<>();
			Collections.addAll(list, value.replaceAll("[\\r\\n]+$", "").split(STRING_LIST_SPLIT_EXPR));
			return list;
		}
	}
	protected Font parseFont(String string) {
		if (string != null && !string.isEmpty()) {
			String[] sa = string.split(STRING_LIST_SPLIT_EXPR);
			if (sa.length >= 3) {
				return new Font(sa[0].trim(), Integer.parseInt(sa[1].trim()), Integer.parseInt(sa[2].trim()));
			} else if (new File(string).isFile()) {
				try (FileInputStream input = new FileInputStream(string)) {
					float size = Optional.ofNullable(FONT_SIZE.getInteger()).orElse(12);
					return Font.createFont(Font.TRUETYPE_FONT, input).deriveFont(size);
				} catch (FontFormatException | IOException e) {
					System.err.println("Failed to load font '" + string + "'\n" + e.getMessage());
				}
			} else {
				throw new IllegalArgumentException(id + " is missing attributes. ");
			}
		}
		return null;
	}
	public static void load() {
		try {
			load(getConfigFile());
		} catch (Exception ignored) {
			resetAll();
			System.err.println("Failed to load DroiD64 settings. Using default settings.");
		}
	}
	private static File getConfigFile() {
		return new File ((USER_HOME != null ? USER_HOME + File.separator : "") + DEFAULT_SETTING_FILE_NAME);
	}

	public static File getBookmarkFile() {
		return new File ((USER_HOME != null ? USER_HOME + File.separator : "") + DEFAULT_BOOKMARK_FILE_NAME);
	}

	public static void load(File file) throws IOException {
		load(new FileReader(file));
	}
	public static void save() throws IOException {
		save(getConfigFile());
	}
	public static void save(File file) throws IOException {
		try (PrintWriter writer = new PrintWriter(file)) {
			save(writer, new Date());
		}
	}

	public static void load(InputStreamReader input) throws IOException {
		final Map<String,String> props = loadSettings(input);
		getParameters(s -> true).sorted((a, b) -> {
			if (a.type == ParameterType.FONT) {
				return b.type == ParameterType.FONT ? 0 : 1;
			}
			return b.type == ParameterType.FONT ? -1 : a.id.compareTo(b.id);
		}).forEach(s -> {
			if (props.containsKey(s.id)) {
				s.parse(props.get(s.id));
			} else if (s.type == ParameterType.INDEXED_STRING) {
				final Map<Integer, String> map = new HashMap<>();
				props.keySet().stream().map(a -> a).filter(a -> a.matches("^" + s.id + "\\.\\d+$"))
						.map(m -> Integer.valueOf(m.replaceFirst("^" + s.id + "\\.(\\d+)$", "$1")))
						.forEach(i -> map.put(i, props.get(s.id + DOT + i)));
				int len = map.keySet().stream().reduce((a, b) -> Math.max(a + 1, b + 1)).orElse(0);
				List<String> list = new ArrayList<>(Arrays.asList(new String[len]));
				map.entrySet().stream().forEach(e -> list.set(e.getKey(), e.getValue()));
				s.set(list);
			}
		});
	}

	protected static Map<String, String> loadSettings(InputStreamReader input) throws IOException {
		try (BufferedReader reader = new BufferedReader(input)) {
			return reader.lines()
				.map(String::trim)
				.filter(s -> !s.startsWith("#") && s.contains("="))
				.collect(Collectors.toMap(
						s -> s.substring(0, s.indexOf('=')).trim(),
						s -> s.substring(s.indexOf('=') + 1).trim()));
		}
	}

	public static void save(PrintWriter output, Date timestamp) {
			output.write("# Configuration file for " + DroiD64.PROGNAME + " v" + DroiD64.VERSION + LF);
			output.write("# Saved " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(timestamp) + LF);
			output.write("#" + LF);
			for (var setting : Arrays.asList(Setting.values()).stream().sorted((a, b) -> a.id.compareTo(b.id)).collect(Collectors.toList())) {
				output.write(setting.toString());
			}
			output.write("# End of file\n");
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toString() {
		switch(type) {
		case STRING_LIST:
			String sl = Optional.ofNullable((List<String>)value).map(List::stream).orElseGet(Stream::empty).collect(Collectors.joining(DELIM));
			return  id + EQ + (sl.isEmpty() ? "" : sl) + LF;
		case INDEXED_STRING:
			var list = (List<String>) value;
			if (list != null) {
				StringBuilder buf = new StringBuilder();
				for (int i=0; i < list.size(); i++) {
					String v = list.get(i);
					if (v!=null) {
						buf.append(id).append(DOT).append(i).append(EQ).append(v).append(LF);
					}
				}
				return buf.toString();
			} else {
				return "";
			}
		case FONT:
			return id + EQ + toString((Font) value) + LF;
		case COLOR:
			return id + EQ + toString((Color) value) + LF;
		case BOOLEAN:
		case INTEGER:
		case STRING:
		case FILE:
		default:
			return id + EQ + (value != null ?  String.valueOf(value) : "") + LF;
		}
	}

	public static String toString(Color color) {
		return color != null ? "" + color.getRed() + ',' + color.getGreen() + ',' + color.getBlue() : "";
	}

	public static String toString(Font font) {
		return font != null && !font.equals(getCommodoreFont()) ? font.getName() + DELIM + font.getStyle() + DELIM + font.getSize() : "";
	}

	public static List<ExternalProgram> getExternalPrograms() {
		List<ExternalProgram> list = new ArrayList<>();
		for (int i=0; i < MAX_PLUGINS; i++) {
			list.add(getExternalProgram(i));
		}
		return list;
	}
	@SuppressWarnings("unchecked")
	public static ExternalProgram getExternalProgram(int num) {
		if (num < 0  || num >= MAX_PLUGINS) {
			return null;
		}
		String cmd = Utility.getListItem((List<String>) PLUGIN_COMMAND.get(), num);
		String args = Utility.getListItem((List<String>) PLUGIN_ARGUMENTS.get(), num);
		String descr = Utility.getListItem((List<String>) PLUGIN_DESCRIPTION.get(), num);
		String label = Utility.getListItem((List<String>) PLUGIN_LABEL.get(), num);
		boolean fork = Boolean.parseBoolean(Utility.getListItem((List<String>) PLUGIN_FORK.get(), num));
		return new ExternalProgram(cmd, args, descr, label, fork);
	}

	public static void setExternalProgram(int num, ExternalProgram prg) {
		if (prg != null) {
			Utility.setListItem(PLUGIN_COMMAND.getList(), num, prg.getCommand());
			Utility.setListItem(PLUGIN_ARGUMENTS.getList(), num, prg.getArguments());
			Utility.setListItem(PLUGIN_DESCRIPTION.getList(), num, prg.getDescription());
			Utility.setListItem(PLUGIN_LABEL.getList(), num, prg.getLabel());
			Utility.setListItem(PLUGIN_FORK.getList(), num, Boolean.toString(prg.isForkThread()));
		}
	}

	public static int[] getWindow() {
		return Optional.ofNullable(Setting.WINDOW.getString())
				.map(s -> s.split("\\s*[,:]\\s*"))
				.filter(a -> a.length >= 4)
				.map(b -> new int[] { Integer.parseInt(b[0]), Integer.parseInt(b[1]), Integer.parseInt(b[2]), Integer.parseInt(b[3]) })
				.orElse(new int[0]);
	}

	private static Font getCommodoreFont() {
		try {
			float size = Optional.ofNullable(FONT_SIZE.getInteger()).orElse(12);
			return Font.createFont(Font.TRUETYPE_FONT, Setting.class.getResourceAsStream("resources/droiD64_cbm.ttf")).deriveFont(size);
		} catch (FontFormatException | IOException e) {
			return new JPanel().getFont();
		}
	}

	public static Map<DiskImageType,List<String>> getFileExtensionMap() {
		EnumMap<DiskImageType,List<String>> map = new EnumMap<>(DiskImageType.class);
		map.put(DiskImageType.D64, Utility.joinLists(FILE_EXT_D64.getList(), FILE_EXT_D64_GZ.getList()));
		map.put(DiskImageType.D67, Utility.joinLists(FILE_EXT_D67.getList(), FILE_EXT_D67_GZ.getList()));
		map.put(DiskImageType.D71, Utility.joinLists(FILE_EXT_D71.getList(), FILE_EXT_D71_GZ.getList()));
		map.put(DiskImageType.D80, Utility.joinLists(FILE_EXT_D80.getList(), FILE_EXT_D80_GZ.getList()));
		map.put(DiskImageType.D81, Utility.joinLists(FILE_EXT_D81.getList(), FILE_EXT_D81_GZ.getList()));
		map.put(DiskImageType.D82, Utility.joinLists(FILE_EXT_D82.getList(), FILE_EXT_D82_GZ.getList()));
		map.put(DiskImageType.D88, Utility.joinLists(FILE_EXT_D88.getList(), FILE_EXT_D88_GZ.getList()));
		map.put(DiskImageType.LNX, Utility.joinLists(FILE_EXT_LNX.getList(), FILE_EXT_LNX_GZ.getList()));
		map.put(DiskImageType.T64, Utility.joinLists(FILE_EXT_T64.getList(), FILE_EXT_T64_GZ.getList()));
		return map;
	}

	public static DiskImageType getDiskImageType(File file) {
		if (file.isDirectory()) {
			return DiskImageType.UNDEFINED;
		}
		final String name = file.getName().toLowerCase();
		Optional<Entry<DiskImageType, List<String>>> opt = Setting.getFileExtensionMap().entrySet().stream().filter(
				entry -> entry.getValue().stream().anyMatch(ext -> name.endsWith(ext.toLowerCase()))).findFirst();
		return opt.isPresent() ? opt.get().getKey() : DiskImageType.UNDEFINED;
	}

	public static boolean isImageFileName(File file) {
		return !DiskImageType.UNDEFINED.equals(getDiskImageType(file));
	}

	/**
	 * Check if name ends with a matching file extension. <br>
	 * If so, return name, else return name with the first matching name+extension.
	 * @param imageType image type
	 * @param compressed compressed
	 * @param name name
	 * @return string
	 */
	public static String checkFileNameExtension(DiskImageType imageType, boolean compressed, String name) {
		if (name == null || name.isEmpty() || imageType == null) {
			return name;
		}
		List<String> extensions = getFileExtensionList(imageType, compressed);
		if (extensions.isEmpty()) {
			return name;
		}
		for (String ext : extensions) {
			if (name.toLowerCase().endsWith(ext.toLowerCase())) {
				return name;
			}
		}
		return name + extensions.get(0);
	}

	private static List<String> getFileExtensionList(DiskImageType imgType, boolean compressed) {
		switch (imgType) {
		case D64:
		case D64_CPM_C64:
		case D64_CPM_C128:
			return compressed ? FILE_EXT_D64_GZ.getList() : FILE_EXT_D64.getList();
		case D67:
			return compressed ? FILE_EXT_D67_GZ.getList() : FILE_EXT_D67.getList();
		case D71:
		case D71_CPM:
			return compressed ? FILE_EXT_D71_GZ.getList() : FILE_EXT_D71.getList();
		case D80:
			return compressed ? FILE_EXT_D80_GZ.getList() : FILE_EXT_D80.getList();
		case D81:
		case D81_CPM:
			return compressed ? FILE_EXT_D81_GZ.getList() : FILE_EXT_D81.getList();
		case D82:
			return compressed ? FILE_EXT_D82_GZ.getList() : FILE_EXT_D82.getList();
		case D88:
			return compressed ? FILE_EXT_D88_GZ.getList() : FILE_EXT_D88.getList();
		case T64:
			return compressed ? FILE_EXT_T64_GZ.getList() : FILE_EXT_T64.getList();
		case LNX:
			return compressed ? FILE_EXT_LNX_GZ.getList() : FILE_EXT_LNX.getList();
		default:
			return new ArrayList<>();
		}
	}
}
