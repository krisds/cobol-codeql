package com.semmle.cobol.extractor;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.semmle.util.data.StringUtil;
import com.semmle.util.exception.Exceptions;
import com.semmle.util.exception.ResourceError;
import com.semmle.util.exception.UserError;
import com.semmle.util.io.csv.CSVReader;
import com.semmle.util.process.Env;
import com.semmle.util.projectstructure.ProjectLayout;

import koopa.cobol.sources.SourceFormat;

/**
 * This class takes in the values for the environment variables generated from
 * an <code>lgtm.yml</code> config (if any), and transforms them into data
 * readily usable by the builder.
 */
public class LgtmYmlConfig {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(LgtmYmlConfig.class);

	private static final String ENV_SRC = "LGTM_SRC";
	private static final String ENV_INCLUDES = "LGTM_INDEX_INCLUDE";
	private static final String ENV_EXCLUDES = "LGTM_INDEX_EXCLUDE";
	private static final String ENV_REPOSITORY_FOLDERS = "LGTM_REPOSITORY_FOLDERS_CSV";
	private static final String ENV_FILTERS = "LGTM_INDEX_FILTERS";
	private static final String ENV_FORMAT = "LGTM_INDEX_FORMAT";
	private static final String ENV_TAB_LENGTH = "LGTM_INDEX_TAB_LENGTH";
	private static final String ENV_SOURCE_GLOBS = "LGTM_INDEX_SOURCE_GLOBS";
	private static final String ENV_LIBRARY_GLOBS = "LGTM_INDEX_LIBRARY_GLOBS";
	private static final String ENV_PREPROCESSING = "LGTM_INDEX_PREPROCESSING";
	private static final String ENV_ACCEPT_FAILING_PROJECT = "LGTM_INDEX_ACCEPT_FAILING_PROJECT";

	private static final Pattern NEWLINE = Pattern.compile("\n");

	// ------------------------------------------------------------------------

	public static final Path SRC;
	static {
		SRC = toRealPath(getPathFromEnvVar(ENV_SRC));
	}

	// ------------------------------------------------------------------------

	/**
	 * The set of candidate files is parameterised by a set of <i>include
	 * paths</i> and a set of <i>exclude paths</i>. By default, the single
	 * include path is <code>LGTM_SRC</code>. If the environment variable
	 * <code>LGTM_INDEX_INCLUDE</code> is set, it is interpreted as a
	 * newline-separated list of include paths, which are slash-separated paths
	 * relative to <code>LGTM_SRC</code>. This list <i>replaces</i> (rather than
	 * extends) the default include path.
	 * <p>
	 * Similarly, the set of exclude paths is determined by the environment
	 * variables <code>LGTM_INDEX_EXCLUDE</code> and
	 * <code>LGTM_REPOSITORY_FOLDERS_CSV</code>. The former is interpreted like
	 * <code>LGTM_INDEX_INCLUDE</code>, that is, a newline-separated list of
	 * exclude paths relative to <code>LGTM_SRC</code>. The latter is
	 * interpreted as the path of a CSV file, where each line in the file
	 * consists of a classification tag and an absolute path; any path
	 * classified as "external" or "metadata" becomes an exclude path. Note that
	 * there are no implicit exclude paths.
	 * <p>
	 * If an include or exclude path cannot be resolved, a warning is printed
	 * and the path is ignored.
	 */

	public static final Set<Path> INCLUDES = new LinkedHashSet<>();
	public static final Set<Path> EXCLUDES = new LinkedHashSet<>();

