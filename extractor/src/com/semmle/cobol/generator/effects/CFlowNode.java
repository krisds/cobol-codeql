package com.semmle.cobol.generator.effects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.semmle.cobol.generator.events.TreePath.Node;
import com.semmle.cobol.generator.tuples.Tuple;

import koopa.core.data.Data;
import koopa.core.data.markers.Start;

/**
 * A single node in a {@link CFlowGraph}.
 */
public class CFlowNode {
	/**
	 * What kind of node is this ?
	 */
	public final Data data;

	/**
	 * What's the {@link Tuple} attached to this node ?
	 */
	protected Tuple tuple = null;

	/**
	 * What was the {@link Node} equivalent to this one ?
	 */
	protected Node node = null;

	/**
	 * What {@link CFlowNode} is this one's parent ?
	 */
	private CFlowNode parent = null;

	/**
	 * If we have a {@link #parent}, what child are we ?
	 */
	private int index = -1;

	/**
	 * What child {@link CFlowNode}s do we have ?
	 */
	private final List<CFlowNode> children = new ArrayList<>();

	public CFlowNode(Data data, Node node) {
		this.data = data;
		this.node = node;
	}

	/**
	 * Add a child node.
	 */
	public void add(CFlowNode n) {
		if (n == NULL)
			throw new UnsupportedOperationException();

		n.index = children.size();
		children.add(n);
		n.parent = this;
	}

	public CFlowNode getParent() {
		return parent;
	}

	public Tuple getTuple() {
		return tuple;
	}

	public CFlowNode firstChild() {
		if (!children.isEmpty())
			return children.get(0);
		else
			return NULL;
	}

	/**
	 * Assumes there's only one match expected. So returns the first one it
	 * finds in a scan of the child nodes.
	 */
	public CFlowNode get(Start child) {
		for (CFlowNode c : children)
			if (c.data == child)
				return c;

		return NULL;
	}

	/**
	 * Returns the first child which matches any of the given types.
	 */
	public CFlowNode first(Start... starts) {
		for (int i = 0; i < children.size(); i++) {
			CFlowNode c = children.get(i);
			for (Start s : starts)
				if (c.data == s)
					return c;
		}

		return NULL;
	}

	/**
	 * Returns the last child which matches any of the given types.
	 */
	public CFlowNode last(Start... starts) {
		for (int i = children.size() - 1; i >= 0; i--) {
			CFlowNode c = children.get(i);
			for (Start s : starts)
				if (c.data == s)
					return c;
		}

		return NULL;
	}

	/**
	 * Returns all children which match the given type.
	 */
	public List<CFlowNode> all(Start child) {
		final List<CFlowNode> all = new LinkedList<>();

		for (CFlowNode c : children)
			if (c.data == child)
				all.add(c);

		return all;
	}

	/**
	 * Returns all children which match any of the given type.
	 */
	public List<CFlowNode> all(Collection<Data> starts) {
		final List<CFlowNode> all = new LinkedList<>();

		for (CFlowNode c : children)
			if (starts.contains(c.data))
				all.add(c);

		return all;
	}

	/**
	 * Returns all descendants which match the given type.
	 */
	public List<CFlowNode> findAll(Start descendant) {
		final List<CFlowNode> all = new LinkedList<>();

		for (CFlowNode d : depthFirst())
			if (d.data == descendant)
				all.add(d);

		return all;
	}

	/**
	 * Returns the last descendant which matches the given type.
	 */
	public CFlowNode findLast(Start descendant) {
		for (CFlowNode d : reverseDepthFirst())
			if (d.data == descendant)
				return d;

		return NULL;
	}

	/**
	 * Returns the first sibling following this one which matches the given
	 * type.
	 */
	public CFlowNode next(Start... starts) {
		if (parent == null)
			return NULL;

		final List<CFlowNode> siblings = parent.children;
		for (int i = index + 1; i < siblings.size(); i++) {
			final CFlowNode sibling = siblings.get(i);
			for (Start s : starts)
				if (sibling.data == s)
					return sibling;
		}

		return NULL;
	}

	/**
	 * Returns the closest ancestor which matches any of the given type.
	 */
	public CFlowNode closest(Start... starts) {
		CFlowNode p = parent;

		while (p != null) {
			for (Start s : starts)
				if (p.data == s)
					return p;

			p = p.parent;
		}

		return NULL;
	}

