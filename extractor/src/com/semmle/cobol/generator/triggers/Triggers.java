package com.semmle.cobol.generator.triggers;

import static com.semmle.cobol.generator.events.Event.Type.PRECONDITION;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.semmle.cobol.generator.engine.RuleEngine;
import com.semmle.cobol.generator.events.Event;
import com.semmle.cobol.generator.events.TreePath.Node;
import com.semmle.cobol.generator.preconditions.HasChild;
import com.semmle.cobol.generator.preconditions.IsLast;
import com.semmle.cobol.generator.preconditions.IsLastChild;
import com.semmle.cobol.generator.preconditions.Precondition;

import koopa.core.data.Token;
import koopa.core.data.markers.End;
import koopa.core.data.markers.Start;

/**
 * Facade class for defining {@link Trigger}s and {@link TriggerDefinition}s.
 */
public class Triggers {
	/**
	 * Create a {@link Trigger} which fires if all steps have fired in sequence.
	 */
	public static TriggerDefinition chain(TriggerDefinition... definitions) {
		if (definitions == null || definitions.length == 0)
			return null;
		else if (definitions.length == 1)
			return definitions[0];
		else
			return new TriggerDefinitionSequence(definitions);
	}

	/**
	 * This {@link Trigger} always fires.
	 */
	public static final Trigger ANY = new OnAny();

	/**
	 * This {@link Trigger} fires when it sees a {@link Start} element.
	 */
	public static final Trigger START = new OnStart();

	/**
	 * This {@link Trigger} fires when it sees an {@link End} element.
	 */
	public static final Trigger END = new OnEnd();

	/**
	 * This {@link Trigger} fires when it sees a {@link Token} which has program
	 * text.
	 */
	public static final Trigger PROGRAM_TEXT = new OnProgramText();

	/**
	 * This {@link Trigger} fires when it sees a {@link Token}.
	 */
	public static final Trigger TOKEN = new OnToken();

	/**
	 * This {@link Trigger} fires when it sees the given {@link Start} element.
	 */
	public static Trigger start(final Start start) {
		return new BasicTrigger() {
			@Override
			public TriggerState evaluate(Event e) {
				return TriggerState.fromBoolean(start.equals(e.data));
			}

			@Override
			public String toString() {
				return start.toString();
			}
		};
	}

	/**
	 * This {@link Trigger} fires when it sees the given {@link End} element.
	 */
	public static Trigger end(final End end) {
		return new BasicTrigger() {
			@Override
			public TriggerState evaluate(Event e) {
				return TriggerState.fromBoolean(end.equals(e.data));
			}

			@Override
			public String toString() {
				return end.toString();
			}
		};
	}

	public static TriggerDefinition root(TriggerDefinition def) {
		return new BasicTriggerDefinition() {
			@Override
			public int getScope(int scopeAtInstantiation) {
				return 0;
			}

			@Override
			public Trigger getTriggerFor(Event event, RuleEngine engine) {
				return new BasicTrigger() {
					private final Trigger trigger = def.getTriggerFor(event,
							engine);

					@Override
					public int getScope(int scopeAtInstantiation) {
						return 0;
					}

					@Override
					public TriggerState evaluate(Event e) {
						if (e == null)
							return TriggerState.INACTIVE;

						final Node node = e.getNode();
						if (node != null && node.depth == 1)
							return trigger.evaluate(e);
						else
							return TriggerState.INACTIVE;
					}

					@Override
					public String toString() {
						return "root " + trigger;
					}
				};
			}

			@Override
			public String toString() {
				return "root " + def;
			}
		};
	}

	public static TriggerDefinition child(TriggerDefinition def) {
		return child(def, 0);
	}

