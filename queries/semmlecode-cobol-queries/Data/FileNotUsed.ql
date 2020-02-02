/**
 * @id cbl/unused-file-control-entry
 * @name File not used
 * @description File declarations which go unused hint at unmaintained code.
 * @kind problem
 * @problem.severity warning
 * @precision high
 * @tags maintainability
 */

import cobol

from FileControlEntry file, string msg
where if not exists ( FileIOStmt stmt | stmt.getAFileControlEntry() = file ) then
        msg = file.getName() + " is declared but never used."
      else if not exists ( FileDescription descr | descr.getAFileControlEntry() = file ) then
        msg = file.getName() + " is declared but never described."
      else
        none()
select file, msg

