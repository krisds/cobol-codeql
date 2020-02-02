package com.semmle.cobol.generator.triggers;

import com.semmle.cobol.generator.engine.RuleEngine;
import com.semmle.cobol.generator.events.Event;

/**
 * Something which can instantiate {@link Trigger}s.
 */
public interface TriggerDefinition {

	/**
	 * Return the scope at which a {@link Trigger} returned by
	 * {@link #getTriggerFor(Event, RuleEngine)} should apply.
	 */
	int getScope(int scopeAtStart);

	/**
	 * Return a (possibly new) {@link Trigger} instance for the engine.
	 */
	Trigger getTriggerFor(Event event, RuleEngine engine);
}
