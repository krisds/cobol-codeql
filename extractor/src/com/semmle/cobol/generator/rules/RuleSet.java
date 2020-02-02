package com.semmle.cobol.generator.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.semmle.cobol.generator.effects.Effect;
import com.semmle.cobol.generator.engine.Frame;
import com.semmle.cobol.generator.engine.RuleEngine;
import com.semmle.cobol.generator.events.Event;
import com.semmle.util.exception.CatastrophicError;

import koopa.core.data.markers.Start;

/**
 * The rule set defines how certain nodes (or more complex paths) need to be
 * processed. It provides several {@linkplain Effect}s which can be used to
 * drive the mapping automatically.
 */
public class RuleSet {

	private final Map<Start, List<Rule>> RULES = new LinkedHashMap<>();

	private Effect applyMatchingRule = null;
	private Map<Start, Effect> applyMatchingRules = new LinkedHashMap<>();
	private Map<Start, Effect> applyRules = new LinkedHashMap<>();

	/**
	 * This defines a new rule, which will map any encounters of the given raw
	 * path to the given effect.
	 * <p>
	 * There should only be one instance of any given path in the ruleset. There
	 * can however be more specific paths defining more specific rules.
	 * <p>
	 * For example, you could define a generic rule for <code>&lt;foo&gt;</code>
	 * which will apply to any <code>&lt;foo&gt;</code> which appears in the
	 * stream. But you can then also define, for instance, a path
	 * <code>&lt;fee&gt;/&lt;foo&gt;</code>. With that, the generic rule will
	 * trigger, unless it is for a <code>&lt;foo&gt;</code> which appears as a
	 * child of <code>&lt;fee&gt;</code>.
	 * <p>
	 * The precendence rule is tied to matching path length. The effect attached
	 * to the longest matching path will be the one returned/executed.
	 */
	public void define(String rawPath, Effect effect) {
		final Start[] path = convertRawPathToStartNodes(rawPath);
		final Rule rule = new Rule(path, effect);

		final Start key = rule.key();
		if (!RULES.containsKey(key))
			RULES.put(key, new ArrayList<>());

		final List<Rule> rules = RULES.get(key);
		rules.add(rule);

		Collections.sort(rules, Rule.BY_PATH_LENGTH_DESCENDING);
	}

	/**
	 * Converts a raw path into a matching series of {@linkplain Start} nodes.
	 * <p>
	 * Legal paths can be a single node description, like:
	 * <ul>
	 * <li><code>&lt;namespace:foo&gt;</code></li>
	 * <li><code>&lt;foo&gt;</code>, which implies namespace "cobol"</li>
	 * </ul>
	 * A legal path can also be a sequence of node descriptions separated by
	 * slashes. E.g. <code>&lt;namespace:foo&gt;/&lt;fee&gt;/&lt;fum&gt;</code>.
	 */
	private Start[] convertRawPathToStartNodes(String rawPath) {
		final String[] rawPathElements = rawPath.split("/");
		final Start[] path = new Start[rawPathElements.length];

		for (int i = 0; i < rawPathElements.length; i++) {
			final String element = rawPathElements[i];
			// TODO Is it worth going full regex on this ?
			if (element.charAt(0) != '<' || element.charAt(1) == '/'
					|| element.charAt(element.length() - 1) != '>')
				throw new IllegalArgumentException("Bad path: " + rawPath);

			final String node = element.substring(1, element.length() - 1);
			final int colon = node.indexOf(':');
			if (colon >= 0) {
				final String namespace = node.substring(0, colon);
				final String name = node.substring(colon + 1);
				path[i] = Start.on(namespace, name);
			} else
				path[i] = Start.on("cobol", node);
		}
		return path;
	}

	/**
	 * Returns an {@linkplain Effect} that, when evaluated, will look for a rule
	 * matching the datum at the time of evaluation. If it finds one, the effect
	 * defined by the rule will be applied.
	 * <p>
	 * See {@linkplain #define(String, Effect)} to learn about rule precedence.
	 */
	public Effect applyMatchingRule() {
		// NOTE. We only build one instance of this per rule set.
		if (applyMatchingRule == null)
			applyMatchingRule = new Effect() {
				@Override
				public void apply(Event event, Frame frame, RuleEngine engine) {
					final List<Rule> applicableRules = RULES.get(event.data);

					if (applicableRules == null || applicableRules.isEmpty())
						return;

					for (Rule rule : applicableRules)
						if (rule.appliesTo(event)) {
							rule.apply(event, frame, engine);
							return;
						}
				}
			};

		return applyMatchingRule;
	}

	/**
	 * Returns an {@linkplain Effect} that, when evaluated, will look for a rule
	 * matching the datum at the time of evaluation. If it finds one, the effect
	 * defined by the rule will be applied. If it doesn't find a rule, the
	 * effect associated with the given default rule will be applied instead.
	 * <p>
	 * See {@linkplain #define(String, Effect)} to learn about rule precedence.
	 */
	public Effect applyMatchingRule(Start defaultRuleStart) {
		// NOTE. We only want to build one instance of this effect per instance
		// of Start.
		if (!applyMatchingRules.containsKey(defaultRuleStart))
			applyMatchingRules.put(defaultRuleStart, new Effect() {
				private Rule defaultRule = null;

				@Override
				public void apply(Event event, Frame frame, RuleEngine engine) {
					final List<Rule> applicableRules = RULES.get(event.data);
					if (applicableRules != null)
						for (Rule rule : applicableRules)
							if (rule.appliesTo(event)) {
								rule.apply(event, frame, engine);
								return;
							}

					// Fall back to default.
					if (defaultRule == null) {
						final List<Rule> defaultRules = RULES
								.get(defaultRuleStart);

						if (defaultRules == null || defaultRules.size() == 0)
							throw new CatastrophicError("No rules for '"
									+ defaultRuleStart + "' found.");

						if (defaultRules.size() > 1)
							throw new CatastrophicError("No unique rule for '"
									+ defaultRuleStart + "' found.");

						defaultRule = defaultRules.get(0);
					}

					defaultRule.apply(event, frame, engine);
				}
			});

		return applyMatchingRules.get(defaultRuleStart);
	}

	/**
	 * Returns an {@linkplain Effect} that, when evaluated, will apply the
	 * default effect associated with the given rule.
	 */
	public Effect applyRule(Start defaultRuleStart) {
		// NOTE. We only want to build one instance of this effect per instance
		// of Start.
		if (!applyRules.containsKey(defaultRuleStart))
			applyRules.put(defaultRuleStart, new Effect() {
				private Rule defaultRule = null;

				@Override
				public void apply(Event event, Frame frame, RuleEngine engine) {
					if (defaultRule == null) {
						final List<Rule> defaultRules = RULES
								.get(defaultRuleStart);

						if (defaultRules == null || defaultRules.size() == 0)
							throw new CatastrophicError("No rules for '"
									+ defaultRuleStart + "' found.");

						// The last rule is always the generic one, as we're
						// sorting by Rule.BY_PATH_LENGTH_DESCENDING.
						defaultRule = defaultRules.get(defaultRules.size() - 1);
					}

					defaultRule.apply(event, frame, engine);
				}
			});

		return applyRules.get(defaultRuleStart);
	}
}
