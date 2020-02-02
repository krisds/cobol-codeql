/**
 * @id cbl/collapsible-if
 * @name Collapsible IF statements
 * @description Nested 'IF' statements without 'ELSE' branches can be merged into a single statement,
 *              improving readability and preserving the meaning.
 * @kind treemap
 * @treemap.warnOn highValues
 * @metricType compilation-unit
 * @metricAggregate max
 * @tags readability
 */
import cobol

from RootOfCollapsibleIfThen c, int n
where n = c.getCollapsibleDepth()
select c, n
order by n desc

class IfThen extends IfThenElse {
  IfThen() {
    not exists (getElse()) or
    getElse().getStatementsSize() = 0
  }
}

class CollapsibleIfThen extends IfThen {
  CollapsibleIfThen() {
    getThen().getStatementsSize() = 1 and
    getThen().getFirstStatement() instanceof IfThen
  }
  
  int getCollapsibleDepth() {
    if (getThen().getFirstStatement() instanceof CollapsibleIfThen) then
      result = getThen().getFirstStatement().(CollapsibleIfThen).getCollapsibleDepth() + 1
    else
      result = 1
  }
}

class RootOfCollapsibleIfThen extends CollapsibleIfThen {
  RootOfCollapsibleIfThen() {
    not (getParent().getParent().getParent() instanceof CollapsibleIfThen)
  }
}
