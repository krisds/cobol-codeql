package com.semmle.cobol.generator.preconditions;

import java.util.Stack;

import koopa.core.data.Data;
import koopa.core.data.markers.End;
import koopa.core.data.markers.Start;

/**
 * A {@link Precondition} which looks for subjects which have a certain child.
 */
public class HasChild extends Precondition {

	private class ChildResult extends Result {
		final int depth;

		public ChildResult(int depth) {
			this.depth = depth;
		}
	}

	/**
	 * Which {@link Data} are we interested in ?
	 */
	private final Data subject;

	/**
	 * What child are we looking for ?
	 */
	private final Start child;

	/**
	 * Pending results.
	 */
	private Stack<ChildResult> results = new Stack<>();

	/**
	 * We track depth so we can resolve siblings.
	 */
	private int depth = 0;

	public HasChild(Start subject, Start child) {
		this.subject = subject;
		this.child = child;
	}

	@Override
	public Result process(Data d) {
		ChildResult r = null;

		if (d instanceof Start) {
			// If we found a child, see if it belongs to a subject, and resolve
			// that subject.
			if (d == child && !results.isEmpty()
					&& results.peek().depth == depth - 1)
				results.pop().resolve(true);

			// If we found a subject, set up a pending result.
			if (d == subject)
				results.push(r = new ChildResult(depth));

			depth += 1;

		} else if (d instanceof End) {
			depth -= 1;

			// At the end of a subtree, see if we are still resolving a subject.
			// If so, we failed to find a matching child for it.
			if (!results.isEmpty() && results.peek().depth > depth)
				results.pop().resolve(false);
		}

		return r;
	}

	@Override
	public void done() {
		// Any pending results can now be resolved negatively.
		while (!results.isEmpty())
			results.pop().resolve(false);
	}

	@Override
	public String toString() {
		return subject + "[" + child + "]";
	}
}
