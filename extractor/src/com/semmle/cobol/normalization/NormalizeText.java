package com.semmle.cobol.normalization;

import static com.semmle.cobol.util.Common._TEXT;
import static com.semmle.cobol.util.Common.TEXT;
import static com.semmle.cobol.util.Common.isStart;

import com.semmle.cobol.extractor.StreamProcessingStep;

import koopa.core.data.Data;

/**
 * The root node of the AST is for the "text" node we defined in our
 * CobolProjects. This has a side effect: any leading comments and/or directives
 * will be placed appear before "compilationGroup" or "copybook", rather than
 * inside it as we would like. This method normalizes that by moving the leading
 * comments and directives back to where they belong.
 * <p>
 * So we transform:
 * <ul>
 * <li>text</li>
 * <li>
 * <ul>
 * <li>comment</li>
 * <li>compilationGroup</li>
 * <li>
 * <ul>
 * <li>...</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 * <p>
 * into:
 * </p>
 * <ul>
 * <li>text</li>
 * <li>
 * <ul>
 * <li>compilationGroup</li>
 * <li>
 * <ul>
 * <li>comment</li>
 * <li>...</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 */
public class NormalizeText extends StreamProcessingStep {

	private static enum State {
		WAITING_FOR_TEXT_NODE, WAITING_FOR_ROOT_NODE, PASS_THROUGH
	}

	private State state = State.WAITING_FOR_TEXT_NODE;

	private Data startOfText = null;

	@Override
	public void push(Data d) {
		switch (state) {
		case WAITING_FOR_TEXT_NODE:
			// Waiting on the initial <text> node.
			if (d != TEXT)
				delay(d);
			else {
				startOfText = d;
				state = State.WAITING_FOR_ROOT_NODE;
			}
			break;

		case WAITING_FOR_ROOT_NODE:
			// We have seen the <text> node and are now waiting on the root
			// node, which will be the next <>.

			if (d == _TEXT) {
				// An empty file will not have a root node, and we must allow
				// for that.
				state = State.PASS_THROUGH;
				pass(startOfText);
				passAllDelayed();
				pass(d);

			} else if (!isStart(d))
				delay(d);

			else {
				state = State.PASS_THROUGH;
				pass(startOfText);
				pass(d);
				passAllDelayed();
			}
			break;

		case PASS_THROUGH:
		default:
			// We have seen the root node, and normalized the stream. From that
			// point on we can just pass along the remainder of the stream as
			// is.
			pass(d);
		}
	}

	@Override
	public void done() {
		// This handles the edge cases where parsing is done.

		switch (state) {
		case WAITING_FOR_TEXT_NODE:
			// Not seen a <text> node ? Just forward everything delayed.
			passAllDelayed();
			break;

		case WAITING_FOR_ROOT_NODE:
			// Not seen a root node ? Just forward everything delayed.
			pass(startOfText);
			passAllDelayed();
			break;

		case PASS_THROUGH:
		default:
		}

		super.done();
	}
}
