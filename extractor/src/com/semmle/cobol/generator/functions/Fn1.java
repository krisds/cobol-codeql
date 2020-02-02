package com.semmle.cobol.generator.functions;

/**
 * A unary function.
 */
public interface Fn1<P, R> {
	public R eval(P p);
}