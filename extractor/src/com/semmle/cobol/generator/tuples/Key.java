package com.semmle.cobol.generator.tuples;

import com.semmle.cobol.generator.types.TrappableType;
import com.semmle.cobol.generator.types.Type;

import koopa.core.trees.Tree;

/**
 * The lookup-key for any tuple. There should be no two tuples sharing the same
 * key.
 * <p>
 * It consists of:
 * <ul>
 * <li>the {@linkplain Type} of the tuple, which must be a
 * {@linkplain TrappableType},</li>
 * <li>the subject of the tuple (i.e. the thing being trapped), which we expect
 * to be either a {@linkplain Tree} or another {@linkplain Tuple}, and</li>
 * <li>a "topic", which is a String differentiating multiple tuples for the same
 * subject.</li>
 * </ul>
 */
public class Key {
	public static final Key NULL = new Key(null, null, null);

	public final TrappableType type;
	public final Object subject;
	public final String topic;

	public Key(TrappableType type, Object subject, String topic) {
		super();
		this.type = type;
		this.subject = subject;
		this.topic = topic;
	}

	@Override
	public int hashCode() {
		return type.hashCode() + (subject == null ? 0 : subject.hashCode())
				+ topic.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Key))
			return false;

		final Key other = (Key) obj;
		return type == other.type && subject == other.subject
				&& topic.equals(other.topic);
	}

	@Override
	public String toString() {
		return "(" + type + ", " + subject + ", " + topic + ")";
	}
}