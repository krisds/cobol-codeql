/**
 * @id cbl/trunacted-return-value-in-call
 * @name CALL returns truncated value
 * @description A CALL statement where the return value being received is larger than the parameter which accepts it will cause truncation and possible data loss.
 * @kind problem
 * @problem.severity error
 * @precision high
 * @tags correctness
 */

import cobol

from Call call, QualifiedDataNameWithSubscripts r, int rSize,
     ProgramDefinition prog, QualifiedDataNameWithSubscripts s, int sSize
where prog = call.getTarget()
  and exists ( ProcedureDivisionHeader header |
    header = prog.getProcedureDivision().getHeader() and
    s = header.getReturning() and
    r = call.getGiving().(QualifiedDataNameWithSubscripts) and
    sSize = s.sizeFromRef() and
    rSize = r.sizeFromRef() and
    sSize > rSize
  )
select call, "Call receiving value $@ (size $@) from $@ (size $@) will cause truncation.",
       r, r.getReference().getName(),
       r.getReference().getTarget(), rSize.toString(),
       s, s.getReference().getName(),
       s.getReference().getTarget(), sSize.toString()
