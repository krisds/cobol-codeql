package com.semmle.cobol.mapping.source;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import com.semmle.cobol.extractor.Main;
import com.semmle.util.exception.Exceptions;

import koopa.core.data.Data;
import koopa.core.sources.LineSplitter;
import koopa.core.sources.Source;

public class MappingTokens {
	public static Source getNewSource(String resourceName, Reader reader) {
		Source source;

		source = new LineSplitter(resourceName, reader);
		source = new MappingSource(source);
		source = new IndentationToNesting(source);

		return source;
	}

	public static void main(String[] args) throws FileNotFoundException,
			UnsupportedEncodingException {
		InputStreamReader reader = null;
		try {
			InputStream stream = Main.class
					.getResourceAsStream("/com/semmle/cobol/population/cobol.mapping");
			reader = new InputStreamReader(stream, "UTF-8");
			Source source = getNewSource("", reader);

			Data d = null;
			while ((d = source.next()) != null)
				System.out.println(d);

		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				Exceptions.ignore(e, e.getMessage());
			}
		}
	}
}
