/**
 * @id cbl/file-status-ignored-after-merge
 * @name MERGE statement ignoring file status
 * @description MERGE statements that do not handle file status conditions may trigger unexpected behavior.
 * @kind problem
 * @problem.severity warning
 * @tags correctness
 */

import cobol

from Merge merge, DataReference fileStatus
where fileStatus = merge.getAFileControlEntry().getFileStatus()
  and not testsFileStatusRightAfter(merge, fileStatus)
select merge, "File status $@ should be tested immediately after this MERGE statement.", fileStatus, fileStatus.getName()
