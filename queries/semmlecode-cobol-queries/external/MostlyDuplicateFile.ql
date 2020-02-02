/**
 * @id cbl/duplicate-file
 * @name Mostly duplicate module
 * @description Modules in which most of the lines are duplicated in another module make code more
 *              difficult to understand and introduce a risk of changes being made to only one copy.
 * @kind problem
 * @problem.severity recommendation
 * @precision high
 * @tags testability
 *       maintainability
 *       useless-code
 */
import cobol
import CodeDuplication

from Text p, Text q, float percent, File f, File g
where duplicateTexts(p, q, percent)
  and f = p.getLocation().getFile()
  and g = q.getLocation().getFile()
select f, percent.floor() + "% of statements in " + f.getName() + " are duplicates of statements in $@.", g, g.getName()
