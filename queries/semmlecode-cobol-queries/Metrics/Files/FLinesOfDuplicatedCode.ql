/**
 * @id cbl/duplicated-lines-in-files
 * @name Duplicated lines in files
 * @description The number of lines in a file (including code, comment and whitespace lines)
 *              occurring in a block of lines that is duplicated at least once somewhere else.
 * @kind treemap
 * @treemap.warnOn highValues
 * @metricType file
 * @metricAggregate avg sum max
 * @tags testability
 *       modularity
 */
import external.CodeDuplication

/**
 * Does line l of file f belong to a block of lines that is duplicated somewhere else?
 */
predicate dupLine(int l, File f) {
  exists (DuplicateBlock d | d.sourceFile() = f |
    l in [d.sourceStartLine()..d.sourceEndLine()]
  )
}

from File f, int n
where n = count (int l | dupLine(l, f))
select f, n
order by n desc
