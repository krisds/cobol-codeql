package com.semmle.cobol.generator.test;

import java.util.LinkedList;
import java.util.List;

import com.semmle.cobol.generator.preconditions.Precondition;

import koopa.core.data.Data;

/**
 * This {@link Precondition} wraps a given one, and will log the results of that
 * one for inspection in tests.
 */
class PreconditionLog extends Precondition {

	private final Precondition beingLogged;

	private final List<Result> results = new LinkedList<>();
	private boolean done = false;
	private boolean allResolved = true;
	private final StringBuilder resolutions = new StringBuilder();

	public PreconditionLog(Precondition beingLogged) {
		this.beingLogged = beingLogged;
	}

	@Override
	public Result process(Data d) {
		final Result r = beingLogged.process(d);

		if (r != null)
			results.add(r);

		return r;
	}

	@Override
	public void done() {
		beingLogged.done();

		done = true;

		for (Result result : results) {
			allResolved = allResolved && result.isResolved();
			resolutions.append(result.passed() ? "T" : "F");
		}
	}

	public boolean isDone() {
		return done;
	}

	public boolean allResolved() {
		return allResolved;
	}

	public String resolutions() {
		return resolutions.toString();
	}
}
