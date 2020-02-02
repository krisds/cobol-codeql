package com.semmle.cobol.generator.preconditions;

import koopa.core.data.Data;
import koopa.core.data.markers.Start;

/**
 * A {@link Precondition} which looks for subjects which appear last.
 */
public class IsLast extends Precondition {

	/**
	 * Which {@link Data} are we interested in ?
	 */
	private final Data subject;

	/**
	 * If we saw a {@link #subject}, this is the {@link Result} we're building
	 * for it.
	 */
	private Result pending = null;

	public IsLast(Start subject) {
		this.subject = subject;
	}

	@Override
	public Result process(Data d) {
		Result r = null;

		if (subject == null && d instanceof Start
				|| subject != null && d == subject) {
			// If we found a subject we know the one we saw before can not be
			// last.
			if (pending != null)
				pending.resolve(false);

			r = new Result();
			pending = r;
		}

		return r;
	}

	@Override
	public void done() {
		// If we found a subject, we know it was the last.
		if (pending != null)
			pending.resolve(true);
	}

	@Override
	public String toString() {
		return "is last " + subject;
	}
}