	static {
		boolean seenInclude = false;

		// process `$LGTM_INDEX_INCLUDE`
		for (String pattern : NEWLINE.split(getEnvVar(ENV_INCLUDES, "")))
			seenInclude |= addPathPattern(INCLUDES, SRC, pattern);

		if (!seenInclude)
			INCLUDES.add(SRC);

		// process `$LGTM_INDEX_EXCLUDE`
		for (String pattern : NEWLINE.split(getEnvVar(ENV_EXCLUDES, "")))
			addPathPattern(EXCLUDES, SRC, pattern);

		// process `$LGTM_REPOSITORY_FOLDERS_CSV`
		final String lgtmRepositoryFoldersCsv = getEnvVar(
				ENV_REPOSITORY_FOLDERS);
		if (lgtmRepositoryFoldersCsv != null) {
			final Path path = Paths.get(lgtmRepositoryFoldersCsv);
			try (Reader reader = Files.newBufferedReader(path,
					StandardCharsets.UTF_8);
					CSVReader csv = new CSVReader(reader)) {
				// skip titles
				csv.readNext();
				String[] fields;
				while ((fields = csv.readNext()) != null) {
					if (fields.length != 2)
						continue;
					if ("external".equals(fields[0])
							|| "metadata".equals(fields[0])) {
						String folder = fields[1];
						try {
							Path folderPath = Paths.get(new URI(folder));
							EXCLUDES.add(toRealPath(folderPath));
						} catch (InvalidPathException | URISyntaxException
								| ResourceError e) {
							Exceptions.ignore(e,
									"Ignore path and print warning message instead");
							LOGGER.warn("Ignoring '" + fields[0]
									+ "' classification for " + folder
									+ ", which is not a valid path.");
						}
					}
				}
			} catch (IOException e) {
				throw new ResourceError(
						"Unable to process LGTM repository folder CSV.", e);
			}
		}
	}

	/**
	 * The environment variable <code>LGTM_INDEX_FILTERS</code> is interpreted
	 * as a newline-separated list of patterns to append to the
	 * {@linkplain ProjectLayout} (hence taking precedence over the built-in
	 * patterns). Unlike for {@link ProjectLayout}, patterns in
	 * <code>LGTM_INDEX_FILTERS</code> use the syntax
	 * <code>include: pattern</code> for inclusions and
	 * <code>exclude: pattern</code> for exclusions.
	 */

	public static final ProjectLayout FILTERS;

	static {
		final List<String> patterns = new ArrayList<String>();
		patterns.add("/");

		final String base = SRC.toString().replace('\\', '/');
		// process `$LGTM_INDEX_FILTERS`
		for (String pattern : NEWLINE.split(getEnvVar(ENV_FILTERS, ""))) {
			pattern = pattern.trim();
			if (pattern.isEmpty())
				continue;
			String[] fields = pattern.split(":");
			if (fields.length != 2)
				continue;
			pattern = fields[1].trim();
			pattern = base + "/" + pattern;
			if ("exclude".equals(fields[0].trim()))
				pattern = "-" + pattern;
			patterns.add(pattern);
		}

		FILTERS = new ProjectLayout(patterns.toArray(new String[0]));
	}

	// ------------------------------------------------------------------------

	public static final SourceFormat FORMAT;
	public static final int TAB_LENGTH;
	public static final List<String> SOURCE_GLOBS = new LinkedList<>();
	public static final List<String> LIBRARY_GLOBS = new LinkedList<>();
	public static final boolean PREPROCESSING;

	static {
		final String rawFormat = StringUtil.uc(getEnvVar(ENV_FORMAT, "FIXED"));
		SourceFormat format = SourceFormat.fromName(rawFormat);
		if (format == null) {
			LOGGER.warn("Unkown Cobol source format \"" + rawFormat
					+ "\". Defaulting to \"FIXED\".");
			format = SourceFormat.FIXED;
		}

		FORMAT = format;

		final String rawTabLength = StringUtil
				.uc(getEnvVar(ENV_TAB_LENGTH, ""));

		int tabLength = 4;
		try {
			if (rawTabLength != null && !rawTabLength.isEmpty())
				tabLength = Integer.parseInt(rawTabLength);

			if (tabLength < 1) {
				LOGGER.warn("Illegal tab length \"" + rawTabLength
						+ "\". Unsetting, and using default of 4.");
				tabLength = 4;
			}

		} catch (NumberFormatException e) {
			LOGGER.warn("Illegal tab length \"" + rawTabLength
					+ "\". Unsetting, and using default of 4.");
			tabLength = 4;
		}

		TAB_LENGTH = tabLength;

		for (String glob : NEWLINE.split( //
				getEnvVar(ENV_SOURCE_GLOBS, "**.cbl\n**.CBL\n**.cob\n**.COB")))
			SOURCE_GLOBS.add(glob);

		for (String glob : NEWLINE.split( //
				getEnvVar(ENV_LIBRARY_GLOBS,
						"**.cpy\n**.CPY\n**.copy\n**.COPY")))
			LIBRARY_GLOBS.add(glob);

		boolean preprocessing = true;
		final String rawPreprocessing = getEnvVar(ENV_PREPROCESSING, "true");

		if (rawPreprocessing != null && !rawPreprocessing.isEmpty())
			preprocessing = Boolean.parseBoolean(rawPreprocessing);

		PREPROCESSING = preprocessing;
	}

