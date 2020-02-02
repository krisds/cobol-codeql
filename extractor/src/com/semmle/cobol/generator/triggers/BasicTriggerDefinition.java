package com.semmle.cobol.generator.triggers;

abstract class BasicTriggerDefinition implements TriggerDefinition {
	/**
	 * By default the scope for a {@link Trigger} is the one at which it was
	 * asked for.
	 */
	@Override
	public int getScope(int scopeAtStart) {
		return scopeAtStart;
	}
}
