/**
 * @id cbl/file-status-ignored-after-start
 * @name START statement ignoring file status
 * @description START statements that do not handle file status conditions may trigger unexpected behavior.
 * @kind problem
 * @problem.severity warning
 * @tags correctness
 */

import cobol

from Start start, DataReference fileStatus
where fileStatus = start.getAFileControlEntry().getFileStatus()
  and not testsFileStatusRightAfter(start, fileStatus)
select start, "File status $@ should be tested immediately after this START statement.", fileStatus, fileStatus.getName()
