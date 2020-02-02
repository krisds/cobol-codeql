package com.semmle.cobol.mapping.tags;

public enum MappingTokenTag {
	// Markers for nesting:
	BEGIN, END,

	// Things which are not program text:
	COMMENT, WHITESPACE,

	// Basic elements:
	NODE_NAME, QL_TYPE, ATTRIBUTE_NAME, YPATH, STATE_NAME, STRING,

	// Quick XPaths:
	CURRENT_NODE, ALL_CHILD_NODES, NTH_CHILD_NODE,

	// Operators:
	TRAP, ASSIGN, CALL_BUILTIN, RETURN,
}
