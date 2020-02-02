/**
 * @id cbl/directives/use-of-replace
 * @name Avoid using the REPLACE statement
 * @description The REPLACE statement changes source code before it gets compiled,
 *              making that code harder to understand and maintain.
 * @kind problem
 * @problem.severity recommendation
 * @precision high
 * @tags maintainability
 */

import cobol

from Replace r
select r, "Use of the REPLACE statement is discouraged."