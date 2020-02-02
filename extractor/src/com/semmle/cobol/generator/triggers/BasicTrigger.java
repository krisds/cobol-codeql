package com.semmle.cobol.generator.triggers;

import com.semmle.cobol.generator.engine.RuleEngine;
import com.semmle.cobol.generator.events.Event;

public abstract class BasicTrigger extends BasicTriggerDefinition
		implements Trigger {

	/**
	 * {@link Trigger}s return themselves when used as
	 * {@link TriggerDefinition}.
	 */
	@Override
	public Trigger getTriggerFor(Event event, RuleEngine engine) {
		return this;
	}
}
