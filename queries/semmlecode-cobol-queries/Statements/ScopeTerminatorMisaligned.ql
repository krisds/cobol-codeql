/**
 * @id cbl/incorrectly-indented-terminator
 * @name Scope terminator misaligned
 * @description Misaligning scope terminators in relation to the start of the statement they belong to hampers readability.
 * @kind problem
 * @problem.severity recommendation
 * @precision high
 * @tags readability
 */

import cobol

from ScopeTerminator terminator, Stmt s
where s = terminator.getStmt()
  and not exists (|
    // It's a one-liner...
    onSameLine(terminator, s)
    // Or it starts (almost) in the same column.
    or (terminator.getLocation().getStartColumn() - startColumn(s)).abs() <= 1
  )
  select terminator, "Scope terminator found in column " + terminator.getLocation().getStartColumn()
            + ", but $@ starts in column " + s.getLocation().getStartColumn()
            + ".",
         s, "statement"

predicate onSameLine(AstNode a, AstNode b) {
  a.getLocation().getStartLine() = b.getLocation().getStartLine()
}

int startColumn(Stmt s) {
  // Two options for the starting column:
  
  // 1. The actual start of the statement.
  result = s.getLocation().getStartColumn()
  
  // 2. If the statement is the only statement in a branch,
  //    and it starts on the same line as the branch,
  //    then the start of the branch is also a valid column.
  or exists ( Branch b |
    b.getStatementsSize() = 1 and
    s = b.getStatement(0) and
    onSameLine(s, b) and
    result = b.getLocation().getStartColumn()
  )
}
