package com.semmle.cobol.extractor;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * A {@linkplain Predicate} for {@linkplain Path}s, which uses GLOB
 * {@linkplain PathMatcher}s to test.
 */
public final class Glob implements Predicate<Path> {

	private static final String GLOB_PREFIX = "glob:";
	private final List<PathMatcher> matchers;

	public Glob(List<String> rawPatterns) {
		matchers = new ArrayList<>(rawPatterns.size());

		final FileSystem fs = FileSystems.getDefault();
		for (String rawPattern : rawPatterns)
			matchers.add(fs.getPathMatcher(GLOB_PREFIX + rawPattern));
	}

	public Glob(String... rawPatterns) {
		matchers = new ArrayList<>(rawPatterns.length);

		final FileSystem fs = FileSystems.getDefault();
		for (String rawPattern : rawPatterns)
			matchers.add(fs.getPathMatcher(GLOB_PREFIX + rawPattern));
	}

	@Override
	public boolean test(Path p) {
		for (PathMatcher matcher : matchers)
			if (matcher.matches(p))
				return true;

		return false;
	}
}