/*******************************************************************************************************
 *
 * StatementWithChildrenDescription.java, in msi.gama.core, is part of the source code of the GAMA modeling and
 * simulation platform (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gaml.descriptions;

import static msi.gaml.compilation.GAML.getExpressionFactory;

import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import com.google.common.collect.Iterables;

import msi.gama.common.interfaces.IGamlIssue;
import msi.gama.util.Collector;
import msi.gama.util.GamaMapFactory;
import msi.gama.util.IMap;
import msi.gaml.expressions.IExpression;
import msi.gaml.expressions.IVarExpression;
import msi.gaml.statements.Arguments;
import msi.gaml.statements.Facets;
import msi.gaml.types.IType;

/**
 * The Class StatementWithChildrenDescription.
 */
public class StatementWithChildrenDescription extends StatementDescription {

	/** The temps. */
	protected IMap<String, IVarExpression> temps;

	/** The children. */
	protected final Collector.AsList<IDescription> children = Collector.getList();

	/**
	 * Instantiates a new statement with children description.
	 *
	 * @param keyword
	 *            the keyword
	 * @param superDesc
	 *            the super desc
	 * @param cp
	 *            the cp
	 * @param hasArgs
	 *            the has args
	 * @param source
	 *            the source
	 * @param facets
	 *            the facets
	 * @param alreadyComputedArgs
	 *            the already computed args
	 */
	public StatementWithChildrenDescription(final String keyword, final IDescription superDesc,
			final Iterable<IDescription> cp, final boolean hasArgs, final EObject source, final Facets facets,
			final Arguments alreadyComputedArgs) {
		super(keyword, superDesc, hasArgs, /* cp, */source, facets, alreadyComputedArgs);
		setIf(Flag.Breakable, getMeta().isBreakable());
		setIf(Flag.Continuable, getMeta().isContinuable());
		addChildren(cp);
	}

	@Override
	public boolean visitChildren(final DescriptionVisitor<IDescription> visitor) {
		for (final IDescription d : children) { if (!visitor.process(d)) return false; }
		return true;
	}

	@Override
	public boolean visitOwnChildrenRecursively(final DescriptionVisitor<IDescription> visitor) {
		for (final IDescription d : children) {
			if (!visitor.process(d) || !d.visitOwnChildrenRecursively(visitor)) return false;
		}
		return true;

	}

	@Override
	public boolean visitOwnChildren(final DescriptionVisitor<IDescription> visitor) {
		for (final IDescription d : children) { if (!visitor.process(d)) return false; }
		return true;
	}

	@Override
	public Iterable<IDescription> getOwnChildren() { return children; }

	@Override
	public void dispose() {
		super.dispose();
		Collector.release(children);
		if (temps != null) {
			temps.forEachValue(object -> {
				object.dispose();
				return true;
			});
		}
		temps = null;
	}

	@Override
	public boolean hasAttribute(final String name) {
		return temps != null && temps.containsKey(name);
	}

	@Override
	public IExpression getVarExpr(final String name, final boolean asField) {
		if (temps != null) return temps.get(name);
		return null;
	}

	/**
	 * Checks for temps.
	 *
	 * @return true, if successful
	 */
	public boolean hasTemps() {
		return getMeta().hasScope() /* canHaveTemps */ && temps != null;
	}

	/**
	 * Adds the temp.
	 *
	 * @param declaration
	 *            the declaration
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @return the i expression
	 */
	public IExpression addTemp(final IDescription declaration, final String name, final IType<?> type) {
		// TODO Should separate validation from execution, here.

		if (!getMeta().hasScope() /* canHaveTemps */) {
			if (getEnclosingDescription() == null
					|| !(getEnclosingDescription() instanceof StatementWithChildrenDescription))
				return null;

			return ((StatementWithChildrenDescription) getEnclosingDescription()).addTemp(declaration, name, type);
		}
		final String kw = getKeyword();
		final String facet = LET.equals(kw) || LOOP.equals(kw) ? NAME : RETURNS;
		if (temps == null) { temps = GamaMapFactory.create(); }
		if (!MYSELF.equals(name)) {
			IVarDescriptionProvider description = getDescriptionDeclaringVar(name);
			if (description != null) {
				if (description instanceof SpeciesDescription) {
					declaration.warning("This declaration of " + name + " shadows the declaration of an attribute of "
							+ ((SpeciesDescription) description).getName(), IGamlIssue.SHADOWS_NAME, facet);
				} else if (description instanceof ModelDescription) {
					declaration.warning(
							"This declaration of " + name + " shadows the declaration of a global attribute",
							IGamlIssue.SHADOWS_NAME, facet);
				} else {
					declaration.warning("This declaration of " + name + " shadows a previous declaration",
							IGamlIssue.SHADOWS_NAME, facet);
				}
			}
		}

		// if (temps.containsKey(name) && !name.equals(MYSELF)) {
		// declaration.warning("This declaration of " + name + " shadows a previous declaration",
		// IGamlIssue.SHADOWS_NAME, facet);
		// }
		// final SpeciesDescription sd = declaration.getSpeciesContext();
		// final ModelDescription md = declaration.getModelDescription();
		// if (sd != null && sd != md && sd.hasAttribute(name)) {
		// declaration.warning(
		// "This declaration of " + name + " shadows the declaration of an attribute of " + sd.getName(),
		// IGamlIssue.SHADOWS_NAME, facet);
		// }
		// if (md != null && md.hasAttribute(name)) {
		// declaration.warning("This declaration of " + name + " shadows the declaration of a global attribute",
		// IGamlIssue.SHADOWS_NAME, facet);
		// }
		final IExpression result =
				MYSELF.equals(name) ? getExpressionFactory().createVar(name, type, false, IVarExpression.MYSELF, this)
						: getExpressionFactory().createVar(name, type, false, IVarExpression.TEMP, this);
		temps.put(name, (IVarExpression) result);
		return result;
	}

	@Override
	public IDescription addChild(final IDescription child) {
		final IDescription d = super.addChild(child);
		if (d != null) { children.add(child); }
		return d;
	}

	@Override
	public StatementWithChildrenDescription copy(final IDescription into) {
		final Iterable<IDescription> children = Iterables.transform(this.children, each -> each.copy(into));
		final StatementWithChildrenDescription desc = new StatementWithChildrenDescription(getKeyword(), into, children,
				false, element, getFacetsCopy(), passedArgs == null ? null : passedArgs.cleanCopy());
		desc.originName = getOriginName();
		return desc;
	}

	@Override
	public void replaceChildrenWith(final Iterable<IDescription> array) {
		final List<IDescription> descs = Arrays.asList(Iterables.toArray(array, IDescription.class));
		children.clear();
		children.addAll(descs);
	}

	/**
	 * @return
	 */
	public boolean isBreakable() { return isSet(Flag.Breakable); }

	@Override
	public IVarExpression addNewTempIfNecessary(final String facetName, final IType type) {
		if (LOOP.equals(getKeyword()) && NAME.equals(facetName)) // Case of loops: the variable is inside the loop (not
																	// outside)
			return (IVarExpression) addTemp(this, getLitteral(facetName), type);
		return super.addNewTempIfNecessary(facetName, type);
	}

	/**
	 * Checks if is continuable.
	 *
	 * @return true, if is continuable
	 */
	public boolean isContinuable() { return isSet(Flag.Continuable); }

}