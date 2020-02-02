package com.semmle.cobol.mapping.conversion;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import koopa.core.trees.Tree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.semmle.util.exception.CatastrophicError;

public class TypeConversion<T> {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(TypeConversion.class);

	private Conversion conversion;
	private Map<String, Convertor<T>> REGISTRY = new LinkedHashMap<String, Convertor<T>>();

	public TypeConversion(Conversion conversion, Class<T> clazz) {
		this.conversion = conversion;
		this.conversion.register(clazz, this);
	}

	public void register(String name, Convertor<T> convertor) {
		REGISTRY.put(name, convertor);
	}

	public T loadFrom(Tree definition) {
		String name = definition.getName();

		if (REGISTRY.containsKey(name))
			return REGISTRY.get(name).applyTo(definition);

		else {
			String msg = "Unknown type: " + name;
			LOGGER.error(msg);
			throw new CatastrophicError(msg);
		}
	}

	protected <S> S loadFrom(Tree definition, Class<S> clazz) {
		return conversion.getTypeConversion(clazz).loadFrom(definition);
	}

	public List<T> loadAllFrom(List<Tree> nodes) {
		if (nodes == null || nodes.isEmpty())
			return Collections.emptyList();

		List<T> expressions = new LinkedList<T>();

		for (Tree node : nodes)
			if (node.isNode())
				expressions.add(loadFrom(node));

		return expressions;
	}
}
