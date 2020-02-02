/**
 * @id cbl/file-status-ignored-after-write
 * @name WRITE statement ignoring file status
 * @description WRITE statements that do not handle file status conditions may trigger unexpected behavior.
 * @kind problem
 * @problem.severity warning
 * @tags correctness
 */

import cobol

from Write write, DataReference fileStatus
where fileStatus = write.getAFileControlEntry().getFileStatus()
  and not testsFileStatusRightAfter(write, fileStatus)
select write, "File status $@ should be tested immediately after this WRITE statement.", fileStatus, fileStatus.getName()
