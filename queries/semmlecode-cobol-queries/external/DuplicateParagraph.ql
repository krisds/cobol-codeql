/**
 * @id cbl/duplicate-paragraph
 * @name Duplicate paragraph
 * @description There is another paragraph that shares a lot of code with this paragraph.
 *              Extract the common parts to a shared utility paragraph to improve maintainability.
 * @kind problem
 * @problem.severity recommendation
 * @precision high
 * @tags maintainability
 *       useless-code
 */
import cobol
import CodeDuplication

from Paragraph p, Paragraph q, float percent
where duplicateParagraphs(p, q, percent)
select p, percent.floor() + "% of statements in this paragraph are duplicated in $@.", q, q.getName()
