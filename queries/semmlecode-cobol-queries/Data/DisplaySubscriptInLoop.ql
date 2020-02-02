/**
 * @id cbl/display-subscript-in-loop
 * @name Display subscript in loop
 * @description Using a display subscript inside a loop may lead to bad performance.
 * @kind problem
 * @problem.severity warning
 * @precision high
 * @tags maintainability
 *       efficiency
 */

import cobol

from PerformInline p, RelativeSubscript s, DisplayItem e
where s.hasAncestor(p)
  and s.getReference().getTarget() = e
select s, "This subscript operation in a loop uses a $@, which is approximately 300% slower than an index declared using an INDEXED BY phrase ", e, "display"
