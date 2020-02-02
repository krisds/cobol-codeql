import cobol

/**
 * @id cbl/alert-suppression
 * @name Alert suppression
 * @description Generates information about alert suppressions.
 * @kind alert-suppression
 */

from LgtmSuppressionComment c
select c,                 // suppression comment
       c.getText(),       // text of suppression comment (excluding delimiters)
       c.getAnnotation(), // text of suppression annotation
       c.getScope()       // scope of suppression