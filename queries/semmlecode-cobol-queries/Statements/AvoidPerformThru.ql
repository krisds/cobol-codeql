/**
 * @id cbl/use-of-perform-through
 * @name Avoid PERFORM THRU
 * @description Performing a range of procedures should be avoided as it runs against the principles of structured programming.
 * @kind problem
 * @problem.severity warning
 * @precision high
 * @tags maintainability
 */

import cobol

from PerformOutofline p
where exists (p.getProcedureName2())
select p, "Performing a range of procedures ($@ THRU $@) is discouraged.",
          p.getStartTarget(), p.getProcedureName1().getName(),
          p.getEndTarget(), p.getProcedureName2().getName()
          