package com.semmle.cobol.generator.test;

import koopa.core.data.Data;
import koopa.core.targets.Target;

/**
 * Something which will push {@link Data} into a {@link Target}.
 */
interface Stream {

	void streamInto(Target target);
}
