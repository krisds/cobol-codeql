/**
 * @id cbl/truncated-data-in-move
 * @name MOVE statement truncates
 * @description A MOVE statement where the sender is larger than the receiver will result in truncation and possible data loss.
 * @kind problem
 * @problem.severity error
 * @precision high
 * @tags correctness
 */

import cobol


from Move m, QualifiedDataNameWithSubscripts s, QualifiedDataNameWithSubscripts r, int ssize, int rsize
where s = m.getInitialOperand()
  and r = m.getAReceivingOperand()
  and ssize = s.sizeFromRef()
  and rsize = r.sizeFromRef()
  and ssize > rsize
  
  // This helps guard against incomplete traps, or missing info.
  and rsize > 0
select m, "Move of $@ (size $@) to $@ (size $@) will cause truncation.",
       s, s.getReference().getName(), s.getReference().getTarget(), ssize.toString(), r, r.getReference().getName(), r.getReference().getTarget(), rsize.toString()
