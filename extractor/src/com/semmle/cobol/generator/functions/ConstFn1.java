package com.semmle.cobol.generator.functions;

/**
 * A constant unary function, returning a given value.
 */
public class ConstFn1<P, R> implements Fn1<P, R> {

	private final R r;

	public ConstFn1(R r) {
		this.r = r;
	}

	@Override
	public R eval(P p) {
		return r;
	}
}
