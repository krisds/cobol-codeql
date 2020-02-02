/**
 * @id cbl/similar-file
 * @name Mostly similar module
 * @description Modules in which most of the lines are similar to those in another module make code more
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
where similarTexts(p, q, percent)
  and f = p.getLocation().getFile()
  and g = q.getLocation().getFile()
select f, percent.floor() + "% of statements in " + f.getName() + " are similar to those in $@.", g, g.getName()
