package com.semmle.cobol.extractor;

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedList;
import java.util.List;

import com.semmle.util.exception.ResourceError;
import com.semmle.util.exception.UserError;
import com.semmle.util.files.FileUtil;
import com.semmle.util.process.ArgsParser;
import com.semmle.util.process.ArgsParser.FileMode;
import com.semmle.util.process.CliCommand;

import koopa.cobol.CobolFiles;
import koopa.cobol.sources.SourceFormat;
import koopa.core.util.TabStops;

/**
 * Main entry point for the trap file generation. Give it a list of paths, and
 * it will scan each one for Cobol source code, parse whatever it finds, and
 * generate trap files for each.
 */
public class Main extends CommonBuild {

	public static void main(String[] args) {
		int ret;
		try (CliCommand cmd = new Main()) {
			ret = cmd.run(args);
		} catch (UserError | ResourceError e) {
			System.err.println(e.getMessage());
			ret = 1;
		}
		System.exit(ret);
	}

	private boolean preprocessing = false;
	private List<String> copybooks = new LinkedList<>();
	private SourceFormat format = SourceFormat.FIXED;
	private List<File> files;
	private boolean tolerateParseErrors = false;
	private boolean tolerateTrapErrors = false;
	private int tabLength = 1;
	private TabStops tabStops = new TabStops();

	@Override
	protected void parseArgs(ArgsParser parser) {
		super.parseArgs(parser);
		
		if (parser.has("--copybooks")) {
			preprocessing = true;
			for (String path : parser.getZeroOrMore("--copybooks")) {
				copybooks.add(path);
			}
		}

		if (parser.has("--free"))
			format = SourceFormat.FREE;
		else if (parser.has("--variable"))
			format = SourceFormat.VARIABLE;
		
		files = parser.getOneOrMoreFiles("files",
				FileMode.FILE_OR_DIRECTORY_MUST_EXIST);

		tolerateParseErrors = parser.has("--tolerate-parse-errors");
		tolerateTrapErrors = parser.has("--tolerate-trap-errors");
		
		if (parser.has("--tab-length"))
			tabLength = parser.getInt("--tab-length");
		
		if (parser.has("--tab-stops"))
			try {
				tabStops.fromString(parser.getString("--tab-stops"));
			} catch (IllegalArgumentException e) {
				throw new UserError(
						"Tab stops must be a comma separated list of numbers, in ascending order.",
						e);
			}
	}
	
	@Override
	protected int runApi() {
		final FileFilter cobolFilter = CobolFiles.getFileFilter(true);
		boolean seenParseError = false;
		boolean seenTrapError = false;

		final SemmleCobolProject project = new SemmleCobolProject();
		project.setDefaultFormat(format);
		project.setDefaultPreprocessing(preprocessing);
		project.setDefaultTabLength(tabLength);
		project.setDefaultTabStops(tabStops);
		for (String path : copybooks)
			project.addCopybookPath(new File(path).getAbsoluteFile());
		
		for (File root : files) {
			try {
				File canonical = root.getCanonicalFile();
				if (canonical.isDirectory()) {
					for (File file : FileUtil.recursiveFind(canonical,
							cobolFilter)) {
						Info info = process(file, project);

						if (info.seenParseError)
							seenParseError = true;
						else if (info.seenTrapError)
							seenTrapError = true;
					}

				} else {
					Info info = process(canonical, project);

					if (info.seenParseError)
						seenParseError = true;
					else if (info.seenTrapError)
						seenTrapError = true;
				}

			} catch (Exception e) {
				System.err.println("[FATAL]");
				e.printStackTrace(System.err);
				System.exit(2);
			}
		}

		if (seenParseError && !tolerateParseErrors)
			return 1;
		
		if (seenTrapError && !tolerateTrapErrors)
			return 1;
		
		return 0;
	}

	protected void addArgs(ArgsParser argsParser) {
		super.addArgs(argsParser);
		argsParser.addFlag("--quiet", 0, "Produce less output.");
		argsParser.addFlag("--fixed", 0,
				"Set reference format to FIXED (default).");
		argsParser.addFlag("--free", 0, "Set reference format to FREE.");
		argsParser.addFlag("--variable", 0,
				"Set reference format to VARIABLE.");
		argsParser.addFlag("--copybooks", 1, "Set copybook path.", true);
		argsParser.addFlag("--tolerate-parse-errors", 0,
				"Return '0' even if parse errors are encountered.");
		argsParser.addFlag("--tolerate-trap-errors", 0,
				"Return '0' even if trap errors are encountered.");
		argsParser.addFlag("--tab-length", 1,
				"Tab length expressed in number of spaces (integer, defaults to 1).");
		argsParser.addFlag("--tab-stops", 1,
				"Tab stops (comma separated list of integers, defaults to empty).");
		argsParser.addTrailingParam("files",
				"Files and directories to extract.");
	}
}
