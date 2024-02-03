package droid64.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import droid64.d64.DiskImage;
import droid64.d64.DiskImageType;
import droid64.d64.Utility;

/**<pre style='font-family:Sans,Arial,Helvetica'>
 * Created on 27.07.2004
 *
 *   droiD64 - A graphical filemanager for D64 files
 *   Copyright (C) 2004 Wolfram Heyer
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *   eMail: the@BigBadWolF.de
 *   </pre>
 * @author wolf
 */
public class ExternalProgram {
	
	/** The disk image file name including path */
    private static final String IMAGE      = "{Image}";
    /** The selected files */
    private static final String FILES      = "{Files}";
    /** The selected files prefixed by disk image file */
    private static final String IMAGEFILES = "{ImageFiles}";
    /** The path to the target */
    private static final String TARGET     = "{Target}";
    /** Ask user for details to create a new disk image */
    private static final String NEWFILE    = "{NewFile}";
    /** Disk image type name */
    private static final String IMAGETYPE  = "{ImageType}";
    /** Name of the disk drive used to handle the disk image */
    private static final String DRIVETYPE  = "{DriveType}";
    /** Extra arguments */
    private static final String EXT_ARGUMENTS  = "{ExtArguments}";

	private String command;
	private String arguments;
	private String description;
	private String label;
	private boolean forkThread;
	private String extArguments;

	/**
	 * @param command command to run
	 * @param arguments arguments
	 * @param description description
	 * @param label label
	 * @param forkThread spawn a new thread when running the program
	 */
	public ExternalProgram(String command, String arguments, String description, String label, boolean forkThread) {
		this.command = command;
		this.arguments = arguments;
		this.description = description;
		this.label = label;
		this.forkThread = forkThread;
	}

	/**
	 * Get array of strings to execute as external command
	 * @param sourceImage path the disk image, or null if no image.
	 * @param sourceFiles list of files
	 * @param target folder
	 * @param directory current folder
	 * @param imageType type of disk image
	 * @return array of strings, with command first. Return empty array if command is null or empty string.
	 */
	public List<String> getExecute(File sourceImage, List<String> sourceFiles, File target, File directory, DiskImageType imageType) {
		if (command == null || command.isEmpty()) {
			return new ArrayList<>();
		}
		var files = new ArrayList<String>();
		var imagefiles = new ArrayList<String>();
		if (sourceFiles != null && !sourceFiles.isEmpty()) {
			sourceFiles.stream().filter(s -> s!= null && !s.isEmpty()).forEach(fName -> {
				files.add(fName);
				if (sourceImage != null && sourceImage.exists() && imageType != DiskImageType.UNDEFINED) {
						imagefiles.add(sourceImage.getPath() + ":" + fName);	
				}
			});
		}
		return buildArguments(sourceImage, target, imagefiles, files, directory, imageType);
	}

	private List<String> buildArguments(File sourceImage, File target, List<String> imagefiles, List<String> files, File directory, DiskImageType imageType) {
		var args = new ArrayList<String>();
		if (Utility.isEmpty(command)) {
			return args;
		}
		args.add(command);
		for (String s : arguments.split("\\s+")) {
			switch (s) {
			case IMAGE:
				if (sourceImage != null && sourceImage.exists()) {
					args.add(sourceImage.getPath());
				}
				break;
			case FILES:
				if (!files.isEmpty()) {
					args.addAll(files);
				}
				break;
			case IMAGEFILES:
				if (!imagefiles.isEmpty()) {
					args.addAll(imagefiles);
				} else if (sourceImage != null && sourceImage.exists() && sourceImage.isFile()) {
					args.add(sourceImage.getPath());
				}
				break;
			case TARGET:
				if (target != null) {
					args.add(target.getPath());
				}
				break;
			case NEWFILE:
				var newFile = FileDialogHelper.openImageFileDialog(directory, null, true);
				if (newFile == null) {
					return new ArrayList<>();
				}
				args.add(newFile.getPath());
				break;
			case IMAGETYPE:
				args.add(DiskImage.getImageTypeName(imageType));
				break;
			case DRIVETYPE:
				args.add(imageType.driveName);
				break;
			case EXT_ARGUMENTS:
				if (!Utility.isEmpty(extArguments)) {
					args.add(extArguments);
				}
				break;
			default:
				if (!Utility.isEmpty(s)) {
					args.add(s);
				}
			}
		}
		return args;
	}

