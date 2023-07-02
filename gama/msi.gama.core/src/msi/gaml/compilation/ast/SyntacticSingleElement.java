/*******************************************************************************************************
 *
 * SyntacticSingleElement.java, in msi.gama.core, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package msi.gaml.compilation.ast;

import org.eclipse.emf.ecore.EObject;

import msi.gaml.statements.Facets;

/**
 * The class SyntacticSingleElement.
 *
 * @author drogoul
 * @since 5 f�vr. 2012
 * @modified 9 sept. 2013
 *
 */
public class SyntacticSingleElement extends AbstractSyntacticElement {

	/**
	 * Instantiates a new syntactic single element.
	 *
	 * @param keyword the keyword
	 * @param facets the facets
	 * @param statement the statement
	 */
	SyntacticSingleElement(final String keyword, final Facets facets, final EObject statement) {
		super(keyword, facets, statement);
	}

	/* (non-Javadoc)
	 * @see msi.gaml.compilation.ast.ISyntacticElement#hasChildren()
	 */
	@Override
	public boolean hasChildren() {
		return false;
	}

}
