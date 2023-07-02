/*******************************************************************************************************
 *
 * Collector.java, in msi.gama.core, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package msi.gama.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.Sets;

import msi.gama.common.util.PoolUtils;
import msi.gama.common.util.RandomUtils;
import msi.gaml.types.Types;

/**
 * A generic class that forwards additions to a set and prevents creating the set if no additions occur
 *
 * @author drogoul
 *
 * @param <E>
 */

public abstract class Collector<E, C extends Collection<E>> implements ICollector<E>, Collection<E> {

	/** The Constant LISTS. */
	private static final PoolUtils.ObjectPool<ICollector<?>> LISTS =
			PoolUtils.create("Ordered Collectors", true, AsList::new, (from, to) -> to.set(from), ICollector::clear);

	/** The Constant SETS. */
	private static final PoolUtils.ObjectPool<ICollector<?>> SETS =
			PoolUtils.create("Unique Collectors", true, AsSet::new, (from, to) -> to.set(from), ICollector::clear);

	/** The Constant ORDERED_SETS. */
	private static final PoolUtils.ObjectPool<ICollector<?>> ORDERED_SETS = PoolUtils.create(
			"Unique Ordered Collectors", true, AsOrderedSet::new, (from, to) -> to.set(from), ICollector::clear);

	/**
	 * Gets the list.
	 *
	 * @param <T> the generic type
	 * @return the list
	 */
	@SuppressWarnings ("unchecked")
	public static final <T> Collector.AsList<T> getList() {
		return (AsList<T>) LISTS.get();
	}

	/**
	 * Gets the sets the.
	 *
	 * @param <T> the generic type
	 * @return the sets the
	 */
	@SuppressWarnings ("unchecked")
	public static final <T> Collector.AsSet<T> getSet() {
		return (AsSet<T>) SETS.get();
	}

	/**
	 * Gets the ordered set.
	 *
	 * @param <T> the generic type
	 * @return the ordered set
	 */
	@SuppressWarnings ("unchecked")
	public static final <T> Collector.AsOrderedSet<T> getOrderedSet() {
		return (AsOrderedSet<T>) ORDERED_SETS.get();
	}

	/**
	 * Release.
	 *
	 * @param <T> the generic type
	 * @param coll the coll
	 */
	public static final <T> void release(final ICollector<T> coll) {
		if (coll instanceof AsList) {
			LISTS.release(coll);
		} else if (coll instanceof AsOrderedSet) {
			ORDERED_SETS.release(coll);
		} else if (coll instanceof AsSet) { SETS.release(coll); }
	}

	@SuppressWarnings ("unchecked")
	@Override
	public void set(final ICollector<?> c) {
		this.addAll((Collection<? extends E>) c.items());
	}

	@Override
	public boolean removeIf(final Predicate<? super E> filter) {
		if (collect != null) return collect.removeIf(filter);
		return ICollector.super.removeIf(filter);
	}

	@Override
	public Spliterator<E> spliterator() {
		if (collect != null) return collect.spliterator();
		return ICollector.super.spliterator();
	}

	@Override
	public Stream<E> stream() {
		if (collect != null) return collect.stream();
		return ICollector.super.stream();
	}

	@Override
	public Stream<E> parallelStream() {
		if (collect != null) return collect.parallelStream();
		return ICollector.super.parallelStream();
	}

	/**
	 * The Class AsSet.
	 *
	 * @param <E> the element type
	 */
	public static class AsSet<E> extends Collector<E, Set<E>> {

		/**
		 * Instantiates a new as set.
		 */
		protected AsSet() {}

		/**
		 * The Class Concurrent.
		 *
		 * @param <E> the element type
		 */
		public static class Concurrent<E> extends AsSet<E> {
			@Override
			protected void initCollect() {
				if (collect == null) { collect = Sets.newConcurrentHashSet(); }
			}

			@Override
			public boolean remove(final Object o) {
				if (o == null) return false;
				return super.remove(o);
			}

			@Override
			public boolean removeAll(final Collection<?> o) {
				if (o == null) return false;
				return super.removeAll(o);
			}
		}

		@Override
		protected void initCollect() {
			if (collect == null) { collect = new HashSet<>(); }
		}

		@Override
		public Set<E> items() {
			return collect == null ? Collections.EMPTY_SET : collect;
		}

	}

	/**
	 * The Class AsList.
	 *
	 * @param <E> the element type
	 */
	public static class AsList<E> extends Collector<E, IList<E>> {

		/**
		 * Instantiates a new as list.
		 */
		protected AsList() {}

		@Override
		protected void initCollect() {
			if (collect == null) { collect = GamaListFactory.create(); }
		}

		@Override
		public IList<E> items() {
			return collect == null ? GamaListFactory.EMPTY_LIST : collect;
		}

		/**
		 * Sets the size.
		 *
		 * @param size the new size
		 */
		public void setSize(final int size) {
			if (size > 0 && collect == null) { collect = GamaListFactory.create(Types.NO_TYPE, size); }

		}

		@Override
		public void shuffleInPlaceWith(final RandomUtils random) {
			random.shuffleInPlace(items());
		}
	}

	/**
	 * The Class AsOrderedSet.
	 *
	 * @param <E> the element type
	 */
	public static class AsOrderedSet<E> extends AsSet<E> {

		/**
		 * Instantiates a new as ordered set.
		 */
		protected AsOrderedSet() {}

		@Override
		protected void initCollect() {
			if (collect == null) { collect = new LinkedHashSet<>(); }
		}

	}

	@Override
	public int size() {
		if (collect == null) return 0;
		return collect.size();
	}

	@Override
	public boolean contains(final Object o) {
		if (collect == null) return false;
		return collect.contains(o);
	}

	@Override
	public Object[] toArray() {
		if (collect == null) return new Object[0];
		return collect.toArray();
	}

	@Override
	public <T> T[] toArray(final T[] a) {
		if (collect == null) return a;
		return collect.toArray(a);
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		if (collect == null) return false;
		return collect.containsAll(c);
	}

	@Override
	public boolean addAll(final Collection<? extends E> c) {
		initCollect();
		return collect.addAll(c);
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		if (collect == null) return false;
		return collect.removeAll(c);
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		if (collect == null) return false;
		return collect.retainAll(c);
	}

	/** The collect. */
	C collect;

	/*
	 * (non-Javadoc)
	 *
	 * @see msi.gama.util.ICollector#add(E)
	 */
	@Override
	public boolean add(final E vd) {
		initCollect();
		return collect.add(vd);
	}

	/**
	 * Inits the collect.
	 */
	protected abstract void initCollect();

	/*
	 * (non-Javadoc)
	 *
	 * @see msi.gama.util.ICollector#items()
	 */
	@Override
	public abstract C items();

	@Override
	public Iterator<E> iterator() {
		return items().iterator();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see msi.gama.util.ICollector#remove(E)
	 */
	@Override
	public boolean remove(final Object e) {
		if (collect == null) return false;
		return collect.remove(e);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see msi.gama.util.ICollector#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return collect == null || collect.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see msi.gama.util.ICollector#clear()
	 */
	@Override
	public void clear() {
		collect = null;
	}
}