package com.semmle.cobol.population;

import static com.semmle.cobol.util.Common.COMPILER_STATEMENT;
import static com.semmle.cobol.util.Common.DECLARATIVE_SECTION;
import static com.semmle.cobol.util.Common.PERFORM;
import static com.semmle.cobol.util.Common.STATEMENT;

import java.util.Map;

import com.semmle.cobol.generator.tables.Column;
import com.semmle.cobol.generator.tables.DatabaseScheme;
import com.semmle.cobol.generator.tables.Relation;
import com.semmle.cobol.generator.types.DBType;
import com.semmle.cobol.generator.types.PredefinedType;
import com.semmle.cobol.generator.types.TypeSystem;

import koopa.core.data.markers.Start;

/**
 * This class sets up some common definitions for trap data. These are:
 * <ul>
 * <li>locations_default</li>
 * <li>hasLocation</li>
 * </ul>
 * 
 * Locations may be caught by having scripts call "location_trap".
 */
public final class CommonPopulator {

	private CommonPopulator() {
	}

	public static void populateTypeSystem(TypeSystem typeSystem) {
		PredefinedType locations_default = new PredefinedType(
				"locations_default", "locations_default");
		typeSystem.addType(locations_default);

		PredefinedType hasLocation = new PredefinedType("hasLocation",
				"hasLocation");
		typeSystem.addType(hasLocation);

		PredefinedType numlines = new PredefinedType("numlines", "numlines");
		typeSystem.addType(numlines);

		PredefinedType halstead = new PredefinedType("halstead_counts",
				"halstead_counts");
		typeSystem.addType(halstead);

		PredefinedType successors = new PredefinedType("successor",
				"successors");
		typeSystem.addType(successors);

		PredefinedType errors = new PredefinedType("error", "errors");
		typeSystem.addType(errors);

		PredefinedType compgenerated = new PredefinedType("compgenerated",
				"compgenerated");
		typeSystem.addType(compgenerated);

		PredefinedType commentScopes = new PredefinedType("commentScopes",
				"commentScopes");
		typeSystem.addType(commentScopes);
	}

	public static void populateDatabaseScheme(DatabaseScheme databaseScheme) {
		Relation locations_default = new Relation("locations_default");
		locations_default.addColumn(new Column("id", DBType.INT));
		locations_default.addColumn(new Column("file", DBType.INT));
		locations_default.addColumn(new Column("beginLine", DBType.INT));
		locations_default.addColumn(new Column("beginColumn", DBType.INT));
		locations_default.addColumn(new Column("endLine", DBType.INT));
		locations_default.addColumn(new Column("endColumn", DBType.INT));
		databaseScheme.addRelation(locations_default);

		Relation hasLocation = new Relation("hasLocation");
		hasLocation.addColumn(new Column("locatable", DBType.INT));
		hasLocation.addColumn(new Column("location", DBType.INT));
		databaseScheme.addRelation(hasLocation);

		Relation numlines = new Relation("numlines");
		numlines.addColumn(new Column("element_id", DBType.INT));
		numlines.addColumn(new Column("num_lines", DBType.INT));
		numlines.addColumn(new Column("num_code", DBType.INT));
		numlines.addColumn(new Column("num_comment", DBType.INT));
		numlines.addColumn(new Column("num_water", DBType.INT));
		databaseScheme.addRelation(numlines);

		Relation halstead = new Relation("halstead_counts");
		halstead.addColumn(new Column("halstead_countable", DBType.INT));
		halstead.addColumn(new Column("n1", DBType.INT));
		halstead.addColumn(new Column("n2", DBType.INT));
		halstead.addColumn(new Column("N1", DBType.INT));
		halstead.addColumn(new Column("N2", DBType.INT));
		databaseScheme.addRelation(halstead);

		Relation successors = new Relation("successors");
		successors.addColumn(new Column("predecessor", DBType.INT));
		successors.addColumn(new Column("successor", DBType.INT));
		databaseScheme.addRelation(successors);

		Relation errors = new Relation("errors");
		errors.addColumn(new Column("id", DBType.INT));
		errors.addColumn(new Column("message", DBType.VARCHAR));
		// See ErrorContext enum for values.
		errors.addColumn(new Column("context", DBType.INT));
		databaseScheme.addRelation(errors);

		Relation compgenerated = new Relation("compgenerated");
		compgenerated.addColumn(new Column("id", DBType.INT));
		databaseScheme.addRelation(compgenerated);

		Relation commentScopes = new Relation("commentScopes");
		commentScopes.addColumn(new Column("comment", DBType.INT));
		commentScopes.addColumn(new Column("location", DBType.INT));
		databaseScheme.addRelation(commentScopes);
	}

	public static void populateDefaultTypes(Map<Start, String> defaultTypes) {
		defaultTypes.put(DECLARATIVE_SECTION, "section");
		defaultTypes.put(STATEMENT, "stmt");
		defaultTypes.put(COMPILER_STATEMENT, "stmt");
		defaultTypes.put(PERFORM, "perform");
	}
	
	public static enum ErrorContext {
		/** Context was not specified. */
		UNSPECIFIED(0), 
		
		/** The error occurred during parsing. */
		PARSE(1), 
		
		/** The error occurred during trapping.  */
		TRAP(2);
		
		public final int id;

		private ErrorContext(int id) {
			this.id = id;
		}
	}
}
