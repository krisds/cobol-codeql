package com.semmle.cobol.extractor;

import static com.semmle.cobol.extractor.LgtmYmlConfig.ACCEPT_FAILING_PROJECT;
import static com.semmle.cobol.extractor.LgtmYmlConfig.EXCLUDES;
import static com.semmle.cobol.extractor.LgtmYmlConfig.FILTERS;
import static com.semmle.cobol.extractor.LgtmYmlConfig.FORMAT;
import static com.semmle.cobol.extractor.LgtmYmlConfig.INCLUDES;
import static com.semmle.cobol.extractor.LgtmYmlConfig.LIBRARY_GLOBS;
import static com.semmle.cobol.extractor.LgtmYmlConfig.PREPROCESSING;
import static com.semmle.cobol.extractor.LgtmYmlConfig.SOURCE_GLOBS;
import static com.semmle.cobol.extractor.LgtmYmlConfig.TAB_LENGTH;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.slf4j.LoggerFactory;

import com.semmle.util.exception.ResourceError;
import com.semmle.util.process.CliCommand;

/**
 * An alternative entry point to the Cobol extractor, targeted for use by LGTM.
 */
public class AutoBuild extends CommonBuild {

	private LgtmCobolProject project;

	public AutoBuild() {
		this.project = new LgtmCobolProject(LgtmYmlConfig.SRC);

		project.setDefaultFormat(FORMAT);
		project.setDefaultTabLength(TAB_LENGTH);
		project.setDefaultPreprocessing(PREPROCESSING);
		project.setPredicateForSourceText(new Glob(SOURCE_GLOBS));
		project.setPredicateForLibraryText(new Glob(LIBRARY_GLOBS));
	}

	/**
	 * Perform extraction.
	 */
	@Override
	protected int runApi() {
		try {
			extractSource();
			return 0;
		} catch (IOException e) {
			throw new ResourceError("an extraction error occurred", e);
		}
	}

	/**
	 * Extract all supported candidate files that pass the filters.
	 */
	private void extractSource() throws IOException {
		// This is to aid troubleshooting.
		LgtmYmlConfig.logConfig();

		final Path[] currentRoot = new Path[1];
		FileVisitor<? super Path> visitor = new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {

				if (attrs.isSymbolicLink())
					return SKIP_SUBTREE;

				if (!file.equals(currentRoot[0]) && EXCLUDES.contains(file))
					return SKIP_SUBTREE;

				if (FILTERS.includeFile(getNormalizedPath(file)))
					project.add(file);

				return super.visitFile(file, attrs);
			}

			@Override
			public FileVisitResult preVisitDirectory(Path dir,
					BasicFileAttributes attrs) throws IOException {
				if (!dir.equals(currentRoot[0])
						&& (EXCLUDES.contains(dir) || dir.toFile().isHidden()))
					return SKIP_SUBTREE;
				else
					return super.preVisitDirectory(dir, attrs);
			}

			private String getNormalizedPath(Path file) {
				final String path = file.toString().replace('\\', '/');
				if (path.charAt(0) == '/')
					return path;
				else
					return "/" + path;
			}
		};

		for (Path root : INCLUDES) {
			currentRoot[0] = root;
			Files.walkFileTree(currentRoot[0], visitor);
		}

		int successfulParses = 0;
		for (Path p : project.getFiles()) {
			final Info info = process(p.toFile(), project);
			if (info.seenParseError)
				logger.error("Failed to parse " + p + ". Ignoring.");
			else
				successfulParses += 1;
			if (info.seenTrapError)
				throw new ResourceError("An extraction error occurred.");
		}

		// LGTM-2979 : If all COBOL files in the project failed to parse, do not
		// accept the project as a COBOL project.
		if (!ACCEPT_FAILING_PROJECT && project.getFiles().size() > 0
				&& successfulParses == 0)
			throw new ResourceError("Failed to parse any file as COBOL.");
	}

	public static void main(String[] args) {
		int ret = 1;
		try (CliCommand cmd = new AutoBuild()) {
			ret = cmd.run(args);

		} catch (Exception | Error e) {
			e.printStackTrace();
			LoggerFactory.getLogger(AutoBuild.class)
					.error("Error during COBOL extraction.", e);
			ret = 0xDA5A;

		} finally {
			System.exit(ret);
		}
	}
}
