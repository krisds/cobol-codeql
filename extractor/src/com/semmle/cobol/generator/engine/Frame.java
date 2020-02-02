package com.semmle.cobol.generator.engine;

import java.util.List;

import com.semmle.cobol.generator.effects.Effect;
import com.semmle.cobol.generator.events.TreePath.Node;
import com.semmle.cobol.generator.tuples.Tuple;

import koopa.core.data.Data;

/**
 * A place where {@link Effect}s can store data while processing.
 */
public class Frame {

	/**
	 * The {@link Tuple} is what everything is really about.
	 */
	public Tuple tuple = null;

	/**
	 * {@link Node} linked to the execution of this Frame and, therefore, to the
	 * {@link #tuple}.
	 */
	public Node node = null;

	/**
	 * True if it's ok for a rule not to return a tuple.
	 */
	public boolean mayBeOmitted = false;

	/**
	 * Used for collecting {@link Data} as part of the execution of an
	 * {@link Effect}.
	 */
	public List<Data> data = null;

	/**
	 * Frames can have a parent, which creates a kind of call stack.
	 */
	private Frame parent = null;

	public Frame() {
	}

	private Frame(Frame parent) {
		this.parent = parent;
	}

	/**
	 * Create a new {@link Frame} with this {@link Frame} as its parent.
	 */
	public Frame push() {
		return new Frame(this);
	}

	/**
	 * Copy the values stored on this frame to its {@link #parent}.
	 */
	public void pushValuesToParent() {
		parent.node = node;
		parent.tuple = tuple;
		parent.mayBeOmitted = mayBeOmitted;
	}

	/**
	 * Walk the {@link #parent} chain, starting with this frame, returning the
	 * first one which has a {@link #tuple}.
	 */
	public Frame findFrameWithTuple() {
		if (tuple != null)
			return this;
		else
			return findAncestorWithTuple();
	}

	/**
	 * Walk the {@link #parent} chain, starting with our {@link #parent},
	 * returning the first one which has a {@link #tuple}.
	 */
	public Frame findAncestorWithTuple() {
		Frame f = parent;

		while (f != null && f.tuple == null)
			f = f.parent;

		return f;
	}

	@Override
	public String toString() {
		String s = "#" + hashCode();
		if (tuple != null)
			s += "[" + tuple + "]";
		if (parent == null)
			return s;
		return parent.toString() + " | " + s;
	}
}
