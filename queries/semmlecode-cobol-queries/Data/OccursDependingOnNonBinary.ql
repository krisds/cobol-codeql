/**
 * @id cbl/non-binary-occurs-clause
 * @name OCCURS DEPENDING ON uses non-binary data item
 * @description Using a non-binary data item to define the size of a variable-length table is inefficient.
 * @kind problem
 * @problem.severity warning
 * @precision high
 * @tags maintainability
 *       efficiency
 */

import cobol

from OccursClause o, DataDescriptionEntry e
where o.getObject().getTarget() = e
     and (e instanceof DisplayItem or
          e instanceof PackedDecimalItem)
select o, "Using a $@ to define the size of a variable-length table is inefficient.", e, "non-binary data item"