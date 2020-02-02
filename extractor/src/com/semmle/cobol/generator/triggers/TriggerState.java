package com.semmle.cobol.generator.triggers;

import java.util.List;

import com.semmle.cobol.generator.effects.Effect;
import com.semmle.cobol.generator.events.Event;

/**
 * When asking a {@link Trigger} whether it applies to an {@link Event}, this is
 * how it replies.
 */
public class TriggerState {

	/**
	 * Did the {@link Trigger} fire, and should the associated {@link Effect} be
	 * run ?
	 */
	public final boolean fired;

	/**
	 * Did the {@link Trigger} expire, and should it no longer be evaluated ?
	 */
	public final boolean expired;

	/**
	 * Does the {@link Trigger} want to add more {@link Trigger}s ?
	 */
	public final List<TriggerDefinition> also;

	private TriggerState(boolean fired, boolean expired) {
		this.fired = fired;
		this.expired = expired;
		this.also = null;
	}

	public TriggerState(boolean fired, boolean expired,
			List<TriggerDefinition> also) {
		this.fired = fired;
		this.expired = expired;
		this.also = also;
	}

	/**
	 * Returns a version of this trigger, but which has expired.
	 * <p>
	 * If this trigger has already expires, returns <code>this</code>.
	 */
	public TriggerState andExpired() {
		if (expired)
			return this;
		else
			return new TriggerState(fired, true, also);
	}

	/**
	 * Returns a version of this trigger, but which also asks to add more
	 * triggers.
	 * <p>
	 * If the given list is <code>null</code> or empty, returns
	 * <code>this</code>.
	 */
	public TriggerState also(List<TriggerDefinition> also) {
		if (also == null || also.isEmpty())
			return this;
		else
			return new TriggerState(fired, expired, also);
	}

	/**
	 * The trigger fired and is now expired.
	 */
	private static final TriggerState FIRED_AND_EXPIRED = new TriggerState(true,
			true) {
		@Override
		public TriggerState andExpired() {
			return this;
		}
	};

	/**
	 * The trigger fired.
	 */
	public static final TriggerState FIRED = new TriggerState(true, false) {
		@Override
		public TriggerState andExpired() {
			return FIRED_AND_EXPIRED;
		}
	};

	/**
	 * The trigger did not fire, but is now expired.
	 */
	private static final TriggerState EXPIRED = new TriggerState(false, true) {
		@Override
		public TriggerState andExpired() {
			return this;
		}
	};

	/**
	 * The trigger did not fire.
	 */
	public static final TriggerState INACTIVE = new TriggerState(false, false) {
		@Override
		public TriggerState andExpired() {
			return EXPIRED;
		}
	};

	/**
	 * Turn a boolean value into {@link #FIRED} (if <code>true</code>) or
	 * {@link #INACTIVE} (if <code>false</code>).
	 */
	public static TriggerState fromBoolean(boolean b) {
		return b ? FIRED : INACTIVE;
	}
}