	/**
	 * Run the external program.
	 * @param imageFile the image file
	 * @param execArgs arguments as parsed by {@link #getExecute(File, List, File, File, DiskImageType)}
	 * @param mainPanel used to log output from command
	 */
	public void runProgram(final File imageFile, final List<String> execArgs, final MainPanel mainPanel) {
		if (execArgs == null || execArgs.isEmpty()) {
			mainPanel.appendConsole("No command to execute!");
		}
		try {
			mainPanel.appendConsole("Executing: " + execArgs);
			if (forkThread) {
				var runner = new Thread() {
					@Override
					public void run() {
						runProgramThread(imageFile, execArgs, mainPanel);
					}
				};
				runner.start();
			} else {
				runProgramThread(imageFile, execArgs, mainPanel);
				mainPanel.getLeftDiskPanel().reloadDiskImage(true);
				mainPanel.getRightDiskPanel().reloadDiskImage(true);
			}
		} catch (Exception e) {	//NOSONAR
			mainPanel.appendConsole('\n'+e.getMessage());
		}
	}

	private static int runProgramThread(File folder, List<String> execArgs, MainPanel mainPanel) {
		return runProgramThread(folder, execArgs, mainPanel::appendConsole, mainPanel::appendConsole);
	}

	/**
	 * @param folder the current folder for the command
	 * @param execArgs the command and its arguments
	 * @param outputConsumer consumer of the output
	 * @param errorConsumer the receiver of error printouts
	 * @return return code. Normally 0 means success.
	 */
	public static int runProgramThread(File folder, List<String> execArgs, Consumer<String> outputConsumer, Consumer<String> errorConsumer) {
		int returnCode=1;
		try {
			var process = new ProcessBuilder()
					.directory(folder != null ? folder : new File("."))
					.redirectOutput(Redirect.PIPE)
					.redirectErrorStream(true)
					.command(execArgs)
					.start();
			try (var br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = br.readLine()) != null) {
					outputConsumer.accept(line);
				}
				returnCode = process.waitFor();
			}
		} catch (Exception e) {	//NOSONAR
			errorConsumer.accept('\n'+e.getMessage());
		}
		return returnCode;
	}

	@Override
	public String toString() {
		return label;
	}

	/**
	 * @param command program to run
	 * @param arguments arguments to program
	 * @param description description
	 * @param label label
	 * @param forkThread forkThread
	 */
	public void setValues(String command, String arguments, String description, String label, boolean forkThread) {
		this.command = command;
		this.arguments = arguments;
		this.description = description;
		this.label = label;
		this.forkThread = forkThread;
	}

	/**
	 * @return arguments
	 */
	public String getArguments() {
		return arguments;
	}

	/**
	 * @return command
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param string arguments
	 */
	public void setArguments(String string) {
		arguments = string;
	}

	/**
	 * @param string command
	 */
	public void setCommand(String string) {
		command = string;
	}

	/**
	 * @param string description
	 */
	public void setDescription(String string) {
		description = string;
	}

	/**
	 * @param string label
	 */
	public void setLabel(String string) {
		label = string;
	}

	public boolean isForkThread() {
		return forkThread;
	}

	public void setForkThread(boolean forkThread) {
		this.forkThread = forkThread;
	}

	public String getExtArguments() {
		return extArguments;
	}

	public void setExtArguments(String extArguments) {
		this.extArguments = extArguments;
	}
}
