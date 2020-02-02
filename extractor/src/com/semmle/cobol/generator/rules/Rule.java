package com.semmle.cobol.generator.rules;

import static com.semmle.cobol.generator.effects.Effects.RETURN;
import static com.semmle.cobol.generator.effects.Effects.all;
import static com.semmle.cobol.generator.effects.Effects.atEnd;
import static com.semmle.cobol.generator.effects.Effects.sub;

import java.util.Comparator;

import com.semmle.cobol.generator.effects.Effect;
import com.semmle.cobol.generator.effects.Effects;
import com.semmle.cobol.generator.engine.Frame;
import com.semmle.cobol.generator.engine.RuleEngine;
import com.semmle.cobol.generator.events.Event;
import com.semmle.cobol.generator.events.TreePath.Node;

import koopa.core.data.markers.Start;

/**
 * A rule defines which {@link Effect} needs to be run when mapping a certain
 * path in the stream.
 */
class Rule {

	/**
	 * Rule precedence is dictated by path length.
	 */
	public static final Comparator<Rule> BY_PATH_LENGTH_DESCENDING = new Comparator<Rule>() {
		@Override
		public int compare(Rule a, Rule b) {
			return b.path.length - a.path.length;
		}
	};

	/**
	 * The path is defined as a sequence of {@link Start}s, which are to be
	 * matched exactly against the path on the {@link Event}.
	 */
	private final Start[] path;

	/**
	 * The key matches the most specific node in the {@link #path}, and is how
	 * {@link Rule}s will be grouped in the {@link RuleSet}.
	 */
	private final Start key;

	/**
	 * This is the {@link Effect} the rule associates with occurrences the
	 * {@link #path}.
	 */
	private final Effect effect;

	public Rule(Start[] path, Effect effect) {
		this.path = path;
		this.key = path[path.length - 1];
		this.effect = effect;
	}

	public Start key() {
		return key;
	}

	/**
	 * Apply the {@link Effect} of this rule.
	 * <p>
	 * The {@link #effect} will be run in a sub {@link Frame} of the current
	 * one, and any tuple which gets built will be {@linkplain Effects#RETURN}ed
	 * at the end.
	 */
	public void apply(Event event, Frame frame, RuleEngine engine) {
		sub(all( //
				effect, //
				atEnd(RETURN) //
		)).apply(event, frame, engine);
	}

	/**
	 * Compare the expected path to the one on the event, and return whether or
	 * not this rule applies to it.
	 */
	public boolean appliesTo(Event event) {
		Node n = event.after.parent;
		for (int i = path.length - 2; i >= 0; i--) {
			if (n == null || n.data != path[i])
				return false;
			else
				n = n.parent;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append(path[0].toString());
		for (int i = 1; i < path.length; i++) {
			b.append("/");
			b.append(path[i].toString());
		}
		b.append(" : ");
		b.append(effect.toString());

		return b.toString();
	}
}