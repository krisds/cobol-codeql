/**
 * @id cbl/truncated-call-argument
 * @name CALL argument truncated
 * @description A CALL statement where the argument being passed is larger than the parameter which accepts it will cause truncation and possible data loss.
 * @kind problem
 * @problem.severity error
 * @precision high
 * @tags correctness
 */

import cobol

from Call call, ProgramDefinition prog, int position, 
     CallArgWithValue arg, QualifiedDataNameWithSubscripts s, int argSize,
     ProcedureDivisionParameter param, QualifiedDataNameWithSubscripts r, int paramSize
where prog = call.getTarget()
  and exists ( ProcedureDivisionHeader header |
    header = prog.getProcedureDivision().getHeader() and
    position >= 0 and
    position < call.getUsingSize() and
    position < header.getUsingSize() and
    arg = call.getUsing(position) and
    param = header.getUsing(position) and
    s = arg.getValue() and
    r = param.getValue() and
    argSize = s.sizeFromRef() and
    paramSize = r.sizeFromRef() and
    argSize > paramSize
  )
select call, "Call passing argument $@ (size $@) to $@ (size $@) will cause truncation.",
       arg, s.getReference().getName(),
       s.getReference().getTarget(), argSize.toString(),
       param, r.getReference().getName(),
       r.getReference().getTarget(), paramSize.toString()
