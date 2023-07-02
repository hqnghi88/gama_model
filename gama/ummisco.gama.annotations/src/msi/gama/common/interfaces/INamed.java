/*******************************************************************************************************
 *
 * INamed.java, in ummisco.gama.annotations, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package msi.gama.common.interfaces;

/**
 * Interface INamed. Represents objects that can be provided with a name.
 * 
 * @author A. Drogoul
 * @since 10 nov. 2009
 */
public interface INamed extends IGamlable {

	/** The comparator. */
	public static java.util.Comparator<? super INamed> COMPARATOR =
			(a, b) -> a.getName().compareToIgnoreCase(b.getName());

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	default String getName() {
		return toString();
	}

	/**
	 * Sets the name.
	 *
	 * @param newName the new name
	 */
	default void setName(final String newName) {}

}
