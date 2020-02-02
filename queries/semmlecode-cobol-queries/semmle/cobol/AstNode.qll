import AST
import Location

/** A node in the abstract syntax tree, any program element. */
class AstNode extends AstNode_, Locatable {
  /** Does this AstNode have 'other' as a descendant? */
  predicate hasDescendant(AstNode other) {
    other.hasAncestor(this)
  }

  /** Does this AstNode have 'other' as a ancestor? */
  pragma [nomagic]
  predicate hasAncestor(AstNode other) {
    this.getParent+() = other
  }

  /** Get any possible successor in the progam's control flow. */
  AstNode getASuccessor() {
    successors(this, result)
  }

  private AstNode getNonUnitParent() {
    result = getParent() and not result instanceof Unit
  }

  /** Get the unit containing this element. */
  Unit getEnclosingUnit() {
    result = this.getNonUnitParent*().getParent()
  }
  
  private AstNode getNonTextParent() {
    result = getParent() and not result instanceof Text
  }

  /** Get the unit containing this element. */
  Text getEnclosingText() {
    result = this or
    result = this.getNonTextParent*().getParent()
  }
  
  override string toString() {
    result = "AstNode"
  }
}

/**
 * A paragraph or section exit node. These
 * are inserted by the extractor to provide
 * a common end point for the control flow
 * leaving a procedure.
 */
class ExitNode extends ExitNode_ {
  /** Get any possible successor of this exit node in the program's control flow. */
  override AstNode getASuccessor() {
    result = super.getASuccessor() or
    exists(PerformOutofline s |
      this = s.getEndTarget().getExitNode() and
      if (exists (s.getLoopForm())) then result = s
      else result = s.getAContinuation()
    )
  }
}