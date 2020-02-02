/**
 * @id cbl/incorrectly-indented-conditional
 * @name Conditional indentation
 * @description Conditional branches should be indented relative to their enclosing statement.
 * @kind problem
 * @problem.severity warning
 * @precision high
 * @tags readability
 */

import cobol

ElseBranch precedingElseBranch(IfThenElse s) {
  result.getFirstStatement() = s and 
  result.getLocation().getStartLine() = s.getLocation().getStartLine()
}

int minStartCol(IfThenElse s) {
  if exists(precedingElseBranch(s)) then
    result = precedingElseBranch(s).getLocation().getStartColumn()
  else
    result = s.getLocation().getStartColumn()
}

from Decision d, Stmt nested, int nestedStartColumn
where
   nestedStartColumn = nested.getLocation().getStartColumn() and
   (nested = d.getABranch().getAStatement()) and
   (minStartCol(d) >= nestedStartColumn or
    (d instanceof Evaluate and
      exists(WhenBranch b | d.getABranch() = b and b.getLocation().getStartColumn() >= nestedStartColumn)))
select d, "This conditional statement has a child $@ with misleading indentation.", nested, nested.toString()