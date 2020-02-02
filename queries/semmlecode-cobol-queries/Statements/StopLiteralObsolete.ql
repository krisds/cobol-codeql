/**
 * @id cbl/literal-in-stop
 * @name Obsolete form of STOP statement
 * @description The form of the STOP statement with a literal argument is obsolete.
 * @kind problem
 * @problem.severity recommendation
 * @precision high
 * @tags maintainability
 */

import cobol

from Stop s
where exists(s.getLiteral())
select s, "This form of the STOP statement (with a literal argument) is obsolete."