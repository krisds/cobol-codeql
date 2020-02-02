package com.semmle.cobol.generator.functions;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A unary function which maps its input to an output.
 */
public class SwitchFn1<P, R> implements Fn1<P, R> {

	private Map<P, R> choices = new LinkedHashMap<>();

	public SwitchFn1<P, R> put(P p, R r) {
		choices.put(p, r);
		return this;
	}

	@Override
	public R eval(P p) {
		return choices.get(p);
	}
}
