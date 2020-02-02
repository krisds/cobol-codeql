package com.semmle.cobol.mapping.values;

import java.io.File;

import com.semmle.cobol.generator.tuples.Value;
import com.semmle.cobol.mapping.runtime.TrapFile;

/**
 * This is a {@link Value} for a {@link File} reference.
 */
public class TrapFileValue extends Value {

	/**
	 * The {@link File} being referenced.
	 */
	private File path;

	private final TrapFile trapFile;

	public TrapFileValue(String name, TrapFile trapFile, String path) {
		super(name);
		this.trapFile = trapFile;
		this.path = new File(path);
	}

	@Override
	public Object getValue() {
		return trapFile.populateFile(path);
	}
}