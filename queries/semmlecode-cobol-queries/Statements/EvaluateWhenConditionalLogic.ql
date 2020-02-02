/**
 * @id cbl/conditional-when-in-evaluate
 * @name EVALUATE WHEN conditional logic
 * @description EVALUATE WHEN branches should not contain conditional logic.
 * @kind problem
 * @problem.severity recommendation
 * @precision high
 * @tags readability
 */

import cobol

from WhenBranch s, Decision d
where s.getStatementsSize() = 1
  and d = s.getFirstStatement()
select s, "EVALUATE WHEN branches should not contain $@.", d, "conditional logic"