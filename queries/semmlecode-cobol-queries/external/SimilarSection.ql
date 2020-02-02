/**
 * @id cbl/similar-section
 * @name Similar section
 * @description Very similar sections make code more difficult to understand
 *              and introduce a risk of changes being made to only one copy.
 * @kind problem
 * @problem.severity recommendation
 * @precision high
 * @tags maintainability
 *       useless-code
 */
import cobol
import CodeDuplication

from Section p, Section q, float percent
where similarSections(p, q, percent)
select p, percent.floor() + "% of statements in this section are similar to statements in $@.", q, q.getName()
