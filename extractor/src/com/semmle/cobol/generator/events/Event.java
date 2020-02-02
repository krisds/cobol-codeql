package com.semmle.cobol.generator.events;

import com.semmle.cobol.generator.events.TreePath.Node;

import koopa.core.data.Data;

/**
 * Clients should not hold on to this event beyond the callback to which it was
 * provided. Instances of this class may/will be reused.
 */
public class Event {
	public static enum Type {
		START, END, TOKEN, PRECONDITION, HALSTEAD
	}

	public Type type = null;

	public Data data = null;
	public Node before = null;
	public Node after = null;

	/**
	 * Return the right node depending on the event. This is always
	 * {@link #after}, except when the {@link #type} is {@link Type#END}, in
	 * which case this return {@link #before}.
	 * <p>
	 * The effect is that we refer to the subtree being processed.
	 */
	public Node getNode() {
		switch (type) {
		case END:
			return before;
		default:
			return after;
		}
	}

	@Override
	public String toString() {
		final Node node = getNode();
		return "{ #" + (node == null ? 0 : node.depth) + " " + type + " " + node
				+ " | " + data.toString() + " }";
	}
}
