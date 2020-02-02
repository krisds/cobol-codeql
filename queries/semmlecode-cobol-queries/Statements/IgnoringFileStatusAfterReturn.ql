/**
 * @id cbl/file-status-ignored-after-return
 * @name RETURN statement ignoring file status
 * @description RETURN statements that do not handle file status conditions may trigger unexpected behavior.
 * @kind problem
 * @problem.severity warning
 * @tags correctness
 */

import cobol

from ReturnStmt return, DataReference fileStatus
where fileStatus = return.getAFileControlEntry().getFileStatus()
  and not testsFileStatusRightAfter(return, fileStatus)
select return, "File status $@ should be tested immediately after this RETURN statement.", fileStatus, fileStatus.getName()
