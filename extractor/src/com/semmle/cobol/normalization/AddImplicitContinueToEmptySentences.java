package com.semmle.cobol.normalization;

import static com.semmle.cobol.util.Common.COMPILER_STATEMENT;
import static com.semmle.cobol.util.Common.CONTINUE;
import static com.semmle.cobol.util.Common.SEMMLE__COMPGENERATED;
import static com.semmle.cobol.util.Common.SENTENCE;
import static com.semmle.cobol.util.Common.STATEMENT;
import static com.semmle.cobol.util.Common._CONTINUE;
import static com.semmle.cobol.util.Common._SEMMLE__COMPGENERATED;
import static com.semmle.cobol.util.Common._SENTENCE;
import static com.semmle.cobol.util.Common._STATEMENT;
import static com.semmle.cobol.util.Common.isDot;
import static com.semmle.cobol.util.Common.newWord;

import com.semmle.cobol.extractor.StreamProcessingStep;

import koopa.core.data.Data;
import koopa.core.data.Token;

/**
 * This looks for sentences in the stream which have no statements inside. To
 * each of these it then adds an implicit CONTINUE statement. Its position will
 * match the start of the sentence's closing dot. Its length will be zero. It
 * will be placed before this closing dot. The token for this implicit CONTINUE
 * statement will be tagged as {@linkplain NormalizationTag#COMPILER_GENERATED}.
 */
public class AddImplicitContinueToEmptySentences extends StreamProcessingStep {

	private static enum State {
		WAITING_FOR_SENTENCE, //
		WAITING_FOR_END_OF_SENTENCE
	}

	private State state = State.WAITING_FOR_SENTENCE;
	private Token closingDot;
	private boolean statements;

	@Override
	public void push(Data d) {
		switch (state) {
		case WAITING_FOR_SENTENCE:
			pass(d);
			if (d == SENTENCE) {
				state = State.WAITING_FOR_END_OF_SENTENCE;
				// We need to know whether there were any statements in this
				// sentence.
				statements = false;
				// And, as said, we need to track the closing dot of this
				// sentence, so we can add the implicit CONTINUE statement in
				// the right location.
				closingDot = null;
			}
			break;

		case WAITING_FOR_END_OF_SENTENCE:
			if (d == _SENTENCE) {
				// If there were no statements we add the implicit CONTINUE
				// statement to the stream.
				if (!statements)
					passImplicitContinue(closingDot);

				state = State.WAITING_FOR_SENTENCE;
				passAllDelayed();
				pass(d);
				break;
			}

			if (!statements && (d == STATEMENT || d == COMPILER_STATEMENT))
				statements = true;

			if (isDot(d)) {
				passAllDelayed();
				closingDot = (Token) d;
				delay(closingDot);

			} else if (closingDot != null)
				delay(d);

			else
				pass(d);

			break;

		default:
			pass(d);
		}
	}

	private void passImplicitContinue(Token token) {
		final Token fakeContinue = newWord("CONTINUE", token.getStart(),
				token.getReplaced());

		pass(SEMMLE__COMPGENERATED);
		pass(STATEMENT);
		pass(CONTINUE);
		pass(fakeContinue);
		pass(_CONTINUE);
		pass(_STATEMENT);
		pass(_SEMMLE__COMPGENERATED);
	}
}
