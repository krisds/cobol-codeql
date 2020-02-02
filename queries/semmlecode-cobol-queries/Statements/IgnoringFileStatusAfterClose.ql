/**
 * @id cbl/file-status-ignored-after-close
 * @name CLOSE statement ignoring file status
 * @description CLOSE statements that do not handle file status conditions may trigger unexpected behavior.
 * @kind problem
 * @problem.severity warning
 * @tags correctness
 */

import cobol

from Close close, DataReference fileStatus
where fileStatus = close.getAFileControlEntry().getFileStatus()
  and not testsFileStatusRightAfter(close, fileStatus)
select close, "File status $@ should be tested immediately after this CLOSE statement.", fileStatus, fileStatus.getName()
