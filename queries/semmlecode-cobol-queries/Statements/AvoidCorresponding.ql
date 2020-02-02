/**
 * @id cbl/use-of-corresponding
 * @name Avoid using the CORRESPONDING phrase
 * @description The CORRESPONDING phrase in MOVE, ADD and SUBTRACT statements
 *              can lead to unexpected behavior if the fields differ in name or data type.
 * @kind problem
 * @problem.severity warning
 * @precision medium
 * @tags correctness
 */

import cobol

from CorrespondingClause c
select c, "Use of the CORRESPONDING clause is discouraged."
