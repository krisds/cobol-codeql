/**
 * @id cbl/incorrectly-typed-initial-value
 * @name Data item initialized with incorrect data type
 * @description Data items should be initialized with the correct data type.
 * @kind problem
 * @problem.severity error
 * @precision high
 * @tags correctness
 */

import cobol

predicate incorrectNumericInitialize(DataDescriptionEntry dde, string message) {
	dde.getPicture().isNumeric() and
	dde.getValue().getLiteral().isAlphanumeric() and
	message = "Numeric data items should not be initialized with an alphanumeric value."
}

predicate incorrectAlphanumericInitialize(DataDescriptionEntry dde, string message) {
	dde.getPicture().isAlphanumeric() and
	not dde.getPicture().isCharacter() and // exception: single character alphanumeric
	dde.getValue().getLiteral().isNumeric() and
	message = "Alphanumeric data items should not be initialized with a numeric value."
}

from DataDescriptionEntry dde, string message
where
	incorrectNumericInitialize(dde, message) or
	incorrectAlphanumericInitialize(dde, message)
select dde.getValue(), message
