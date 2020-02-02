/**
 * @id cbl/sql/leading-wildcard-in-like-condition
 * @name LIKE with leading wildcard
 * @description Patterns which start with a wildcard will prevent the use of indexes
 *    and can drastically reduce query performance.
 * @kind problem
 * @problem.severity warning
 * @precision high
 * @tags performance
 */

import cobol

from SqlLikePredicate like, string pattern
where
  pattern = like.getPattern() and (
    pattern.charAt(1) = "%" or
    pattern.charAt(1) = "_"
  )
select like, "LIKE pattern " + pattern + " starts with a wildcard."
