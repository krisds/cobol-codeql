/**
 * @id cbl/dynamic-call
 * @name CALL target modified at runtime
 * @description Dynamically changing the target of a CALL statement makes your code harder to understand.
 * @kind problem
 * @problem.severity warning
 * @precision high
 * @tags maintainability
 */

import cobol

from DataDescriptionEntry entry, Stmt s
where exists ( Call call |
    entry = call.getProgramName().(QualifiedDataNameWithSubscripts).getReference().getTarget()
    and entry = s.getAReceivingDataEntry())
select s, "$@ identifies a CALL target, and should not be modified at runtime.",
       entry, entry.getName()
