package com.semmle.cobol.normalization;

import static com.semmle.cobol.util.Common.SEMMLE__COMPGENERATED;
import static com.semmle.cobol.util.Common.SENTENCE;
import static com.semmle.cobol.util.Common._SEMMLE__COMPGENERATED;
import static com.semmle.cobol.util.Common._SENTENCE;
import static com.semmle.cobol.util.Common.newSeparator;

import com.semmle.cobol.extractor.StreamProcessingStep;

import koopa.core.data.Position;
import koopa.core.data.Replaced;

public abstract class AddImplicitSentence extends StreamProcessingStep {

	protected void passNewImplicitSentence(Position pos, Replaced replaced) {
		pass(SEMMLE__COMPGENERATED);
		pass(SENTENCE);
		pass(newSeparator(".", pos, replaced));
		pass(_SENTENCE);
		pass(_SEMMLE__COMPGENERATED);
	}
}
