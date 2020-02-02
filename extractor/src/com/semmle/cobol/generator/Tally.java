package com.semmle.cobol.generator;

public final class Tally {
	public int lineNumber = 0;
	public boolean sawCode = false;
	public boolean sawComment = false;
	public boolean sawWater = false;

	public int lines = 0;
	public int code = 0;
	public int comments = 0;
	public int water = 0;

	@Override
	public String toString() {
		return lines + " lines, " + code + " code, " + comments + " comments, "
				+ water + " water";
	}
}