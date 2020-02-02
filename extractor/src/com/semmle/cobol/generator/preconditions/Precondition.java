package com.semmle.cobol.generator.preconditions;

import koopa.core.data.Data;

/**
 * Something which calculates a condition based on future {@link Data}.
 */
public abstract class Precondition {

	/**
	 * Precondition results are {@link Data} which will become part of the
	 * {@link Data} stream when they resolve to <code>true</code>.
	 */
	public class Result implements Data {
		public final Precondition precondition;
		private boolean resolved = false;
		private boolean passed = false;

		public Result() {
			precondition = Precondition.this;
		}

		public void resolve(boolean finalValue) {
			passed = finalValue;
			resolved = true;
		}

		public boolean isResolved() {
			return resolved;
		}

		public boolean passed() {
			return passed;
		}

		public boolean failed() {
			return !passed;
		}

		@Override
		public String toString() {
			return precondition.toString();
		}
	}

	/**
	 * Process new incoming {@link Data}.
	 * <p>
	 * If the data implies a new {@link Result}, returns a (probably unresolved)
	 * result for that data.
	 */
	public abstract Result process(Data d);

	/**
	 * Resolve all pending {@link Result}s.
	 */
	public abstract void done();
}
