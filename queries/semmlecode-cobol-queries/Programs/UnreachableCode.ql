/**
 * @id cbl/unreachable-code
 * @name Unreachable code
 * @description Unreachable code is often indicative of missing program logic or latent bugs and should be avoided.
 * @kind problem
 * @problem.severity warning
 * @precision high
 * @tags maintainability
 *       useless-code
 */

import cobol

class ProcedureOrStmt extends AstNode
{
  ProcedureOrStmt() {
    this instanceof Procedure or
    this instanceof Stmt
  }

  string getMessage() {
    (this instanceof Stmt and result = "This statement") or
    result = this.(Procedure).toString()
  }
}

predicate reachableFromProcedureDivision(AstNode n) {
  n instanceof ProcedureDivision or
  exists(AstNode m |
    reachableFromProcedureDivision(m) |
    m.getASuccessor() = n)
}

predicate paragraphOnlyContainsExit(Paragraph p) {
  p.getSentencesSize() = 1
  and p.getFirstSentence().getStatementsSize() = 1
  and p.getFirstSentence().getFirstStatement() instanceof Exit
}

predicate notFromCopybook(AstNode n) {
  exists(CompilationGroup text |
    text.getLocation().getFile() = n.getLocation().getFile())
}

from ProcedureOrStmt unreachable
where
  notFromCopybook(unreachable)
  and not reachableFromProcedureDivision(unreachable)
  and (unreachable instanceof Stmt implies
         reachableFromProcedureDivision(unreachable.(Stmt).getEnclosingParagraph()))
  and not paragraphOnlyContainsExit((Paragraph)unreachable)
select unreachable, unreachable.getMessage() + " is not reachable."
