/**
 * @id cbl/file-status-ignored-after-read
 * @name READ statement ignoring file status
 * @description READ statements that do not handle file status conditions may trigger unexpected behavior.
 * @kind problem
 * @problem.severity warning
 * @tags correctness
 */

import cobol

from Read read, DataReference fileStatus
where fileStatus = read.getAFileControlEntry().getFileStatus()
  and not testsFileStatusRightAfter(read, fileStatus)
select read, "File status $@ should be tested immediately after this READ statement.", fileStatus, fileStatus.getName()