	/**
	 * Returns the closest ancestor which matches any of the given type.
	 */
	public CFlowNode closest(Collection<Data> starts) {
		CFlowNode p = parent;

		while (p != null) {
			if (starts.contains(p.data))
				return p;

			p = p.parent;
		}

		return NULL;
	}

	/**
	 * Gives an {@link Iterable} which will present the descendants of this node
	 * in depth-first order.
	 */
	public Iterable<CFlowNode> depthFirst() {
		return new Iterable<CFlowNode>() {
			@Override
			public Iterator<CFlowNode> iterator() {
				final Deque<CFlowNode> dq = new LinkedList<>();
				dq.addFirst(CFlowNode.this);

				return new Iterator<CFlowNode>() {
					@Override
					public boolean hasNext() {
						return !dq.isEmpty();
					}

					@Override
					public CFlowNode next() {
						final CFlowNode next = dq.removeFirst();

						if (!next.children.isEmpty())
							for (int i = next.children.size() - 1; i >= 0; i--)
								dq.addFirst(next.children.get(i));

						return next;
					}
				};
			}
		};
	}

	/**
	 * Gives an {@link Iterable} which will present the descendants of this node
	 * in reverse depth-first order.
	 */
	public Iterable<CFlowNode> reverseDepthFirst() {
		return new Iterable<CFlowNode>() {
			@Override
			public Iterator<CFlowNode> iterator() {
				final Deque<CFlowNode> dq = new LinkedList<>();
				dq.addFirst(CFlowNode.this);

				return new Iterator<CFlowNode>() {
					@Override
					public boolean hasNext() {
						return !dq.isEmpty();
					}

					@Override
					public CFlowNode next() {
						final CFlowNode next = dq.removeLast();

						if (!next.children.isEmpty())
							dq.addAll(next.children);

						return next;
					}
				};
			}
		};
	}

	/**
	 * Utility method which returns this node in a singleton list.
	 */
	public List<CFlowNode> asList() {
		return Collections.singletonList(this);
	}

	/**
	 * Utility method which says whether this node is equivalent to
	 * <code>null</code>.
	 */
	public boolean isNull() {
		return false;
	}

	@Override
	public String toString() {
		return data.toString() + "#" + ID;
	}

	/**
	 * The intent of the NULL CFlowNode is to allow chaining of lookup methods
	 * even when an intermediary one would return <code>null</code>.
	 * <p>
	 * This instance should throw exceptions on any call which would modify it,
	 * and return NULL/itself, or the empty list, on any further lookup method.
	 */
	private static final CFlowNode NULL = new CFlowNode(null, null) {
		@Override
		public String toString() {
			return "NULLLLLLLL";
		}

		@Override
		public List<CFlowNode> asList() {
			return Collections.emptyList();
		}

		@Override
		public boolean isNull() {
			return true;
		}

		@Override
		public void add(CFlowNode n) {
			throw new UnsupportedOperationException();
		}

		@Override
		public CFlowNode firstChild() {
			return this;
		}

		@Override
		public CFlowNode get(Start child) {
			return this;
		}

		@Override
		public CFlowNode first(Start... starts) {
			return this;
		}

		@Override
		public CFlowNode last(Start... starts) {
			return this;
		}

		@Override
		public List<CFlowNode> all(Start child) {
			return Collections.emptyList();
		}

		@Override
		public List<CFlowNode> all(Collection<Data> starts) {
			return Collections.emptyList();
		}

		@Override
		public List<CFlowNode> findAll(Start descendant) {
			return Collections.emptyList();
		}

		@Override
		public Iterable<CFlowNode> depthFirst() {
			throw new UnsupportedOperationException();
		}

		@Override
		public CFlowNode next(Start... starts) {
			return this;
		}

		@Override
		public CFlowNode getParent() {
			return this;
		}

		@Override
		public CFlowNode closest(Start... starts) {
			return this;
		}

		@Override
		public CFlowNode closest(Collection<Data> starts) {
			return this;
		}

		@Override
		public CFlowNode findLast(Start descendant) {
			return this;
		}
	};

	// ------------------------------------------------------------------------

	private static int NEXT_ID = 0;
	private final int ID = NEXT_ID++;

	@Override
	public int hashCode() {
		return ID;
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}
}