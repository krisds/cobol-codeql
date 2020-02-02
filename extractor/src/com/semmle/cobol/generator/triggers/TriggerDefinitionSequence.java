package com.semmle.cobol.generator.triggers;

import java.util.Collections;

import com.semmle.cobol.generator.engine.RuleEngine;
import com.semmle.cobol.generator.events.Event;

class TriggerDefinitionSequence implements TriggerDefinition {

	private final TriggerDefinition[] definitions;
	private final int index;

	public TriggerDefinitionSequence(TriggerDefinition... definitions) {
		this.definitions = definitions;
		this.index = 0;
	}

	private TriggerDefinitionSequence(TriggerDefinition[] definitions,
			int index) {
		this.definitions = definitions;
		this.index = index;
	}

	@Override
	public int getScope(int scopeAtInstantiation) {
		return definitions[0].getScope(scopeAtInstantiation);
	}

	@Override
	public Trigger getTriggerFor(Event event, RuleEngine engine) {
		final Trigger t = definitions[index].getTriggerFor(event, engine);

		return new BasicTrigger() {
			@Override
			public TriggerState evaluate(Event event) {
				final TriggerState a = t.evaluate(event);
				if (!a.fired || index + 1 >= definitions.length)
					return a;

				return new TriggerState(false, a.expired,
						Collections.singletonList(new TriggerDefinitionSequence(
								definitions, index + 1)));
			}

			@Override
			public int getScope(int scopeAtInstantiation) {
				return t.getScope(scopeAtInstantiation);
			}

			@Override
			public String toString() {
				final StringBuilder b = new StringBuilder(t.toString());

				for (int i = index + 1; i < definitions.length; i++) {
					b.append(" and then ");
					b.append(definitions[i].toString());
				}

				return b.toString();
			}
		};
	}
}
