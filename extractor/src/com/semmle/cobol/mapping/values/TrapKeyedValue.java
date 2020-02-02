package com.semmle.cobol.mapping.values;

import com.semmle.cobol.generator.tuples.Value;
import com.semmle.cobol.mapping.runtime.TrapFile;

/**
 * This is a {@link Value} for a value set to a key in a {@link TrapFile}.
 */
public class TrapKeyedValue extends Value {

	/**
	 * The key to the value to look up in the {@link TrapFile}.
	 */
	private final String key;

	private final TrapFile trapFile;

	public TrapKeyedValue(String name, TrapFile trapFile, String key) {
		super(name);
		this.trapFile = trapFile;
		this.key = key;
	}

	@Override
	public Object getValue() {
		return trapFile.getValue(key);
	}
}