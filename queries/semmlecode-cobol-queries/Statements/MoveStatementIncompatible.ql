/**
 * @id cbl/incompatible-data-in-move
 * @name MOVE statement incompatible arguments
 * @description The sender and a receiver of a 'MOVE' statement should have compatible data categories.
 * @kind problem
 * @problem.severity error
 * @precision high
 * @tags correctness
 */

import cobol

from Move m, DataReference s, DataReference r
where s = m.getInitialOperand().(QualifiedDataNameWithSubscripts).getReference()
  and r = m.getAReceivingOperand().(QualifiedDataNameWithSubscripts).getReference()
  and s.getTarget().getPicture().getCategory().matches("alphanumeric%")
  and r.getTarget().getPicture().getCategory().matches("numeric%")
select m, "Move of $@ (references $@) to $@ (references $@), may cause abend at runtime.", s, s.getName(), s.getTarget(), "alphanumeric data item", r, r.getName(), r.getTarget(), "numeric data item"
