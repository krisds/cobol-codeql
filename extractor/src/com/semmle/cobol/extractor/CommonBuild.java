package com.semmle.cobol.extractor;

import static koopa.core.data.Position.ZERO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import com.semmle.cobol.generator.GenerateTrapFile;
import com.semmle.cobol.generator.Tally;
import com.semmle.cobol.generator.Trap;
import com.semmle.cobol.generator.engine.RuleEngine;
import com.semmle.cobol.halstead.CalculateHalstead;
import com.semmle.cobol.mapping.runtime.TrapFile;
import com.semmle.cobol.normalization.AddControlFlowExitNodes;
import com.semmle.cobol.normalization.AddDirectives;
import com.semmle.cobol.normalization.AddImplicitContinueToEmptySentences;
import com.semmle.cobol.normalization.AddImplicitSentenceToEmptyDeclarativeSections;
import com.semmle.cobol.normalization.AddImplicitSentenceToEmptyParagraphs;
import com.semmle.cobol.normalization.AddImplicitSentenceToEmptySections;
import com.semmle.cobol.normalization.NormalizeRelationOperators;
import com.semmle.cobol.normalization.NormalizeText;
import com.semmle.cobol.normalization.UnpackTrees;
import com.semmle.cobol.population.CommonPopulator.ErrorContext;
import com.semmle.cobol.processing.Done;
import com.semmle.cobol.timing.Timing;
import com.semmle.cobol.util.Common;
import com.semmle.util.exception.CatastrophicError;
import com.semmle.util.exception.Exceptions;
import com.semmle.util.exception.ResourceError;
import com.semmle.util.files.FileUtil;
import com.semmle.util.io.WholeIO;
import com.semmle.util.language.LegacyLanguage;
import com.semmle.util.process.CliCommand;
import com.semmle.util.srcarchive.DefaultSourceArchive;
import com.semmle.util.srcarchive.ISourceArchive;
import com.semmle.util.trap.DefaultTrapWriterFactory;

import koopa.cobol.CobolProject;
import koopa.cobol.parser.CobolParser;
import koopa.cobol.parser.ParseResults;
import koopa.core.data.Position;
import koopa.core.data.Token;
import koopa.core.parsers.Messages;
import koopa.core.parsers.Parse;
import koopa.core.trees.Tree;
import koopa.core.util.Files;

public abstract class CommonBuild extends CliCommand {

	private final ISourceArchive sourceArchive = new DefaultSourceArchive(LegacyLanguage.COBOL);
	private final DefaultTrapWriterFactory trapWriterFactory = new DefaultTrapWriterFactory(LegacyLanguage.COBOL);

	public Info process(File file, CobolProject project) {
		Timing.start(file.getPath());
		try {
			return processStreamed(file, project);

		} finally {
			Timing.end(file.getPath());
		}
	}

	/**
	 * Parse and trap the given file in the context of the given Cobol project,
	 * and return info on the results.
	 */
	private Info processStreamed(File file, CobolProject project) {
		logger.info("Processing " + file.getAbsolutePath());

		logger.debug("Copying to source archive ...");
		Timing.start("copying to source archive");
		sourceArchive.add(file, new WholeIO().strictread(file));
		Timing.end("copying to source archive");

		logger.debug("Parsing Cobol text ...");
		final ParseResults results = parseStreamed(file, project);

		if (results.isValidInput()) {
			logger.debug("Processing complete.");
			return Info.nominal();

		} else {
			logger.error("Failed to parse " + file.getAbsolutePath());
			trapParseErrors(file, results);
			return Info.notParsed();
		}
	}

