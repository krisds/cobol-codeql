/**
 * @id cbl/internal/location-problem
 * @name Cobol Location sanity check
 * @description Every Location should be associated with exactly one program element.
 * @kind problem
 * @problem.severity info
 */

import cobol
import Uniqueness

predicate location_sanity(Location l, string clsname, string problem, string what) {
  clsname = l.getAQlClass() and
    (uniqueness_error(count(l.toString()), "toString", problem) and what = "at " + l.toString() or
     not exists(l.toString()) and problem = "no toString" and what = "a location")
}

from Location l, string clsname, string problem, string what
where location_sanity(l, clsname, problem, what)
select l, clsname + " " + what + " has " + problem
