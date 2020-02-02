/**
 * @id cbl/value-clause-in-linkage-section
 * @name Data value clause used in linkage section
 * @description Avoid using the data value clause in a linkage section.
 * @kind problem
 * @problem.severity error
 * @precision high
 * @tags correctness
 */

import cobol

from LinkageSection ls, DataDescriptionEntry dde, ValueClause vc
where
	dde.getValue() = vc and
	dde.getParent*() = ls and
	dde.getLevelNumber() != 88 // level 88 data description entry is a condition-name
select vc, "Avoid using the data value clause in a linkage section."