	// ------------------------------------------------------------------------

	public static final boolean ACCEPT_FAILING_PROJECT;
	static {
		boolean acceptFailingProject = false;
		final String rawAcceptFailingProject = getEnvVar(
				ENV_ACCEPT_FAILING_PROJECT, "false");

		if (rawAcceptFailingProject != null
				&& !rawAcceptFailingProject.isEmpty())
			acceptFailingProject = Boolean
					.parseBoolean(rawAcceptFailingProject);

		ACCEPT_FAILING_PROJECT = acceptFailingProject;
	}

	// ------------------------------------------------------------------------

	private static String getEnvVar(String envVarName) {
		return getEnvVar(envVarName, null);
	}

	private static String getEnvVar(String envVarName, String deflt) {
		final String value = Env.systemEnv().getNonEmpty(envVarName);
		return value != null ? value : deflt;
	}

	private static Path getPathFromEnvVar(String envVarName) {
		final String lgtmSrc = getEnvVar(envVarName);
		if (lgtmSrc == null)
			throw new UserError(envVarName + " must be set.");
		return Paths.get(lgtmSrc);
	}

	/**
	 * Convert {@code p} to a real path (as per
	 * {@link Path#toRealPath(java.nio.file.LinkOption...)}), throwing a
	 * {@link ResourceError} if this fails.
	 */
	private static Path toRealPath(Path p) {
		try {
			return p.toRealPath();
		} catch (IOException e) {
			throw new ResourceError(
					"Could not compute real path for " + p + ".", e);
		}
	}

	/**
	 * Add {@code pattern} to {@code patterns}, trimming off whitespace and
	 * prepending {@code base} to it. If {@code pattern} ends with a trailing
	 * slash, that slash is stripped off.
	 * 
	 * @return true if {@code pattern} is non-empty
	 */
	private static boolean addPathPattern(Set<Path> patterns, Path base,
			String pattern) {
		pattern = pattern.trim();
		if (pattern.isEmpty())
			return false;
		final Path path = base.resolve(pattern);
		try {
			Path realPath = toRealPath(path);
			patterns.add(realPath);
		} catch (ResourceError e) {
			Exceptions.ignore(e, "Ignore exception and print warning instead.");
			LOGGER.debug("Skipping path " + path + ", which does not exist.");
		}
		return true;
	}

	/**
	 * This provides the basic troubleshooting info which should become part of
	 * any report.
	 */
	public static void logConfig() {
		LOGGER.info("Source: " + SRC);

		if (INCLUDES == null || INCLUDES.isEmpty())
			LOGGER.info("Includes: none");
		else {
			LOGGER.info("Includes:");
			for (Path include : INCLUDES)
				LOGGER.info("+ " + include);
		}

		if (EXCLUDES == null || EXCLUDES.isEmpty())
			LOGGER.info("Excludes: none");
		else {
			LOGGER.info("Excludes:");
			for (Path exclude : EXCLUDES)
				LOGGER.info("- " + exclude);
		}

		try {
			LOGGER.info("Filters:");
			final StringWriter sw = new StringWriter();
			FILTERS.writeTo(sw);
			final String filters = sw.toString();
			if (!filters.isEmpty())
				LOGGER.info(filters);
		} catch (IOException e) {
			LOGGER.error("error writing out filters", e);
		}

		LOGGER.info("Format: " + FORMAT);
		LOGGER.info("Tab length: " + TAB_LENGTH);

		LOGGER.info("Source GLOBs:");
		for (String glob : SOURCE_GLOBS)
			LOGGER.info(glob);

		LOGGER.info("Library GLOBs:");
		for (String glob : LIBRARY_GLOBS)
			LOGGER.info(glob);

		LOGGER.info("Preprocessing: " + PREPROCESSING);
	}
}
