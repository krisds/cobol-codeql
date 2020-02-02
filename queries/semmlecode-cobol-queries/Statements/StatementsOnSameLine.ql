/**
 * @id cbl/statements-on-same-line
 * @name Statements on same line
 * @description Placing statements on separate lines improves readability.
 * @kind problem
 * @problem.severity recommendation
 * @precision high
 * @tags readability
 */

import cobol

from Stmt first, Stmt second, StmtList l, Location firstLocation, Location secondLocation
where l.getNextItem(first) = second
  and firstLocation  = first.getLocation()
  and secondLocation = second.getLocation()
  and firstLocation.getFile()        = secondLocation.getFile()
  and firstLocation.getStartLine()   = secondLocation.getStartLine()
  and firstLocation.getStartColumn() < secondLocation.getStartColumn()
  and (not first.isCompilerGenerated() and not second.isCompilerGenerated())
select second, "This statement follows another on the same line. Place it on a separate line to improve readability."
