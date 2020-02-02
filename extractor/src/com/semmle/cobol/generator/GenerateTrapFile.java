package com.semmle.cobol.generator;

import static com.semmle.cobol.generator.effects.Effects.CFLOW;
import static com.semmle.cobol.generator.effects.Effects.NUMLINES;
import static com.semmle.cobol.generator.effects.Effects.all;
import static com.semmle.cobol.generator.effects.Effects.closure;
import static com.semmle.cobol.generator.triggers.Triggers.first;
import static com.semmle.cobol.generator.triggers.Triggers.start;
import static com.semmle.cobol.halstead.CalculateHalstead.HALSTEAD_DATA;
import static com.semmle.cobol.halstead.CalculateHalstead.TRAP_HALSTEAD_DATA;
import static com.semmle.cobol.util.Common.TEXT;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.semmle.cobol.extractor.StreamProcessingStep;
import com.semmle.cobol.generator.effects.Effect;
import com.semmle.cobol.generator.engine.RuleEngine;
import com.semmle.cobol.generator.rules.RuleSet;
import com.semmle.cobol.generator.triggers.Trigger;
import com.semmle.cobol.generator.tuples.Tuple;
import com.semmle.cobol.mapping.runtime.TrapFile;
import com.semmle.cobol.population.CobolRulesFromSpec;
import com.semmle.cobol.timing.Timing;
import com.semmle.util.exception.CatastrophicError;
import com.semmle.util.language.LegacyLanguage;
import com.semmle.util.trap.DefaultTrapWriterFactory;

import koopa.core.data.Data;

/**
 * This is the final {@linkplain StreamProcessingStep}. It takes the fully
 * normalized and annotated data stream, and feeds it to the {@link #engine}
 * which will test {@linkplain Trigger}s and run {@linkplain Effect}s when the
 * triggers fire. The end result will, hopefully, be a fully provisioned
 * {@linkplain TrapFile}.
 */
public class GenerateTrapFile extends StreamProcessingStep {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(GenerateTrapFile.class);

	/**
	 * The rule set, configured with mappings for trapping COBOL files.
	 */
	private static final RuleSet RULES = new RuleSet();
	static {
		CobolRules.initialize(RULES);
		CobolRulesFromSpec.initialize(RULES);
	}

	/**
	 * The {@link TrapFile} which will hold the resulting {@link Tuple}s.
	 */
	private final TrapFile trapFile;

	/**
	 * The {@link RuleEngine} which will drive the logic generating the
	 * {@link Tuple}s.
	 */
	private final RuleEngine engine;

	public GenerateTrapFile(File sourceFile) {
		trapFile = new TrapFile(sourceFile);
		engine = new RuleEngine(trapFile);

		// text : map $. ; [CFLOW]
		engine.add( //
				first(start(TEXT)), //
				closure(all( //
						RULES.applyMatchingRule(), //
						CFLOW //
				)) //
		);

		// [HALSTEAD]
		engine.add(HALSTEAD_DATA, TRAP_HALSTEAD_DATA);

		// [NUMLINES]
		engine.atStart(closure(NUMLINES));
	}

	/**
	 * Any {@link Data} we receive is given to the {@link #engine}.
	 */
	@Override
	public void push(Data d) {
		engine.push(d);
	}

	/**
	 * When the stream is done, we also tell the {@link #engine}.
	 * <p>
	 * After that we
	 * {@linkplain TrapFile#validateTuplesAgainstDatabaseScheme()}, and if that
	 * yields a positive result we store the final tuples. If something is wrong
	 * this throws a {@link CatastrophicError} instead.
	 */
	@Override
	public void done() {
		engine.done();
		super.done();

		Timing.end("parse + generation");

		trapFile.clearNonTrappableTuples();

		final boolean valid = trapFile.validateTuplesAgainstDatabaseScheme();

		if (valid) {
			trapFile.storeTuples(new DefaultTrapWriterFactory(LegacyLanguage.COBOL));

		} else {
			LOGGER.error("Failed to trap: " + trapFile);
			throw new CatastrophicError("Failed to trap: " + trapFile);
		}
	}
}
