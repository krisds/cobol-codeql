/**
 * @id cbl/file-status-ignored-after-delete
 * @name DELETE statement ignoring file status
 * @description DELETE statements that do not handle file status conditions may trigger unexpected behavior.
 * @kind problem
 * @problem.severity warning
 * @tags correctness
 */

import cobol

from Delete delete, DataReference fileStatus
where fileStatus = delete.getAFileControlEntry().getFileStatus()
  and not testsFileStatusRightAfter(delete, fileStatus)
select delete, "File status $@ should be tested immediately after this DELETE statement.", fileStatus, fileStatus.getName()
