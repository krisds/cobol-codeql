package com.semmle.cobol.cflow;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.semmle.util.exception.CatastrophicError;

import koopa.core.data.Data;
import koopa.core.data.markers.Start;

public abstract class Wiring {

	private final Map<Data, Logic> LOGIC = new LinkedHashMap<>();

	public static enum Tag {
		/**
		 * Tag for {@linkplain Data} items which help define control flow.
		 */
		CFLOW,

		/**
		 * Tag for {@linkplain Data} items which act as branches in control
		 * flow.
		 */
		BRANCH
	}

	private final Map<Tag, Set<Data>> TAGS = new LinkedHashMap<>();

	protected Wiring() {
		for (Tag tag : Tag.values())
			TAGS.put(tag, new LinkedHashSet<Data>());
	}

	protected void logic(Start start, Logic logic) {
		LOGIC.put(start, logic);
	}

	protected void tag(Data d, Tag... tags) {
		for (Tag tag : tags)
			TAGS.get(tag).add(d);
	}

	public boolean hasLogic(Data d) {
		return LOGIC.containsKey(d);
	}

	public Logic getLogic(Data d) {
		if (hasLogic(d))
			return LOGIC.get(d);
		else
			throw new CatastrophicError(
					"Missing control flow analysis for " + d);
	}

	public boolean participatesInCFlow(Data d) {
		return TAGS.get(Tag.CFLOW).contains(d);
	}

	public Collection<Data> getNodeTypesTagged(Tag tag) {
		return TAGS.get(tag);
	}
}
