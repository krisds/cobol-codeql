/**
 * @id cbl/mixed-datatypes-in-computation
 * @name Mixing datatypes in computations
 * @description Mixing datatypes imposes additional overhead on computations.
 * @kind problem
 * @problem.severity warning
 * @precision high
 * @tags maintainability
 *       efficiency
 */

import cobol

from ComputationalStmt computation, int usageCount, 
     QualifiedDataNameWithSubscripts first, string firstUsage, 
     QualifiedDataNameWithSubscripts different, string differentUsage
where
  // Count all different usages, and continue if there is more than one.
  usageCount = count( string usage | usage = usage(computation.getAnOperand()) | usage )
  and usageCount > 1

  // Grab the first operand, and its usage.
  and first = firstOperand(computation)
  and firstUsage = usage(first)
  
  // Find a later operand, of a different usage than the first.
  and different = computation.getAnOperand()
  and different != first
  // and before(first, different)  -- This is implied by different != first.
  and differentUsage = usage(different)
  and firstUsage != differentUsage
  
  // But make sure there is no earlier operand than that one which also has a 
  // different usage than the first.
  and not exists ( QualifiedDataNameWithSubscripts earlierDifferent, string earlierDifferentUsage |
    earlierDifferent = computation.getAnOperand()
    and earlierDifferent != first
    and earlierDifferent != different
    // and before(first, earlierDifferent)  -- This is implied by earlierDifferent != first.
    and before(earlierDifferent, different)
    and earlierDifferentUsage = usage(earlierDifferent)
    and firstUsage != earlierDifferentUsage
  )
select computation, "This computation uses " + usageCount + " different types of operands. "
                  + "E.g. $@ (" + firstUsage + ") and $@ (" + differentUsage + ").",
       first, first.getReference().getName(),
       different, different.getReference().getName()


QualifiedDataNameWithSubscripts firstOperand(ComputationalStmt computation) {
  result = computation.getAnOperand()
  and not exists (QualifiedDataNameWithSubscripts op |
    op = computation.getAnOperand() and
    before(op, result)
  )
}

string usage(QualifiedDataNameWithSubscripts a) {
  result = a.getReference().getTarget().getNormalizedUsage()
}


private predicate before(QualifiedDataNameWithSubscripts a, QualifiedDataNameWithSubscripts b) {
    exists (Location al, Location bl |
        al = a.getLocation() and
        bl = b.getLocation() and
        al.getStartLine() * 1000 + al.getStartColumn() < bl.getStartLine() * 1000 + bl.getStartColumn() 
    )
}
