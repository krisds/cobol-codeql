package com.semmle.cobol.generator.events;

import koopa.core.data.Position;
import koopa.core.data.Token;
import koopa.core.data.markers.Start;

/**
 * This represent a path in the tree as found in the data stream.
 */
public class TreePath {

	/**
	 * A node represents a single step in the tree path.
	 */
	public static class Node {
		private final int id;
		public final Node parent;
		public final Start data;

		/** What position were we at when this node was created ? */
		private final Position comesAfter;

		public final int depth;

		/**
		 * What's the start position of the first token within the scope of this
		 * path ? If there is no token, this will equal {@link #comesAfter}.
		 */
		public Position start = null;

		/**
		 * What's the first token within the scope of this path ?
		 */
		public Token startToken = null;

		/**
		 * What's the end position of the last token within the scope of this
		 * path ? If there is no token, this will equal {@link #comesAfter}.
		 */
		public Position end = null;

		/**
		 * What's the last token within the scope of this path ?
		 */
		public Token endToken = null;

		/**
		 * Is this node part of something compiler generated ?
		 */
		public final boolean compilerGenerated;

		public Node(int id, Start data, Position comesAfter, Node parent,
				boolean compilerGenerated) {
			this.id = id;
			this.data = data;
			this.comesAfter = comesAfter;
			this.parent = parent;
			this.depth = parent == null ? 1 : parent.depth + 1;
			this.compilerGenerated = compilerGenerated;
		}

		@Override
		public int hashCode() {
			return id;
		}

		@Override
		public boolean equals(Object obj) {
			// There shouldn't be copies of Nodes, so this should be fine.
			return this == obj;
		}

		private String getPath() {
			if (parent == null)
				return "/" + data.getName();
			else
				return parent.getPath() + "/" + data.getName();
		}

		@Override
		public String toString() {
			return getPath();
		}
	}

	private int nextNodeId = 0;
	private Node current = null;

	/**
	 * Have the path enter a new node, and return that Node.
	 */
	public Node enter(Start s, Position comesAfter, boolean compilerGenerated) {
		current = new Node(nextNodeId, s, comesAfter, current,
				compilerGenerated);
		nextNodeId += 1;
		return current;
	}

	/**
	 * What is the current node for this path ?
	 */
	public Node current() {
		return current;
	}

	/**
	 * Leave the current subtree of this path, and return the node the path ends
	 * up at.
	 */
	public Node leave() {
		current = current.parent;
		return current;
	}

	/**
	 * Provide the start information for any recent nodes which do not have it
	 * yet.
	 */
	public void setStart(Position start, Token startToken) {
		Node c = current;
		while (c != null && c.start == null) {
			c.start = start;
			c.startToken = startToken;
			c = c.parent;
		}
	}

	/**
	 * Provide the end information for any recent nodes which do not have it
	 * yet.
	 */
	public void setEnd(Position end, Token endToken) {
		if (current.start == null) {
			// End but no start ? No token was seen on this path. So we use the
			// position at creation time.
			current.start = current.comesAfter;
			current.end = current.comesAfter;

		} else {
			current.end = end;
			current.endToken = endToken;
		}
	}

	@Override
	public String toString() {
		return current.toString();
	}
}
