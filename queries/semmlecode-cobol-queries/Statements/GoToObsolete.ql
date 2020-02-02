/**
 * @id cbl/goto-without-target
 * @name Use of obsolete form of GO TO statement
 * @description The form of the 'GO TO' statement without any explicit targets requires 'ALTER'
 *              and therefore results in complex and unmaintainable code.
 * @kind problem
 * @problem.severity recommendation
 * @precision high
 * @tags maintainability
 */

import cobol

from GoTo s
where (not exists(s.getTargetsList()))
   or (s.getTargetsSize() = 0)
select s, "This form of the GO TO statement (without explicit targets) is obsolete and its use is strongly discouraged."