/**
 * @id cbl/use-of-alter
 * @name Use of obsolete ALTER statement
 * @description The 'ALTER' statement is obsolete and its use results in unmaintainable code
 *              due to the highly complex control flow.
 * @kind problem
 * @problem.severity recommendation
 * @precision high
 * @tags maintainability
 */

import cobol

from Alter x
select x, "The ALTER statement is obsolete and its use is strongly discouraged."