	private ParseResults parseStreamed(File file, CobolProject project) {
		Reader reader = null;
		try {
			reader = Files.getReader(file);

			// We prepare a COBOL parser.
			final CobolParser parser = new CobolParser();
			parser.setProject(project);

			// The parser should not build a syntax tree. Instead we will be
			// processing the raw data stream, which includes tokens and
			// annotations, as it is returned to us.
			parser.setBuildTrees(false);
			// We also don't want the parser to hold on to tokens. Anything we
			// need we'll keep track of ourselves.
			parser.setKeepingTrackOfTokens(false);

			// We prepare a parse of the file, which we will configure with our
			// own processing steps.
			final Parse parse = parser.getParseSetup(file, reader);

			// And here are the steps. Each will take the data stream as input,
			// and pass it on to the next step.
			final NormalizeText normalizeText = new NormalizeText();
			normalizeText.then(new NormalizeRelationOperators()) //
					.then(new AddImplicitSentenceToEmptyDeclarativeSections()) //
					.then(new AddImplicitSentenceToEmptySections()) //
					.then(new AddImplicitSentenceToEmptyParagraphs()) //
					.then(new AddImplicitContinueToEmptySentences()) //
					.then(new AddControlFlowExitNodes()) //
					.then(new AddDirectives(parse)) //
					.then(new CalculateHalstead()) //
					.then(new UnpackTrees()) //
					.then(new GenerateTrapFile(file)) //
					.then(new Done());

			parse.to(normalizeText);

			Timing.start("parse + generation");
			return parser.parse(file, parse);

		} catch (IOException e) {
			throw new ResourceError("IOException while parsing " + file, e);

		} finally {
			try {
				if (reader != null)
					reader.close();

			} catch (IOException e) {
				Exceptions.ignore(e, "Failed to close: " + file);
			}
		}
	}

	/**
	 * To be called when something went really wrong with the parse. This will
	 * set the appropriate error information on the file. It will also trap the
	 * number of lines so that the file can be shown in the dashboard.
	 */
	private void trapParseError(File sourceFile, String message) {
		final TrapFile trapFile = new TrapFile(sourceFile);
		final RuleEngine engine = new RuleEngine(trapFile);

		final Token token = new Token("", ZERO, ZERO);
		Trap.trapError(ErrorContext.PARSE, engine, token, message);

		final Tally tally = getNumLines(sourceFile);
		Trap.trapNumLines(null, token, tally, engine);

		trapFile.clearNonTrappableTuples();

		if (trapFile.validateTuplesAgainstDatabaseScheme())
			trapFile.storeTuples(trapWriterFactory);
		else
			throw new CatastrophicError(
					"Found some problems while generating tuples for the trap file."
							+ " Check the logs for details.");
	}

	/**
	 * To be called when something went really wrong with the parse. This will
	 * set the appropriate error information on the file. It will also trap the
	 * number of lines so that the file can be shown in the dashboard.
	 */
	private void trapParseErrors(File sourceFile, ParseResults results) {
		final Parse parse = results.getParse();
		final Messages messages = parse.getMessages();
		if (messages.getErrorCount() == 0) {
			trapParseError(sourceFile, "Failed to parse.");
			return;
		}

		final TrapFile trapFile = new TrapFile(sourceFile);
		final RuleEngine engine = new RuleEngine(trapFile);

		for (int i = 0; i < messages.getErrorCount(); i++) {
			final koopa.core.util.Tuple<Token, String> error = messages
					.getError(i);

			Token t = error.getFirst();

			// The token on an error may be null when the error applies to the
			// entire file, or when the parser simply can not identify it.
			if (t == null) {
				final Position finalPosition = parse.getFinalPosition();
				t = new Token("", finalPosition, finalPosition);
			}

			Trap.trapError(ErrorContext.PARSE, engine, t, error.getSecond());
		}

		final Tally tally = getNumLines(sourceFile);
		Trap.trapNumLines(null, new Tree(Common.TEXT), tally, engine);

		trapFile.clearNonTrappableTuples();

		if (trapFile.validateTuplesAgainstDatabaseScheme())
			trapFile.storeTuples(trapWriterFactory);
		else
			throw new CatastrophicError(
					"Found some problems while generating tuples for the trap file."
							+ " Check the logs for details.");
	}

	private Tally getNumLines(File sourceFile) {
		BufferedReader reader = null;
		try {
			Tally num = new Tally();

			reader = new BufferedReader(new FileReader(sourceFile));
			while (reader.readLine() != null)
				num.lines++;

			// When failing to parse, everything is in the water...
			num.water = num.lines;
			return num;

		} catch (FileNotFoundException e) {
			throw new CatastrophicError(e);

		} catch (IOException e) {
			throw new CatastrophicError(e);

		} finally {
			FileUtil.close(reader);
		}
	}

	public static class Info {
		public final boolean seenParseError;

		/**
		 * This value only makes sense when {@linkplain #seenParseError} is
		 * <code>true</code>.
		 */
		public final boolean seenTrapError;

		private Info(boolean seenParseError, boolean seenTrapError) {
			this.seenParseError = seenParseError;
			this.seenTrapError = seenTrapError;
		}

		public static Info nominal() {
			return new Info(false, false);
		}

		public static Info notParsed() {
			return new Info(true, false);
		}

		public static Info parsedNotTrapped() {
			return new Info(false, true);
		}
	}
}
