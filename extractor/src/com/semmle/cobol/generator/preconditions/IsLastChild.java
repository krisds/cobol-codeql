package com.semmle.cobol.generator.preconditions;

import java.util.Stack;

import koopa.core.data.Data;
import koopa.core.data.markers.End;
import koopa.core.data.markers.Start;

/**
 * A {@link Precondition} which looks for subjects which appear as the last
 * child among their siblings.
 */
public class IsLastChild extends Precondition {

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
	 * Pending results.
	 */
	private Stack<ChildResult> results = new Stack<>();

	/**
	 * We track depth so we can resolve siblings.
	 */
	private int depth = 0;

	public IsLastChild(Start subject) {
		this.subject = subject;
	}

	@Override
	public Result process(Data d) {
		ChildResult r = null;

		if (d instanceof Start) {
			if (subject == null || d == subject) {
				// We found a(nother) subject. So we can say that any previous
				// sibling we know of is not the last child.
				if (!results.isEmpty() && results.peek().depth == depth)
					results.pop().resolve(false);

				results.push(r = new ChildResult(depth));
			}

			depth += 1;

		} else if (d instanceof End) {
			depth -= 1;

			// We leave a subtree. If we found a subject in that subtree we can
			// now resolve it positively.
			if (!results.isEmpty() && results.peek().depth > depth)
				results.pop().resolve(true);
		}

		return r;
	}

	@Override
	public void done() {
		// Any remaining subjects we saw will be "last children".
		while (!results.isEmpty())
			results.pop().resolve(true);
	}

	@Override
	public String toString() {
		if (subject == null)
			return "<>[-1]";
		else
			return subject + "[-1]";
	}
}
