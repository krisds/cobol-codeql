package com.semmle.cobol.timing;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Timing {
	private static final Logger TIMING = LoggerFactory.getLogger(Timing.class);

	private static final boolean ACTIVE = TIMING.isTraceEnabled();

	private static Map<String, Long> running = new LinkedHashMap<>();

	public static void start(String process) {
		if (!ACTIVE)
			return;

		final long start = System.currentTimeMillis();
		running.put(process, start);
		TIMING.trace("(x) start of {} at {}", process, start);
	}

	public static void end(String process) {
		if (!ACTIVE)
			return;

		final long end = System.currentTimeMillis();
		TIMING.trace("(x) end of {} at {}", process, end);

		final long start = running.get(process);
		running.remove(process);

		TIMING.trace("(x) {} : {}ms", process, end - start);
	}
}
