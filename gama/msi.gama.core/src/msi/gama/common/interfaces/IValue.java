/*******************************************************************************************************
 *
 * IValue.java, in msi.gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2.0.0).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama2 for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gama.common.interfaces;

import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;

/**
 * Represents a 'value' in GAML (a Java object that can provide a GAML type, be serializable into a GAML expression, and
 * be copied
 *
 * @author drogoul
 * @since 19 nov. 2008
 *
 */
public interface IValue extends IGamlable, ITyped {

	/**
	 * Returns the string 'value' of this value.
	 *
	 * @param scope
	 *            the current GAMA scope
	 * @return a string representing this value (not necessarily its serialization in GAML)
	 * @throws GamaRuntimeException
	 */
	String stringValue(IScope scope) throws GamaRuntimeException;

	/**
	 * Int value.
	 *
	 * @param scope
	 *            the scope
	 * @return the int
	 */
	default int intValue(final IScope scope) {
		return 0;
	}

	/**
	 * Float value.
	 *
	 * @param scope
	 *            the scope
	 * @return the double
	 */
	default double floatValue(final IScope scope) {
		return intValue(scope);
	}

	/**
	 * Returns a copy of this value
	 *
	 * @param scope
	 *            the current GAMA scope
	 * @return a copy of this value. The definition of copy (whether shallow or deep, etc.) depends on the subclasses
	 * @throws GamaRuntimeException
	 */
	IValue copy(IScope scope) throws GamaRuntimeException;

}
