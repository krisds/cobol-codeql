package com.semmle.cobol.extractor;

import java.io.File;

import koopa.cobol.CobolFiles;
import koopa.cobol.CobolProject;
import koopa.cobol.projects.StandardCobolProject;
import koopa.core.grammars.combinators.MatchEndOfFile;
import koopa.core.grammars.combinators.Scoped;
import koopa.core.grammars.combinators.Scoped.Visibility;
import koopa.core.parsers.ParserCombinator;
import koopa.core.parsers.combinators.Optional;
import koopa.core.parsers.combinators.Sequence;

/**
 * A {@linkplain CobolProject} implementation for use by ODASA, which uses paths
 * and file extensions to decide how to process files.
 */
public class SemmleCobolProject extends StandardCobolProject {

	private static final Scoped LIBRARY_TEXT = new Scoped(grammar, "text",
			Visibility.PUBLIC, false);
	static {
		LIBRARY_TEXT.setParser(grammar.copybook());
	}

	private static final Scoped SOURCE_TEXT = new Scoped(grammar, "text",
			Visibility.PUBLIC, false);
	static {
		SOURCE_TEXT.setParser(new Sequence(new ParserCombinator[] {
				new Optional(grammar.compilationGroup()),
				new MatchEndOfFile(grammar) }));
	}

	/**
	 * If the file is a copybook (based on
	 * {@linkplain CobolFiles#isCopybook(File)}) we return a copybook parser.
	 * Otherwise we return a compilation group parser.
	 */
	@Override
	public ParserCombinator parserFor(File file) {
		if (CobolFiles.isCopybook(file))
			return LIBRARY_TEXT;
		else
			return SOURCE_TEXT;
	}

	@Override
	public String toString() {
		return "ODASA COBOL Project";
	}
}