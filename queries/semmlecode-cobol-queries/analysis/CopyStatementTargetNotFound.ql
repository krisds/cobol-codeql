/**
 * @id cbl/missing-library-text
 * @name Copy statement target not found
 * @description The target of this Copy statement wasn't found.
 * @kind problem
 * @problem.severity info
 */

import cobol

from Copy c, string libraryName
where not exists (Text text | c = text.getAPreprocessingDirective()) and
      if exists (c.getLibraryName()) then
        libraryName = c.getLibraryName()
      else
        libraryName = "<no library>"
select c, "This copy statement's target wasn't found: '" + c.getTextName() + " of " + libraryName + "'"