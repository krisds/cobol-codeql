package com.semmle.cobol.cflow;

import java.util.List;

import com.semmle.cobol.generator.effects.CFlowNode;

public interface Logic {
	List<CFlowNode> getSuccessors(CFlowNode n);

	List<CFlowNode> getContinuations(CFlowNode n);
}