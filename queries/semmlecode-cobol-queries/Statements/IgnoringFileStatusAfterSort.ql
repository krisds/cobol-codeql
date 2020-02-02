/**
 * @id cbl/file-status-ignored-after-sort
 * @name SORT statement ignoring file status
 * @description SORT statements that do not handle file status conditions may trigger unexpected behavior.
 * @kind problem
 * @problem.severity warning
 * @tags correctness
 */

import cobol

from Sort sort, DataReference fileStatus
where fileStatus = sort.getAFileControlEntry().getFileStatus()
  and not testsFileStatusRightAfter(sort, fileStatus)
select sort, "File status $@ should be tested immediately after this SORT statement.", fileStatus, fileStatus.getName()
