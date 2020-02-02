package com.semmle.cobol.normalization;

import static com.semmle.cobol.util.Common.DECLARATIVES;
import static com.semmle.cobol.util.Common.DECLARATIVE_SECTION;
import static com.semmle.cobol.util.Common.PARAGRAPH;
import static com.semmle.cobol.util.Common.SENTENCE;
import static com.semmle.cobol.util.Common._DECLARATIVES;
import static com.semmle.cobol.util.Common._DECLARATIVE_SECTION;

import koopa.core.data.Data;
import koopa.core.data.Position;
import koopa.core.data.Replaced;
import koopa.core.data.Token;

/**
 * This looks for declaratives sections in the stream which have no sentences or
 * paragraphs inside. To each of these it then adds an implicit empty sentence.
 * Its position will match the end of the declarative section's last token. Its
 * length will be zero. The token for this implicit sentence will be tagged as
 * {@linkplain NormalizationTag#COMPILER_GENERATED}.
 */
public class AddImplicitSentenceToEmptyDeclarativeSections
		extends AddImplicitSentence {

	private static enum State {
		WAITING_FOR_DECLARATIVES, //
		IN_DECLARATIVES, //
		WAITING_FOR_END_OF_DECLARATIVE_SECTION
	}

	private State state = State.WAITING_FOR_DECLARATIVES;

	private int sentences = 0;
	private boolean paragraphs;
	private Position end = null;
	private Replaced replaced = null;

	@Override
	public void push(Data d) {
		switch (state) {
		case WAITING_FOR_DECLARATIVES:
			// We're waiting for the start of declaratives. A COBOL program can
			// only have one of these, though there may be multiple programs in
			// a single file.
			pass(d);
			if (d == DECLARATIVES)
				state = State.IN_DECLARATIVES;
			break;

		case IN_DECLARATIVES:
			// We're inside the "DECLARATIVES". Here we expect to find
			// declaratives sections, or the end of the declaratives itself.
			pass(d);
			if (d == _DECLARATIVES)
				state = State.WAITING_FOR_DECLARATIVES;
			else if (d == DECLARATIVE_SECTION) {
				state = State.WAITING_FOR_END_OF_DECLARATIVE_SECTION;
				// We will need to know how many sentences are in the
				// declaratives section, and whether or not this section had any
				// paragraphs.
				sentences = 0;
				paragraphs = false;
				// If we generate an empty sentence we need to have the right
				// location info for it. As said, this will match the end of the
				// last token in the section.
				end = Position.ZERO;
				replaced = null;
			}
			break;

		case WAITING_FOR_END_OF_DECLARATIVE_SECTION:
			// We're inside a declaratives section, which means tracking
			// sentences and paragraphs, and waiting for the end of the section.
			if (d == _DECLARATIVE_SECTION) {
				// When we find the end we will insert an empty sentence into
				// the stream iff there were no paragraphs and no sentences.
				// Note, however, that the start of a declaratives section will
				// be a sentence holding a USE statement, which doesn't count
				// towards the total. Rather than trying to ignore it when we
				// see it in the stream, we tweak the test to see if there is no
				// more than one sentence.
				if (sentences <= 1 && !paragraphs)
					passNewImplicitSentence(end, replaced);
				state = State.IN_DECLARATIVES;

			} else if (sentences <= 1 && SENTENCE.equals(d))
				sentences += 1;

			else if (!paragraphs && PARAGRAPH.equals(d))
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
