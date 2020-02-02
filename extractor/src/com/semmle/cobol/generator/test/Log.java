package com.semmle.cobol.generator.test;

import com.semmle.cobol.generator.effects.Effect;
import com.semmle.cobol.generator.engine.Frame;
import com.semmle.cobol.generator.engine.RuleEngine;
import com.semmle.cobol.generator.events.Event;
import com.semmle.cobol.generator.events.TreePath.Node;

/**
 * An {@link Effect} which logs info about the events it sees. It tracks depths,
 * event types, and node names.
 */
class Log implements Effect {
	final int id;

	final StringBuilder depth = new StringBuilder();
	final StringBuilder types = new StringBuilder();
	final StringBuilder nodes = new StringBuilder();

	public Log() {
		id = 0;
	}

	public Log(int id) {
		this.id = id;
	}

	@Override
	public void apply(Event event, Frame frame, RuleEngine engine) {
		final Node node = event.getNode();
		depth.append("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
				.charAt(node == null ? 0 : node.depth));

		switch (event.type) {
		case START:
			types.append('(');
			break;
		case TOKEN:
			types.append('T');
			break;
		case END:
			types.append(')');
			break;
		default:
			types.append('?');
			break;
		}

		nodes.append(node == null ? "-" : node.data.getName().charAt(0));
	}

	public String getDepth() {
		return depth.toString();
	}

	public String getTypes() {
		return types.toString();
	}

	public String getNodes() {
		return nodes.toString();
	}

	@Override
	public String toString() {
		if (id > 0)
			return "log " + id;
		else
			return "log";
	}
}