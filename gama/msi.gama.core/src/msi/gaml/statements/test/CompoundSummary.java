/*******************************************************************************************************
 *
 * CompoundSummary.java, in msi.gama.core, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package msi.gaml.statements.test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.emf.common.util.URI;

import com.google.common.base.Objects;

import msi.gama.util.GamaMapFactory;
import one.util.streamex.StreamEx;

/**
 * A summary composed of other summaries (for instance, a TestStatement summary is composed of AsserStatement summaries)
 *
 * @author drogoul
 *
 * @param <S>
 *            the type of the statement represented by this summary
 *
 * @param <T>
 *            the type of the sub-summaries
 *
 */
public class CompoundSummary<T extends AbstractSummary<?>, S extends WithTestSummary<?>> extends AbstractSummary<S> {

	/** The summaries. */
	public final Map<String, T> summaries = GamaMapFactory.create();
	
	/** The aborted. */
	public boolean aborted;
	
	/** The string summary. */
	public String stringSummary;

	/**
	 * Instantiates a new compound summary.
	 *
	 * @param symbol the symbol
	 */
	@SuppressWarnings ("unchecked")
	public CompoundSummary(final S symbol) {
		super(symbol);
		if (symbol != null) {
			symbol.getSubElements().forEach(a -> addSummary((T) a.getSummary()));
		}

	}

	/**
	 * Instantiates a new compound summary.
	 *
	 * @param summaries the summaries
	 */
	public CompoundSummary(final Collection<T> summaries) {
		super(null);
		summaries.forEach(a -> addSummary(a));
	}

	// public CompoundSummary(final Collection<T> summaries) {
	// super(null);
	// summaries.forEach(a -> addSummary(a));
	// }

	/**
	 * Instantiates a new compound summary.
	 */
	public CompoundSummary() {
		this(Collections.EMPTY_LIST);
	}

	/**
	 * Checks if is empty.
	 *
	 * @return true, if is empty
	 */
	public boolean isEmpty() {
		return summaries.isEmpty();
	}

	@Override
	public long getTimeStamp() {
		return StreamEx.ofValues(summaries).mapToLong(s -> s.getTimeStamp()).max().getAsLong();
	}

	@Override
	public int size() {
		return summaries.size();
	}

	@Override
	public void setState(final TestState s) {
		aborted = s == TestState.ABORTED;
	}

	@Override
	public Map<String, ? extends AbstractSummary<?>> getSummaries() {
		return summaries;
	}

	/**
	 * Adds the summary.
	 *
	 * @param summary the summary
	 */
	public void addSummary(final T summary) {
		final String originalKey = summary.getTitle();
		String key = originalKey;
		int i = 2;
		while (summaries.containsKey(key)) {
			key = originalKey + "[" + i++ + "]";
		}
		summaries.put(key, summary);
	}

	/**
	 * Adds the summaries.
	 *
	 * @param sum the sum
	 */
	public void addSummaries(final Collection<T> sum) {
		for (final T s : sum) {
			addSummary(s);
		}
	}

	@Override
	public void reset() {
		summaries.values().forEach(u -> u.reset());
		aborted = false;
	}

	@SuppressWarnings ("unchecked")
	@Override
	public boolean equals(final Object o) {
		if (!getClass().isInstance(o)) { return false; }
		return Objects.equal(((AbstractSummary<?>) o).getTitle(), getTitle());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getTitle());
	}

	@Override
	public TestState getState() {
		if (aborted) { return TestState.ABORTED; }
		TestState state = TestState.NOT_RUN;
		for (final AbstractSummary<?> a : summaries.values()) {
			final TestState s = a.getState();
			switch (s) {
				case NOT_RUN:
					break;
				case FAILED:
					state = TestState.FAILED;
					break;
				case PASSED:
					if (state.equals(TestState.NOT_RUN)) {
						state = TestState.PASSED;
					}
					break;
				case WARNING:
					if (state.equals(TestState.PASSED) || state.equals(TestState.NOT_RUN)) {
						state = TestState.WARNING;
					}
					break;
				case ABORTED:
					return TestState.ABORTED;
			}
		}
		return state;

	}

	@Override
	public int countTestsWith(final TestState state) {
		return StreamEx.ofValues(summaries).mapToInt(s -> s.countTestsWith(state)).sum();
	}

	/**
	 * Gets the sub summaries belonging to.
	 *
	 * @param fileURI the file URI
	 * @param collector the collector
	 * @return the sub summaries belonging to
	 */
	public void getSubSummariesBelongingTo(final URI fileURI, final List<AbstractSummary<?>> collector) {
		getSummaries().values().forEach(s -> {
			if (matches(s.getURI(), fileURI)) {
				collector.add(s);
			} else if (s instanceof CompoundSummary) {
				((CompoundSummary<?, ?>) s).getSubSummariesBelongingTo(fileURI, collector);
			}
		});
	}

	/**
	 * Matches.
	 *
	 * @param summary the summary
	 * @param query the query
	 * @return true, if successful
	 */
	private boolean matches(final URI summary, final URI query) {
		if (summary == null) { return false; }
		final String s = summary.toString();
		final String q = query.toString();
		return s.startsWith(q);

	}

	/**
	 * Gets the string summary.
	 *
	 * @return the string summary
	 */
	public String getStringSummary() {
		if (stringSummary == null) {
			stringSummary = createTestsSummary();
		}
		return stringSummary;
	}

	/**
	 * Creates the tests summary.
	 *
	 * @return the string
	 */
	protected String createTestsSummary() {
		final Map<TestState, Integer> map = new TreeMap<>();
		map.put(TestState.ABORTED, 0);
		map.put(TestState.FAILED, 0);
		map.put(TestState.NOT_RUN, 0);
		map.put(TestState.PASSED, 0);
		map.put(TestState.WARNING, 0);
		final int[] size = { 0 };
		summaries.values().forEach(t -> {
			map.keySet().forEach(state -> map.put(state, map.get(state) + t.countTestsWith(state)));
			size[0] += t.size();
		});
		String message = "" + size[0] + " tests";
		for (final TestState s : map.keySet()) {
			if (map.get(s) == 0) {
				continue;
			}
			message += ", " + map.get(s) + " " + s;
		}
		return message;
	}

}