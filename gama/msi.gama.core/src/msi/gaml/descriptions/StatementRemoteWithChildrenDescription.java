/*******************************************************************************************************
 *
 * StatementRemoteWithChildrenDescription.java, in msi.gama.core, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package msi.gaml.descriptions;

import org.eclipse.emf.ecore.EObject;

import com.google.common.collect.Iterables;

import msi.gaml.compilation.ISymbol;
import msi.gaml.statements.Arguments;
import msi.gaml.statements.Facets;
import msi.gaml.types.IType;

/**
 * The Class StatementRemoteWithChildrenDescription.
 */
@SuppressWarnings ({ "rawtypes" })
public class StatementRemoteWithChildrenDescription extends StatementWithChildrenDescription {

	/** The previous description. */
	protected IDescription previousDescription;

	/**
	 * Instantiates a new statement remote with children description.
	 *
	 * @param keyword the keyword
	 * @param superDesc the super desc
	 * @param cp the cp
	 * @param hasArgs the has args
	 * @param source the source
	 * @param facets the facets
	 * @param alreadyComputedArgs the already computed args
	 */
	public StatementRemoteWithChildrenDescription(final String keyword, final IDescription superDesc,
			final Iterable<IDescription> cp, final boolean hasArgs, final EObject source, final Facets facets,
			final Arguments alreadyComputedArgs) {
		super(keyword, superDesc, cp, hasArgs, source, facets, alreadyComputedArgs);
	}

	@Override
	public boolean isDocumenting() {
		return super.isDocumenting() || previousDescription != null && previousDescription.isDocumenting();
	}

	@Override
	public void dispose() {
		super.dispose();
		previousDescription = null;
	}

	@Override
	public boolean validateChildren() {
		IDescription previousEnclosingDescription = null;
		try {
			previousEnclosingDescription = pushRemoteContext();
			return super.validateChildren();
		} finally {
			popRemoteContext(previousEnclosingDescription);
		}
	}

	@Override
	public Iterable<? extends ISymbol> compileChildren() {

		final SpeciesDescription sd = getGamlType().getDenotedSpecies();
		if (sd != null) {
			final IType t = getSpeciesContext().getGamlType();
			addTemp(this, MYSELF, t);
			setEnclosingDescription(sd);
		}

		return super.compileChildren();
	}

	@Override
	public StatementRemoteWithChildrenDescription copy(final IDescription into) {
		final Iterable<IDescription> children = Iterables.transform(this.children, each -> each.copy(into));
		final StatementRemoteWithChildrenDescription desc = new StatementRemoteWithChildrenDescription(getKeyword(),
				into, children, false, element, getFacetsCopy(), passedArgs == null ? null : passedArgs.cleanCopy());
		desc.originName = getOriginName();
		return desc;
	}

	@Override
	public void setEnclosingDescription(final IDescription desc) {
		previousDescription = getEnclosingDescription();
		super.setEnclosingDescription(desc);
	}

	@Override
	public ModelDescription getModelDescription() {
		ModelDescription result = super.getModelDescription();
		if (result == null && previousDescription != null) {
			result = previousDescription.getModelDescription();
		}
		return result;
	}

	@Override
	public IVarDescriptionProvider getDescriptionDeclaringVar(final String name) {
		IVarDescriptionProvider result = super.getDescriptionDeclaringVar(name);
		if (result == null && previousDescription != null) {
			result = previousDescription.getDescriptionDeclaringVar(name);
		}
		return result;
	}

	@Override
	public IDescription getDescriptionDeclaringAction(final String name, final boolean superInvocation) {
		IDescription result = super.getDescriptionDeclaringAction(name, superInvocation);
		if (result == null && previousDescription != null) {
			result = previousDescription.getDescriptionDeclaringAction(name, superInvocation);
		}
		return result;
	}

	/**
	 * Push remote context.
	 *
	 * @return the i description
	 */
	public IDescription pushRemoteContext() {
		final SpeciesDescription denotedSpecies = getGamlType().getDenotedSpecies();
		IDescription previousEnclosingDescription = null;
		if (denotedSpecies != null) {
			final SpeciesDescription s = getSpeciesContext();
			if (s != null) {
				final IType t = s.getGamlType();
				addTemp(this, MYSELF, t);
				previousEnclosingDescription = getEnclosingDescription();
				setEnclosingDescription(denotedSpecies);

				// FIXME ===> Model Description is lost if we are
				// dealing
				// with a built-in species !
			}
		}
		return previousEnclosingDescription;
	}

	/**
	 * Pop remote context.
	 *
	 * @param previousEnclosingDescription the previous enclosing description
	 */
	public void popRemoteContext(final IDescription previousEnclosingDescription) {

		if (previousEnclosingDescription != null) {
			setEnclosingDescription(previousEnclosingDescription);
		}

	}

}
