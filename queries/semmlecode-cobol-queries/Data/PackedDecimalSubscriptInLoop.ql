/**
 * @id cbl/packed-decimal-subscript-in-loop
 * @name Packed decimal subscript in loop
 * @description Using a packed decimal subscript inside a loop may lead to bad performance.
 * @kind problem
 * @problem.severity warning
 * @precision high
 * @tags maintainability
 *       efficiency
 */

import cobol

from PerformInline p, RelativeSubscript s, PackedDecimalItem e
where s.hasAncestor(p)
  and s.getReference().getTarget() = e
select s, "This subscript operation in a loop uses a $@, which is approximately 220% slower than an index declared using an INDEXED BY phrase ", e, "packed decimal"
