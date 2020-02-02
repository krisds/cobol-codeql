package com.semmle.cobol.generator.effects;

import com.semmle.cobol.generator.engine.Frame;
import com.semmle.cobol.generator.engine.RuleEngine;
import com.semmle.cobol.generator.events.Event;
import com.semmle.cobol.generator.triggers.Trigger;

/**
 * An Effect is some logic which gets run by the {@link RuleEngine} in response
 * to a {@link Trigger}.
 */
public interface Effect {

	void apply(Event event, Frame frame, RuleEngine engine);
}
