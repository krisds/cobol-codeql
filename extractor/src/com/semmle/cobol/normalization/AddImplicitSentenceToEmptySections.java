package com.semmle.cobol.normalization;

import static com.semmle.cobol.util.Common.PARAGRAPH;
import static com.semmle.cobol.util.Common.SECTION;
import static com.semmle.cobol.util.Common.SENTENCE;
import static com.semmle.cobol.util.Common._SECTION;

import koopa.core.data.Data;
import koopa.core.data.Position;
import koopa.core.data.Replaced;
import koopa.core.data.Token;

/**
 * This looks for sections in the stream which have no sentences or paragraphs
 * inside. To each of these it then adds an implicit empty sentence. Its
 * position will match the end of the section's last token. Its length will be
 * zero. The token for this implicit sentence will be tagged as
 * {@linkplain NormalizationTag#COMPILER_GENERATED}.
 */
public class AddImplicitSentenceToEmptySections extends AddImplicitSentence {

	private static enum State {
		WAITING_FOR_SECTION, //
		WAITING_FOR_END_OF_SECTION
	}

	private State state = State.WAITING_FOR_SECTION;
	private boolean sentences;
	private boolean paragraphs;
	private Position end = null;
	private Replaced replaced = null;

	@Override
	public void push(Data d) {
		switch (state) {
		case WAITING_FOR_SECTION:
			pass(d);
			if (d == SECTION) {
				state = State.WAITING_FOR_END_OF_SECTION;
				// We will need to know whether this section had any sentences
				// or paragraphs.
				sentences = false;
				paragraphs = false;
				// If we generate an empty sentence we need to have the right
				// location info for it. As said, this will match the end of the
				// last token in the section.
				end = Position.ZERO;
				replaced = null;
			}
			break;

		case WAITING_FOR_END_OF_SECTION:
			// We're inside a section, which means tracking sentences and
			// paragraphs, and waiting for the end of the section.
			if (d == _SECTION) {
				// If there were no sentences and paragraphs in this section we
				// insert an implicit empty sentence.
				if (!sentences && !paragraphs)
					passNewImplicitSentence(end, replaced);
				state = State.WAITING_FOR_SECTION;

			} else if (!sentences && d == SENTENCE)
				sentences = true;

			else if (!paragraphs && d == PARAGRAPH)
				paragraphs = true;

			else if (d instanceof Token) {
				final Token t = (Token) d;
				end = t.getEnd();
				replaced = t.getReplaced();
			}

			pass(d);
			break;

		default:
			pass(d);
		}
	}
}
