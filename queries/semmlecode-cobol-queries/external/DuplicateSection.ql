/**
 * @id cbl/duplicate-section
 * @name Duplicate section
 * @description Duplicated sections make code more difficult to understand
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
where duplicateSections(p, q, percent)
select p, percent.floor() + "% of statements in this section are duplicated in $@.", q, q.getName()
