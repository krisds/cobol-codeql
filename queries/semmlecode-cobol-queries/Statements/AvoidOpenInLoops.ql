/**
 * @id cbl/file-opened-in-loop
 * @name Avoid opening files inside a loop
 * @description Opening files can be expensive and is therefore best avoided in
 *              loops.
 * @kind problem
 * @problem.severity warning
 * @precision high
 * @tags maintainability
 *       efficiency
 */

import cobol

from Perform p, Open o
where
  exists(p.getLoopForm()) and
  o = p.getAStmtInScope()
select o, "Opening files in a $@ is discouraged.", p, "loop"
