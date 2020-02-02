/**
 * @id cbl/directives/change-of-format-in-copybook
 * @name Changing source format in copybook
 * @description Changing the format of copybooks may break the containing source code.
 * @kind problem
 * @problem.severity warning
 * @precision high
 * @tags maintainability
 */

import cobol

from Copybook copy, Directive dir, string format
where dir.getParent*() = copy
  and setsSourceFormat(dir, format)
select dir, "This $@ changes the source format to '" + format + "'.",
       copy, "copybook"

predicate setsSourceFormat(Directive dir, string format) {
  format = dir.(SourceFormatDirective).getFormat()
  or exists (string f |
    f = dir.(MFSetStatement).getFormat() and
    format = f.substring(1, f.length()-1)
  )
}
