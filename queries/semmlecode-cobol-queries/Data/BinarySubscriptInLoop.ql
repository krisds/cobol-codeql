/**
 * @id cbl/binary-subscript-in-loop
 * @name Binary subscript in loop
 * @description Using a binary subscript inside a loop may lead to bad performance.
 * @kind problem
 * @problem.severity warning
 * @precision high
 * @tags maintainability
 *       efficiency
 */

import cobol

from PerformInline p, RelativeSubscript s, BinaryItem e
where s.hasAncestor(p)
  and s.getReference().getTarget() = e
select s, "This subscript operation in a loop uses a $@, which is approximately 40% slower than an index declared using an INDEXED BY phrase ", e, "binary"
