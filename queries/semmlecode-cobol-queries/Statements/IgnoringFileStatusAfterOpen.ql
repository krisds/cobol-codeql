/**
 * @id cbl/file-status-ignored-after-open
 * @name OPEN statement ignoring file status
 * @description OPEN statements that do not handle file status conditions may trigger unexpected behavior.
 * @kind problem
 * @problem.severity warning
 * @tags correctness
 */

import cobol

from Open open, DataReference fileStatus
where fileStatus = open.getAFileControlEntry().getFileStatus()
  and not testsFileStatusRightAfter(open, fileStatus)
select open, "File status $@ should be tested immediately after this OPEN statement.", fileStatus, fileStatus.getName()
