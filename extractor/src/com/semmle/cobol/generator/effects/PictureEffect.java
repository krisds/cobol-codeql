package com.semmle.cobol.generator.effects;

import static com.semmle.cobol.extractor.CobolExtractor.getDatabaseType;
import static com.semmle.cobol.extractor.CobolExtractor.getType;
import static com.semmle.cobol.util.Util.countOccurrences;
import static com.semmle.cobol.util.Util.countSymbols;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.semmle.cobol.generator.Trap;
import com.semmle.cobol.generator.engine.Frame;
import com.semmle.cobol.generator.engine.RuleEngine;
import com.semmle.cobol.generator.events.Event;
import com.semmle.cobol.generator.tuples.Tuple;
import com.semmle.cobol.generator.types.Attribute;
import com.semmle.cobol.generator.types.DatabaseType;
import com.semmle.cobol.generator.types.Partition;
import com.semmle.cobol.generator.types.Type;
import com.semmle.cobol.generator.types.TypeWithAttributes;

/**
 * This {@link Effect} takes the <code>picture_string</code> attribute from the
 * tuple, and generates the <code>normalized_picture_string</code> and
 * <code>category</code> counterparts.
 *
 */
class PictureEffect implements Effect {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(PictureEffect.class);

	public static final String ALPHABETIC = "alphabetic";
	public static final String ALPHANUMERIC = "alphanumeric";
	public static final String ALPHANUMERIC_EDITED = "alphanumeric-edited";
	public static final String BOOLEAN = "boolean";
	public static final String NATIONAL = "national";
	public static final String NATIONAL_EDITED = "national-edited";
	public static final String NUMERIC = "numeric";
	public static final String NUMERIC_EDITED = "numeric-edited";

	private static final String SYMBOL_SET = "ABENPSVXZ019,./*+-$";
	private static final int SYMBOL_A = SYMBOL_SET.indexOf("A");
	private static final int SYMBOL_B = SYMBOL_SET.indexOf("B");
	private static final int SYMBOL_E = SYMBOL_SET.indexOf("E");
	private static final int SYMBOL_N = SYMBOL_SET.indexOf("N");
	private static final int SYMBOL_P = SYMBOL_SET.indexOf("P");
	private static final int SYMBOL_S = SYMBOL_SET.indexOf("S");
	private static final int SYMBOL_V = SYMBOL_SET.indexOf("V");
	private static final int SYMBOL_X = SYMBOL_SET.indexOf("X");
	private static final int SYMBOL_Z = SYMBOL_SET.indexOf("Z");
	private static final int SYMBOL_0 = SYMBOL_SET.indexOf("0");
	private static final int SYMBOL_1 = SYMBOL_SET.indexOf("1");
	private static final int SYMBOL_9 = SYMBOL_SET.indexOf("9");
	private static final int SYMBOL_COMMA = SYMBOL_SET.indexOf(",");
	private static final int SYMBOL_DOT = SYMBOL_SET.indexOf(".");
	private static final int SYMBOL_SLASH = SYMBOL_SET.indexOf("/");
	private static final int SYMBOL_STAR = SYMBOL_SET.indexOf("*");
	private static final int SYMBOL_PLUS = SYMBOL_SET.indexOf("+");
	private static final int SYMBOL_MINUS = SYMBOL_SET.indexOf("-");
	private static final int SYMBOL_DOLLAR = SYMBOL_SET.indexOf("$");

	@Override
	public void apply(Event event, Frame frame, RuleEngine engine) {
		final String pic = frame.tuple.getStringAttribute("picture_string");

		final String normalized = getNormalizedPictureString(pic);
		if (normalized == null) {
			if (LOGGER.isTraceEnabled())
				LOGGER.trace("PIC {} could not be normalized", pic);
			return;
		}

		final String category = getCategory(normalized);

		if (LOGGER.isTraceEnabled())
			LOGGER.trace("PIC {} is a(n) {}, and normalizes to {}", pic,
					category, normalized);

		trapStringAttribute("normalized_picture_string", normalized, frame,
				engine);

		if (category != null)
			trapStringAttribute("category", category, frame, engine);
	}

	public void trapStringAttribute(String attributeName, String value,
			Frame frame, RuleEngine engine) {

		final TypeWithAttributes pictureClause = (TypeWithAttributes) getType(
				frame.tuple);

		final Attribute attribute = pictureClause.getAttribute(attributeName);
		final Type attributeType = getType(attribute);
		final DatabaseType attributeDBType = getDatabaseType(attributeType);
		final Partition p = (Partition) attributeDBType;

		final Tuple tuple = Trap.trapTuple(attributeDBType.getName(),
				frame.node, p.getValueColumn(), engine);
		tuple.addConstantValue(p.getValueColumn(), value);

		Trap.parentTupleToAttribute(frame.tuple, attribute, tuple);
	}

	@Override
	public String toString() {
		return "picture";
	}

	/**
	 * Given a picture string, normalizes it.
	 * <p>
	 * Specifically, this means that occurrences of
	 * <code><i>C</i>(<i>N</i>)</code> get replaced by
	 * <code><i>CCCC...C</i></code>. If N <b>can not</b> be cast to an integer
	 * then this method will return <code>null</code> instead.
	 */
	public static String getNormalizedPictureString(String picture) {
		picture = picture.toUpperCase();

		StringBuilder b = new StringBuilder();

		int i = 0;
		while (i < picture.length()) {
			char c = picture.charAt(i);

			if (c == '(') {
				final int l = i;

				while (i < picture.length() && picture.charAt(i) != ')')
					i++;

				final int r = i;

				final String n = picture.substring(l + 1, r).trim();
				final int m;
				try {
					m = Integer.parseInt(n);
				} catch (NumberFormatException e) {
					return null;
				}

				c = picture.charAt(l - 1);
				for (int o = 1; o < m; o++)
					b.append(c);

			} else {
				b.append(c);
			}

			i++;
		}

		return b.toString();
	}

