package com.semmle.cobol.mapping.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import com.semmle.cobol.extractor.Main;
import com.semmle.cobol.mapping.grammar.MappingGrammar;
import com.semmle.cobol.mapping.source.MappingTokens;
import com.semmle.util.exception.ResourceError;

import koopa.core.parsers.Parse;
import koopa.core.sources.Source;
import koopa.core.trees.KoopaTreeBuilder;
import koopa.core.trees.Tree;
import koopa.core.trees.ui.TreeFrame;

public final class MappingUtil {

	private MappingUtil() {
	}

	public static Tree getAST(String scriptName, String scriptLocation) {
		try {
			InputStream stream = Main.class.getResourceAsStream(scriptLocation);
			InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
			return getAST(scriptName, reader);

		} catch (IOException e) {
			throw new ResourceError("IOException while loading script "
					+ scriptLocation, e);
		}
	}

	public static Tree getAST(File input) throws IOException {
		return getAST(input.getName(), new FileReader(input));
	}

	public static Tree getStatementASTFromCode(String code) {
		final Source source = MappingTokens.getNewSource(//
				"code", new StringReader(code));

		final MappingGrammar grammar = new MappingGrammar();

		final Parse parse = Parse.of(source).to(new KoopaTreeBuilder(grammar));

		boolean accepts = grammar.statement().accepts(parse);

		if (!accepts)
			return null;

		final KoopaTreeBuilder builder = parse
				.getTarget(KoopaTreeBuilder.class);
		Tree ast = builder.getTree();

		return ast;
	}

	public static Tree getASTFromCode(String code) {
		final Source source = MappingTokens.getNewSource(//
				"code", new StringReader(code));

		final MappingGrammar grammar = new MappingGrammar();

		final Parse parse = Parse.of(source).to(new KoopaTreeBuilder(grammar));

		boolean accepts = grammar.rules().accepts(parse);

		if (!accepts)
			return null;

		final KoopaTreeBuilder builder = parse
				.getTarget(KoopaTreeBuilder.class);
		Tree ast = builder.getTree();

		return ast;
	}

	public static Tree getAST(String resourceName, Reader reader)
			throws IOException {
		final Source source = MappingTokens.getNewSource(//
				resourceName, reader);

		final MappingGrammar grammar = new MappingGrammar();

		final Parse parse = Parse.of(source).to(new KoopaTreeBuilder(grammar));

		boolean accepts = grammar.rules().accepts(parse);

		if (!accepts)
			return null;

		final KoopaTreeBuilder builder = parse
				.getTarget(KoopaTreeBuilder.class);
		Tree ast = builder.getTree();

		return ast;
	}

	public static void main(String[] args) throws IOException {
		Tree ast = getAST("main", "/com/semmle/cobol/population/cobol.mapping");
		new TreeFrame("main", ast).setVisible(true);
	}
}