	public static TriggerDefinition child(TriggerDefinition def, int levelsUp) {
		return new BasicTriggerDefinition() {
			@Override
			public Trigger getTriggerFor(Event event, RuleEngine engine) {
				final Trigger trigger = def.getTriggerFor(event, engine);
				final int depth = (event == null ? 1
						: event.getNode().depth + 1) - levelsUp;

				return new BasicTrigger() {
					@Override
					public TriggerState evaluate(Event e) {
						final int currentDepth = e.getNode().depth;
						// This expects precondition results to appear before
						// start nodes, which is exactly how it was done.
						if (e.type == PRECONDITION && depth == currentDepth + 1)
							return trigger.evaluate(e);
						else if (depth == currentDepth)
							return trigger.evaluate(e);
						else
							return TriggerState.INACTIVE;
					}

					@Override
					public int getScope(int scopeAtInstantiation) {
						return scopeAtInstantiation - levelsUp;
					}

					@Override
					public String toString() {
						return "#" + depth + " and " + trigger;
					}
				};
			}

			@Override
			public int getScope(int scopeAtInstantiation) {
				return scopeAtInstantiation - levelsUp;
			}

			@Override
			public String toString() {
				if (levelsUp == 0)
					return "child " + def;
				else
					return "child (up " + levelsUp + ") " + def;
			}
		};
	}

	public static TriggerDefinition first(final TriggerDefinition def) {
		return new BasicTriggerDefinition() {
			@Override
			public Trigger getTriggerFor(Event event, RuleEngine engine) {
				final Trigger trigger = def.getTriggerFor(event, engine);

				return new BasicTrigger() {
					@Override
					public TriggerState evaluate(Event e) {
						TriggerState a = trigger.evaluate(e);
						if (a.fired)
							return a.andExpired();
						else
							return a;
					}

					@Override
					public String toString() {
						return "first " + trigger;
					}
				};
			}

			@Override
			public String toString() {
				return "first " + def;
			}
		};
	}

	private static final Pattern STEP = Pattern.compile(
			"<(([\\w-]+):)?([\\w-]+)?>(\\[(-1|\\d+)\\]|\\[<(([\\w-]+):)?([\\w-]+)>\\])?");

	public static TriggerDefinition path(String path) {
		String[] steps = path.split("/");

		boolean root = false;
		boolean child = true;
		int levelsUp = 0;

		final List<TriggerDefinition> defs = new ArrayList<>(steps.length);

		for (int i = 0; i < steps.length; i++) {
			String step = steps[i];
			// System.out.println("PATH " + i + " = " + step);

			if (i == 0 && step.isEmpty()) {
				root = true;
				continue;
			}

			if (step.charAt(0) == '@') {
				final String key = step.substring(1);
				Trigger token = null;
				if ("PROGRAM_TEXT".equalsIgnoreCase(key))
					token = PROGRAM_TEXT;

				if (token == null)
					throw new IllegalArgumentException("Unknown type: " + step);
				else if (child)
					defs.add(child(token, levelsUp));
				else {
					defs.add(token);
				}

				root = false;
				child = true;
				levelsUp = 0;

				continue;
			}

			if ("**".equals(step)) {
				root = false;
				child = false;
				continue;

			} else if ("..".equals(step)) {
				root = false;
				child = true;
				levelsUp += 1;
				continue;
			}

			Matcher match = STEP.matcher(step);
			if (match.matches()) {
				String ns = match.group(2);
				if (ns == null)
					ns = "cobol";

				final String nn = match.group(3);

				final String cnt = match.group(5);

				String child_ns = match.group(7);
				final String child_nn = match.group(8);

				TriggerDefinition t;
				if (nn == null)
					t = START;
				else
					t = start(Start.on(ns, nn));

				if (cnt != null) {
					int index = Integer.parseInt(cnt);
					if (index == -1) {
						if (nn != null)
							t = isLastChild(Start.on(ns, nn));
						else
							t = isLastChild(null);
					} else if (index == 1)
						t = first(t);
					else
						t = nth(index, t);

					if (root)
						t = root(t);
					else if (child)
						t = child(t, levelsUp);

				} else if (child_nn != null) {
					if (child_ns == null)
						child_ns = "cobol";

					t = hasChild(Start.on(ns, nn),
							Start.on(child_ns, child_nn));

					if (root)
						t = root(t);
					else if (child)
						t = child(t, levelsUp);

				} else {
					if (root)
						t = root(t);
					else if (child)
						t = child(t, levelsUp);
				}

				root = false;
				child = true;
				levelsUp = 0;
				defs.add(t);

			} else
				throw new IllegalArgumentException("Illegal step: " + step);
		}

		return chain(defs.toArray(new TriggerDefinition[defs.size()]));
	}

