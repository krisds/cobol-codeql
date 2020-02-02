/**
 * @id cbl/internal/extraction-error
 * @name Extractor error
 * @description The Cobol extractor encountered an error.
 * @kind problem
 * @problem.severity info
 */

import cobol

from Error e
select e, e.toString()