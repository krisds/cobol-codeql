package com.semmle.cobol.generator.test;

import com.semmle.cobol.generator.effects.Effect;
import com.semmle.cobol.generator.engine.Frame;
import com.semmle.cobol.generator.engine.RuleEngine;
import com.semmle.cobol.generator.events.Event;

/**
 * Support class to capture a trace of {@link Effect} execution, which lets you
 * use it for verification in testing.
 */
class Trace {
	final StringBuilder trace = new StringBuilder();

	/**
	 * Return a new {@link Effect} which will add the given marker to the trace
	 * whenever it is executed.
	 */
	public Effect mark(String marker) {
		return new Effect() {
			@Override
			public void apply(Event event, Frame frame, RuleEngine engine) {
				trace.append(marker);
			}
		};
	}

	public String getTrace() {
		return trace.toString();
	}
}