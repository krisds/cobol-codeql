package com.semmle.cobol.extractor;

import static com.semmle.util.data.StringUtil.uc;
import static com.semmle.util.files.FileUtil.basename;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import koopa.cobol.CobolProject;
import koopa.cobol.projects.BasicCobolProject;
import koopa.core.grammars.combinators.MatchEndOfFile;
import koopa.core.grammars.combinators.Scoped;
import koopa.core.grammars.combinators.Scoped.Visibility;
import koopa.core.parsers.ParserCombinator;
import koopa.core.parsers.combinators.Choice;
import koopa.core.parsers.combinators.Optional;
import koopa.core.parsers.combinators.Sequence;

/**
 * A {@linkplain CobolProject} implementation for use by LGTM, which uses GLOB
 * patterns to decide how to process files.
 */
public final class LgtmCobolProject extends BasicCobolProject {

	private static final Scoped SOURCE_TEXT = new Scoped(grammar, "text",
			Visibility.PUBLIC, false);
	static {
		SOURCE_TEXT.setParser(new Sequence(new ParserCombinator[] {
				new Optional(grammar.compilationGroup()),
				new MatchEndOfFile(grammar) }));
	}

	private static final Scoped LIBRARY_TEXT = new Scoped(grammar, "text",
			Visibility.PUBLIC, false);
	static {
		LIBRARY_TEXT.setParser(grammar.copybook());
	}

	private static final Scoped UNKNOWN_TEXT = new Scoped(grammar, "text",
			Visibility.PUBLIC, false);
	static {
		UNKNOWN_TEXT.setParser(
				new Choice(grammar.compilationGroup(), grammar.copybook()));
	}

	private final Path root;
	private final Map<String, List<Path>> libraryMap = new LinkedHashMap<>();

	private Predicate<Path> sourcePredicate;
	private Predicate<Path> libraryPredicate;

	private List<Path> files;

	public LgtmCobolProject(Path root) {
		this.root = root;
		this.files = new LinkedList<>();

		setPredicateForSourceText(
				new Glob("**.cbl", "**.CBL", "**.cob", "**.COB"));
		setPredicateForLibraryText(
				new Glob("**.cpy", "**.CPY", "**.copy", "**.COPY"));
	}

	public File locateCopybook(String textName, String libraryName,
			File source) {
		final Path sourcePath = source.toPath();

		textName = uc(textName);

		if (!libraryMap.containsKey(textName))
			return null;

		Predicate<Path> pred = new Predicate<Path>() {
			@Override
			public boolean test(Path p) {
				return !p.equals(sourcePath);
			}
		};

		if (libraryName != null) {
			final String ln = uc(libraryName);
			pred = pred.and(new Predicate<Path>() {
				@Override
				public boolean test(Path p) {
					final Path parent = p.getParent();
					return !root.equals(p) && uc(basename(parent)).equals(ln);
				}
			});
		}

		final List<Path> libraryList = libraryMap.get(textName);
		for (Path libraryPath : libraryList)
			if (pred.test(libraryPath))
				return libraryPath.toFile();

		return null;
	}

	@Override
	public CobolProject duplicate() {
		final LgtmCobolProject project = new LgtmCobolProject(root);
		copyBasicSettingsInto(project);
		for (String libraryName : libraryMap.keySet())
			project.libraryMap.put(libraryName,
					new LinkedList<Path>(libraryMap.get(libraryName)));
		return project;
	}

	/**
	 * If either {@link #sourcePredicate} or {@link #libraryPredicate} matches
	 * we return a parser of the specific type. If both match (or neither,
	 * though that would be unexpected) we return a parser which tries all
	 * types.
	 */
	@Override
	public ParserCombinator parserFor(File file) {
		final boolean isSource = isSourceText(file.toPath());
		final boolean isLibrary = isLibraryText(file.toPath());

		if (isSource && isLibrary)
			return UNKNOWN_TEXT;
		else if (isSource)
			return SOURCE_TEXT;
		else if (isLibrary)
			return LIBRARY_TEXT;
		else
			return UNKNOWN_TEXT;
	}

	public void setPredicateForSourceText(Predicate<Path> predicate) {
		this.sourcePredicate = new RelativeTo(root, predicate);
	}

	public void setPredicateForLibraryText(Predicate<Path> predicate) {
		this.libraryPredicate = new RelativeTo(root, predicate);
	}

	public List<Path> getFiles() {
		return files;
	}

	public boolean isSourceText(Path path) {
		return sourcePredicate.test(path);
	}

	public boolean isLibraryText(Path path) {
		return libraryPredicate.test(path);
	}

	/**
	 * This will apply the {@link #sourcePredicate} and
	 * {@link #libraryPredicate} to identify the file. The path will only be
	 * registered if at least one of these predicates matches.
	 */
	public void add(Path path) {
		final boolean isLibrary = isLibraryText(path);
		final boolean include = isLibrary || isSourceText(path);

		if (include)
			this.files.add(path);

		if (isLibrary)
			addLibrary(path);
	}

	private void addLibrary(Path path) {
		final String textName = uc(basename(path));
		final List<Path> libraryList;
		if (libraryMap.containsKey(textName))
			libraryList = libraryMap.get(textName);
		else {
			libraryList = new LinkedList<>();
			libraryMap.put(textName, libraryList);
		}

		libraryList.add(path);
	}

	@Override
	public String toString() {
		return "LGTM COBOL Project";
	}
}