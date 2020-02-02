package com.semmle.cobol.normalization;

import static com.semmle.cobol.util.Common._PARAGRAPH;
import static com.semmle.cobol.util.Common.PARAGRAPH;
import static com.semmle.cobol.util.Common.SENTENCE;

import koopa.core.data.Data;
import koopa.core.data.Position;
import koopa.core.data.Replaced;
import koopa.core.data.Token;

/**
 * This looks for paragraphs in the stream which have no sentences inside. To
 * each of these it then adds an implicit empty sentence. Its position will
 * match the end of the paragraph's last token. Its length will be zero. The
 * token for this implicit sentence will be tagged as
 * {@linkplain NormalizationTag#COMPILER_GENERATED}.
 */
public class AddImplicitSentenceToEmptyParagraphs extends AddImplicitSentence {

	private static enum State {
		WAITING_FOR_PARAGRAPH, //
		WAITING_FOR_END_OF_PARAGRAPH
	}

	private State state = State.WAITING_FOR_PARAGRAPH;
	private Position end = null;
	private Replaced replaced = null;
	private boolean sentences;

	@Override
	public void push(Data d) {
		switch (state) {
		case WAITING_FOR_PARAGRAPH:
			pass(d);
			if (d == PARAGRAPH) {
				state = State.WAITING_FOR_END_OF_PARAGRAPH;
				// We need to know whether there were any sentences inside the
				// paragraph.
				sentences = false;
				// If we generate an empty sentence we need to have the right
				// location info for it. As said, this will match the end of the
				// last token in the paragraph.
				end = Position.ZERO;
				replaced = null;
			}
			break;

		case WAITING_FOR_END_OF_PARAGRAPH:
			// We're inside a paragraph, which means tracking sentences and
			// waiting for the end of the paragraph.
			if (d == _PARAGRAPH) {
				// If there were no sentences in the paragraph we add an
				// implicit empty sentence.
				if (!sentences)
					passNewImplicitSentence(end, replaced);
				state = State.WAITING_FOR_PARAGRAPH;

			} else if (!sentences && d == SENTENCE)
				sentences = true;

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
