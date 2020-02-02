package com.semmle.cobol.generator.effects;

import static com.semmle.cobol.generator.effects.PictureEffect.ALPHABETIC;
import static com.semmle.cobol.generator.effects.PictureEffect.ALPHANUMERIC;
import static com.semmle.cobol.generator.effects.PictureEffect.ALPHANUMERIC_EDITED;
import static com.semmle.cobol.generator.effects.PictureEffect.BOOLEAN;
import static com.semmle.cobol.generator.effects.PictureEffect.NATIONAL;
import static com.semmle.cobol.generator.effects.PictureEffect.NATIONAL_EDITED;
import static com.semmle.cobol.generator.effects.PictureEffect.NUMERIC;
import static com.semmle.cobol.generator.effects.PictureEffect.NUMERIC_EDITED;
import static com.semmle.cobol.generator.effects.PictureEffect.getCategory;
import static com.semmle.cobol.generator.effects.PictureEffect.getNormalizedPictureString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class PictureEffectTest {

	@Test
	public void testNormalization() {
		assertEquals("A", getNormalizedPictureString("A"));
		assertEquals("AAA", getNormalizedPictureString("A(3)"));
		assertEquals("AAAAAB", getNormalizedPictureString("A(5)B"));
		assertEquals("AAAAABCCCD", getNormalizedPictureString("A(5)BC(3)D"));
	}

	@Test
	public void testAlphabetic() {
		assertEquals(ALPHABETIC, getCategory("A"));
		assertEquals(ALPHABETIC, getCategory("AAA"));
	}

	@Test
	public void testAlphanumeric() {
		assertEquals(ALPHANUMERIC, getCategory("XXX"));
		assertEquals(ALPHANUMERIC, getCategory("A9"));
		assertNotEquals(ALPHANUMERIC, getCategory("+XA9"));
	}

	@Test
	public void testAlphanumericEdited() {
		assertEquals(ALPHANUMERIC_EDITED, getCategory("AB"));
		assertEquals(ALPHANUMERIC_EDITED, getCategory("A0"));
		assertEquals(ALPHANUMERIC_EDITED, getCategory("A/"));
		assertEquals(ALPHANUMERIC_EDITED, getCategory("XB"));
		assertEquals(ALPHANUMERIC_EDITED, getCategory("X0"));
		assertEquals(ALPHANUMERIC_EDITED, getCategory("X/"));
	}

	@Test
	public void testBoolean() {
		assertEquals(BOOLEAN, getCategory("1"));
		assertEquals(BOOLEAN, getCategory("111"));
	}

	@Test
	public void testNational() {
		assertEquals(NATIONAL, getCategory("N"));
		assertEquals(NATIONAL, getCategory("NNN"));
	}

	@Test
	public void testNationalEdited() {
		assertEquals(NATIONAL_EDITED, getCategory("NB"));
		assertEquals(NATIONAL_EDITED, getCategory("N0"));
		assertEquals(NATIONAL_EDITED, getCategory("N/"));
	}

	@Test
	public void testNumeric() {
		assertEquals(NUMERIC, getCategory("9"));
		assertEquals(NUMERIC, getCategory("9P"));
		assertEquals(NUMERIC, getCategory("9S"));
		assertEquals(NUMERIC, getCategory("9V"));
	}

	@Test
	public void testFixedPointNumericEdited() {
		assertEquals(NUMERIC_EDITED, getCategory("Z"));
		assertEquals(NUMERIC_EDITED, getCategory("*"));
		assertEquals(NUMERIC_EDITED, getCategory("++"));
		assertEquals(NUMERIC_EDITED, getCategory("--"));
		assertEquals(NUMERIC_EDITED, getCategory("$$"));
		assertEquals(NUMERIC_EDITED, getCategory("9B"));
		assertEquals(NUMERIC_EDITED, getCategory("9CR"));
		assertEquals(NUMERIC_EDITED, getCategory("9DB"));
		assertEquals(NUMERIC_EDITED, getCategory("90"));
		assertEquals(NUMERIC_EDITED, getCategory("9/"));
		assertEquals(NUMERIC_EDITED, getCategory("9,"));
		assertEquals(NUMERIC_EDITED, getCategory("9."));
		assertEquals(NUMERIC_EDITED, getCategory("9+"));
		assertEquals(NUMERIC_EDITED, getCategory("9-"));
		assertEquals(NUMERIC_EDITED, getCategory("9$"));
	}

	@Test
	public void testFloatingPointNumericEdited() {
		assertEquals(NUMERIC_EDITED, getCategory("ZE+9"));
		assertEquals(NUMERIC_EDITED, getCategory("*E+9"));
		assertEquals(NUMERIC_EDITED, getCategory("++E+99"));
		assertEquals(NUMERIC_EDITED, getCategory("--E+99"));
		assertEquals(NUMERIC_EDITED, getCategory("$$E+999"));
		assertEquals(NUMERIC_EDITED, getCategory("9BE+999"));
		assertEquals(NUMERIC_EDITED, getCategory("9CRE+9999"));
		assertEquals(NUMERIC_EDITED, getCategory("9DBE+9999"));
		assertEquals(NUMERIC_EDITED, getCategory("90E+99999"));
		assertEquals(NUMERIC_EDITED, getCategory("9/E+99999"));
		assertEquals(NUMERIC_EDITED, getCategory("9,+999999"));
		assertEquals(NUMERIC_EDITED, getCategory("9.+999999"));
		assertEquals(NUMERIC_EDITED, getCategory("9++9999999"));
		assertEquals(NUMERIC_EDITED, getCategory("9-+99999999"));
		assertEquals(NUMERIC_EDITED, getCategory("9$+999999999"));
	}
}
