package com.semmle.cobol.generator.triggers;

import com.semmle.cobol.generator.effects.Effect;
import com.semmle.cobol.generator.events.Event;

/**
 * Something which evaluates an {@link Event} and lets you know whether that
 * should trigger any {@link Effect} associated with it.
 * <p>
 * Triggers can be stateful and rely on the information from past events to
 * decide whether to fire on the latest one.
 * <p>
 * All triggers are {@link TriggerDefinition}s which instantiate to themselves.
 */
public interface Trigger extends TriggerDefinition {

	TriggerState evaluate(Event event);
}
