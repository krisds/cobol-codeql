/**
 * @id cbl/unclosed-file
 * @name File opened but never closed
 * @description Failing to close files correctly may lead to resource leaks and corrupt data.
 * @kind problem
 * @problem.severity warning
 * @precision high
 * @tags correctness
 */

import cobol

from Open open, FileControlEntry entry
where entry = open.getAFileControlEntry()
  and not exists ( Close close | close.getAFileControlEntry() = entry )
select open, "$@ is never closed.",
       entry, entry.getName()
