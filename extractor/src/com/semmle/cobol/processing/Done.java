package com.semmle.cobol.processing;

import com.semmle.cobol.extractor.StreamProcessingStep;

import koopa.core.data.Data;

/**
 * Just an empty stage, like a <code>/dev/null</code>, to help simplify the
 * stream configuration and logic.
 * <p>
 * Basically this allows each {@link StreamProcessingStep} to assume there will
 * be a next, non-<code>null</code> step.
 */
public class Done extends StreamProcessingStep {

	@Override
	public void push(Data d) {
	}

	@Override
	public void done() {
	}
}