	/**
	 * "A PICTURE clause defines the subject of the entry to fall into one of
	 * the following categories of data: alphabetic - alphanumeric -
	 * alphanumeric-edited - boolean - national - national-edited - numeric -
	 * numeric-edited." - Cobol standard, section 13.18.39.3 "Picture Clause -
	 * General rules".
	 * <p>
	 * This method applies the rules as specified in the standard to figure out
	 * the entry's category.
	 */
	public static String getCategory(String characterString1) {
		int[] count = countSymbols(characterString1, SYMBOL_SET);
		int countCR = countOccurrences(characterString1, "CR");
		int countDB = countOccurrences(characterString1, "DB");
		count[SYMBOL_B] -= countDB;

		// "To define an item as alphabetic, character-string-1 shall contain
		// only one or more occurrences of the symbol 'A'."
		if (count[SYMBOL_A] == characterString1.length())
			return ALPHABETIC;

		// "To define an item as alphanumeric, character-string-1 shall contain
		// a combination of symbols from the set 'A', 'X', and '9', that
		// includes
		// - at least one symbol 'X', or
		// - at least two different symbols from this set."
		if ((count[SYMBOL_X] > 0
				|| (count[SYMBOL_A] > 0 && count[SYMBOL_9] > 0))
				&& count[SYMBOL_X] + count[SYMBOL_A]
						+ count[SYMBOL_9] == characterString1.length())
			return ALPHANUMERIC;

		// "To define an item as alphanumeric-edited, character-string-1 shall
		// include
		// - at least one symbol 'A' or one symbol 'X', and
		// - at least one of the symbols from the set 'B', '0', '/'."
		if ((count[SYMBOL_A] > 0 || count[SYMBOL_X] > 0) && (count[SYMBOL_B] > 0
				|| count[SYMBOL_0] > 0 || count[SYMBOL_SLASH] > 0))
			return ALPHANUMERIC_EDITED;

		// "To define an item as boolean, character-string-1 shall contain only
		// one or more occurrences of the symbol '1'."
		if (count[SYMBOL_1] == characterString1.length())
			return BOOLEAN;

		// "To define an item as national, character-string-1 shall contain
		// only one or more occurrences of the symbol 'N'."
		if (count[SYMBOL_N] == characterString1.length())
			return NATIONAL;

		// "To define an item as national-edited, character-string-1 shall
		// include
		// - at least one symbol 'N', and
		// - at least one of the symbols from the set 'B', '0', '/'."
		if (count[SYMBOL_N] > 0 && (count[SYMBOL_B] > 0 || count[SYMBOL_0] > 0
				|| count[SYMBOL_SLASH] > 0))
			return NATIONAL_EDITED;

		// "To define an item as numeric, character-string-1
		// - shall include at least one symbol '9', and
		// - may contain a combination of symbols from the set 'P', 'S', and
		// 'V'."
		if (count[SYMBOL_9] > 0
				&& (count[SYMBOL_9] + count[SYMBOL_P] + count[SYMBOL_S]
						+ count[SYMBOL_V] == characterString1.length()))
			return NUMERIC;

		// To define an item as numeric-edited, one of the following options
		// shall be specified:
		if (count[SYMBOL_E] == 0) {
			// a) To produce a fixed-point edited result, character-string-1
			// shall include:
			// - at least one symbol 'Z'; or
			// - at least one symbol '*'; or
			// - at least two identical symbols from the set '+', '-', currency
			// symbol; or
			// - at least one symbol '9' and at least one of the symbols from
			// the set 'B', 'CR', 'DB', '0', '/', ',', '.', '+', '-', the
			// currency symbol.
			if (count[SYMBOL_Z] > 0 || count[SYMBOL_STAR] > 0)
				return NUMERIC_EDITED;

			if (count[SYMBOL_PLUS] >= 2 || count[SYMBOL_MINUS] >= 2
					|| count[SYMBOL_DOLLAR] >= 2)
				return NUMERIC_EDITED;

			if (count[SYMBOL_9] > 0)
				if (count[SYMBOL_B] > 0 || countCR > 0 || countDB > 0
						|| count[SYMBOL_0] > 0 || count[SYMBOL_SLASH] > 0
						|| count[SYMBOL_COMMA] > 0 || count[SYMBOL_DOT] > 0
						|| count[SYMBOL_PLUS] > 0 || count[SYMBOL_MINUS] > 0
						|| count[SYMBOL_DOLLAR] > 0)
					return NUMERIC_EDITED;

		} else {
			// b) To produce a floating-point edited result, characters-string-1
			// shall consist of two parts, separated without any spaces, by the
			// symbol 'E'. The first part represents the significand; the second
			// part represents the exponent.
			// The significand shall be a valid character-string for either a
			// numeric item or a numeric-edited item for a fixed-point result.
			// Neither floating insertion editing nor zero suppression with
			// replacement shall be specified for the significand.
			// The exponent shall be '+9', '+99', '+999', '+9999', or '+9(n)'
			// where
			// n = 1, 2, 3, or 4.
			int e = characterString1.indexOf("E");
			String significand = characterString1.substring(0, e);
			String significandCategory = getCategory(significand);

			if (NUMERIC.equals(significandCategory)
					|| NUMERIC_EDITED.equals(significandCategory)) {
				String exponent = characterString1.substring(e + 1);
				if (exponent.charAt(0) == '+' && countOccurrences(exponent,
						"9") == exponent.length() - 1)
					return NUMERIC_EDITED;
			}
		}

		return null;
	}
}
