/**
 * @id cbl/write-then-read-in-move
 * @name MOVE may not assign values as expected
 * @description Moving a value to a data item does not make that value available in further uses of that data item in the same MOVE statement.
 * @kind problem
 * @problem.severity warning
 * @precision high
 * @tags correctness
 */

import cobol

from Move m, int i, int j, QualifiedDataNameWithSubscripts a, QualifiedDataNameWithSubscripts b
where // We have two different operands, a and b:
      0 <= i and i < j and j < m.getToOperandsSize()
  and a = m.getToOperand(i)
  and b = m.getToOperand(j)
  and exists (DataDescriptionEntry e, DataReference r |
    // Operand a writes to data entry e:
    e = a.getReference().getTarget() and
    // Operand b uses e somewhere in its reference, but not as the actual reference: 
    e = r.getTarget() and
    not (r = b.getReference()) and
    r.hasAncestor(b)
  )
select m, "$@ gets set first, then referenced in $@; that reference will still use the old value.",
       a, display(a),
       b, display(b)


string display(QualifiedDataNameWithSubscripts d) {
  if (exists(d.getASubscript())) then
    result = d.getReference().getName() + "(...)"
  else
    result = d.getReference().getName()
}
