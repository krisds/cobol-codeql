/**
 * @id cbl/file-status-ignored-after-rewrite
 * @name REWRITE statement ignoring file status
 * @description REWRITE statements that do not handle file status conditions may trigger unexpected behavior.
 * @kind problem
 * @problem.severity warning
 * @tags correctness
 */

import cobol

from Rewrite rewrite, DataReference fileStatus
where fileStatus = rewrite.getAFileControlEntry().getFileStatus()
  and not testsFileStatusRightAfter(rewrite, fileStatus)
select rewrite, "File status $@ should be tested immediately after this REWRITE statement.", fileStatus, fileStatus.getName()
