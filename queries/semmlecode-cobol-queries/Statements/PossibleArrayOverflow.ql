/**
 * @id cbl/possible-array-overflow-in-loop
 * @name Possible array overflow
 * @description Looping without a constraint on an index may cause an array overflow.
 * @kind problem
 * @problem.severity warning
 * @precision medium
 * @tags correctness
 */

import cobol

from PerformInline loop,
     QualifiedDataNameWithSubscripts use,
     DataDescriptionEntry index
where 
  // We're looking for loops which increment an array index they use.
  suspectLoop(loop, use, index)
  // But only those which align most closely to the use and increment.
  // (I.e. there is no suspect contained within the suspecious loop.)
  and not exists (PerformInline smallerSuspect |
    smallerSuspect.hasAncestor(loop) and
    suspectLoop(smallerSuspect, use, index)
  )
  // A suspect gets flagged when there is no guard on the use of the index,
  // either directly or indirectly.
  and not exists ( PerformInline guard |
    ( guard = loop
      or (
        use.hasAncestor(guard) and
        guard.hasAncestor(loop)
      )
    )
    and loopConstrainsIndex(guard, index)
  )
select use, "$@ may overflow array index $@.",
       loop, "Loop",
       index, index.getName()

predicate suspectLoop(PerformInline loop,
     QualifiedDataNameWithSubscripts use,
     DataDescriptionEntry index) {
  indexUsedInLoop(index, use, loop) and
  indexIncrementedInLoop(index, loop)
}
       
predicate indexUsedInLoop(
    DataDescriptionEntry index, 
    QualifiedDataNameWithSubscripts use, 
    PerformInline loop) {
  use.hasAncestor(loop) and
  exists ( RelativeSubscript reference |
    reference.hasAncestor(use) and
    index = reference.getReference().getTarget()
  )
}

predicate indexIncrementedInLoop(DataDescriptionEntry index, PerformInline loop) {
  exists ( Add add, Identifier id | 
    add.hasAncestor(loop) and
    id = add.getAReceivingOperand() and
    index = id.(QualifiedDataNameWithSubscripts).getReference().getTarget()
  )
}

predicate loopConstrainsIndex(PerformInline loop, DataDescriptionEntry index) {
  exists (Reference ref |
    index = ref.getTarget() and
    // This is a broad interpretation of "loop constrains". It's really checking
    // if the index is mentioned at all, taking that mention as being a good enough
    // indication of there being a constraint.
    ref.hasAncestor(loop.getLoopForm())
  )
}

