/**
 * @id cbl/statement-in-copybook
 * @name Code in copybook
 * @description Sharing code via copybooks can lead to inconsistent systems.
 * @kind problem
 * @problem.severity warning
 * @precision high
 * @tags maintainability
 *       reusability
 */

import cobol

from Copybook copy
where exists ( Stmt s | s.getParent*() = copy )
select copy, "Sharing code via copybooks is strongly discouraged."
