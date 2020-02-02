package com.semmle.cobol.generator.engine;

import com.semmle.cobol.generator.effects.Effect;
import com.semmle.cobol.generator.events.Event;
import com.semmle.cobol.generator.rules.RuleSet;
import com.semmle.cobol.generator.triggers.Trigger;
import com.semmle.cobol.generator.triggers.TriggerState;

/**
 * The active counterpart to a rule defined in a {@link RuleSet} when it is
 * being run in the {@link RuleEngine}.
 *
 */
class ActiveRule {
	/**
	 * Depth or scope at which this rule became active.
	 */
	private final int depth;

	/**
	 * The {@link Trigger} which may activate the {@link #effect}.
	 */
	private final Trigger trigger;

	/**
	 * The {@link Effect} to be run when the {@link #trigger} fires.
	 */
	final Effect effect;

	public ActiveRule(int depth, Trigger trigger, Effect effect) {
		this.depth = depth;
		this.trigger = trigger;
		this.effect = effect;
	}

	public int getDepth() {
		return depth;
	}

	/**
	 * @see Trigger#evaluate(Event)
	 */
	public TriggerState evaluate(Event event) {
		return trigger.evaluate(event);
	}

	/**
	 * @see Effect#apply(Event, Frame, RuleEngine)
	 */
	public void apply(Event event, RuleEngine engine) {
		effect.apply(event, null, engine);
	}

	@Override
	public String toString() {
		return "[ #" + depth + " | " + trigger.toString() + " ]";
	}
}
