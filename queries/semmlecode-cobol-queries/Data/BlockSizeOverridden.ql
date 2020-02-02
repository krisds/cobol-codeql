/**
 * @id cbl/hardcoded-block-contains
 * @name Hardcoded block size
 * @description Manual specifications of block size may not be optimal.
 *              It is better to leave this up to the operating system.
 * @kind problem
 * @problem.severity warning
 * @precision high
 * @tags maintainability
 */

import cobol

from BlockContainsClause b
where b.getMinimumSize() > 0
select b, "Manual specification of block size is strongly discouraged."
