package com.semmle.cobol.extractor;

import java.nio.file.Path;
import java.util.function.Predicate;

/**
 * A {@linkplain Predicate} wrapper which applies
 * {@linkplain Path#relativize(Path)} to the path before further testing.
 */
public class RelativeTo implements Predicate<Path> {

	private final Path root;
	private final Predicate<Path> pred;

	public RelativeTo(Path root, Predicate<Path> pred) {
		this.root = root;
		this.pred = pred;
	}

	@Override
	public boolean test(Path p) {
		return pred.test(root.relativize(p));
	}
}