/**
 * @id cbl/missing-when-other-in-evaluate
 * @name Use of EVALUATE without WHEN OTHER
 * @description An 'EVALUATE' statement without a 'WHEN OTHER' branch can result in silent
 *              incorrect behavior if the domain(s) of the selection object(s) change(s).
 * @kind problem
 * @problem.severity recommendation
 * @precision high
 * @tags maintainability
 */

import cobol

from Evaluate e
where not exists(WhenOtherBranch b | b = e.getABranch())
select e, "An EVALUATE statement should use a WHEN OTHER branch."