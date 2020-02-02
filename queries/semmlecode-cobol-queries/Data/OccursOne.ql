/**
 * @id cbl/occurs-1
 * @name Single element array
 * @description The use of an array is not required if the array will only ever have a single element.
 * @kind problem
 * @problem.severity warning
 * @precision medium
 * @tags maintainability
 */

import cobol

from OccursClause o
where o.getMinimum() = 1
  and (exists(o.getMaximum()) implies (o.getMaximum() = 1))
select o, "No need to use an array when it has only a single element."