	public static TriggerDefinition or(final TriggerDefinition... definitions) {
		return new BasicTriggerDefinition() {
			@Override
			public Trigger getTriggerFor(Event event, RuleEngine engine) {
				final List<Trigger> triggers = new LinkedList<>();
				for (int i = 0; i < definitions.length; i++)
					triggers.add(definitions[i].getTriggerFor(event, engine));

				return new BasicTrigger() {
					@Override
					public TriggerState evaluate(Event event) {
						boolean fired = false;
						boolean expired = true;
						List<TriggerDefinition> also = null;

						Iterator<Trigger> i = triggers.listIterator();
						while (i.hasNext()) {
							final TriggerState a = i.next().evaluate(event);

							fired = fired || a.fired;

							expired = expired && a.expired;
							if (a.expired)
								i.remove();

							if (a.also != null && !a.also.isEmpty()) {
								if (also == null)
									also = new LinkedList<>();
								also.addAll(a.also);
							}
						}

						TriggerState a = TriggerState.fromBoolean(fired);
						if (expired)
							a = a.andExpired();
						return a.also(also);
					}

					@Override
					public String toString() {
						StringBuilder b = new StringBuilder();
						for (int i = 0; i < triggers.size(); i++) {
							if (i > 0)
								b.append(" or ");
							b.append(triggers.get(i).toString());
						}
						return b.toString();
					}
				};
			}

			@Override
			public String toString() {
				StringBuilder b = new StringBuilder();
				for (int i = 0; i < definitions.length; i++) {
					if (i > 0)
						b.append(" or ");
					b.append(definitions[i].toString());
				}
				return b.toString();
			}
		};
	}

	// TODO limit defs to start or ends ?
	public static TriggerDefinition nth(int n, TriggerDefinition def) {
		return new BasicTriggerDefinition() {
			@Override
			public Trigger getTriggerFor(Event event, RuleEngine engine) {
				final Trigger trigger = def.getTriggerFor(event, engine);
				return new BasicTrigger() {
					private int count = 0;
					// private boolean expired = false;

					@Override
					public TriggerState evaluate(Event e) {
						final TriggerState a = trigger.evaluate(e);
						if (a.fired) {
							count += 1;
							if (count == n)
								return a.andExpired();
						}

						return TriggerState.INACTIVE;
					}

					@Override
					public String toString() {
						return n + "-th " + trigger;
					}
				};
			}

			@Override
			public String toString() {
				return n + "-th " + def;
			}
		};
	}

	private static TriggerDefinition hasChild(Start subject, Start child) {
		return new BasicTriggerDefinition() {
			@Override
			public Trigger getTriggerFor(Event event, RuleEngine engine) {
				final Precondition precondition = new HasChild(subject, child);
				engine.addPrecondition(precondition);
				return new OnPrecondition(subject, precondition);
			}

			@Override
			public String toString() {
				return subject + "[" + child + "]";
			}
		};
	}

	public static TriggerDefinition isLastChild(Start subject) {
		return new BasicTriggerDefinition() {

			@Override
			public Trigger getTriggerFor(Event event, RuleEngine engine) {
				final Precondition precondition = new IsLastChild(subject);
				engine.addPrecondition(precondition);
				return new OnPrecondition(subject, precondition);
			}

			@Override
			public String toString() {
				return subject + "[-1]";
			}
		};
	}

	public static TriggerDefinition isLast(Start subject) {
		return new BasicTriggerDefinition() {

			@Override
			public Trigger getTriggerFor(Event event, RuleEngine engine) {
				final Precondition precondition = new IsLast(subject);
				engine.addPrecondition(precondition);
				return new OnPrecondition(subject, precondition);
			}

			@Override
			public String toString() {
				return "last(" + subject + ")";
			}
		};
	}
}
