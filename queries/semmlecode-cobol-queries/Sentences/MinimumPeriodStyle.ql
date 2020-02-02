/**
 * @id cbl/spurious-period
 * @name Spurious periods
 * @description Misplaced periods reduce readability and are a source of bugs.
 * @kind problem
 * @problem.severity recommendation
 * @tags maintainability
 */

import cobol

from SentenceList list, int numberOfRequiredPeriods
where numberOfRequiredPeriods
        = count ( SentenceWhichNeedsAPeriod s | s = list.getAnItem() )
  and numberOfRequiredPeriods < list.size()
select list, "There are " + list.size() + " sentence(s) here, "
             + "but you really only need " + numberOfRequiredPeriods + "."


class SentenceWhichNeedsAPeriod extends Sentence {
  SentenceWhichNeedsAPeriod() {
    this = getParent().(SentenceList).getLastItem() or
    exists ( NextSentence next | next.hasAncestor(this) )
  }
}