/**
 * @id cbl/use-of-sort-and-merge
 * @name Avoid using the SORT and MERGE statements
 * @description The SORT and MERGE statements are inefficient.
 * @kind problem
 * @problem.severity recommendation
 * @precision medium
 * @tags maintainability
 *       efficiency
 */

import cobol

from Stmt c
where c instanceof Sort or
      c instanceof Merge
select c, "Use of the " + c.toString() + " statement is discouraged."
