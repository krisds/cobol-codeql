/**
 * @id cbl/close-missing-status-condition-handlers
 * @name READ statement ignoring file status
 * @description READ statements should handle file status conditions.
 * @kind problem
 * @problem.severity warning
 * @tags correctness
 */

import cobol

from Read r, FileControlEntry e
where r.getLocation().getFile() = e.getLocation().getFile()
  and e = r.getFile().getTarget()
  and not(exists(e.getFileStatus()))
  and not(exists(r.getAtEnd()))
  and not(exists(r.getInvalidKey()))
select r, "This READ statement should handle possible file status conditions itself (via AT END or INVALID KEY), as no status field was set up for $@.", e, e.getName()
