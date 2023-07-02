/*******************************************************************************************************
 *
 * StatementDescription.java, in msi.gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gaml.descriptions;

import static java.util.Collections.EMPTY_LIST;
import static msi.gama.common.interfaces.IGamlIssue.GENERAL;
import static msi.gama.common.interfaces.IGamlIssue.MISSING_NAME;
import static msi.gama.common.interfaces.IGamlIssue.SHOULD_CAST;
import static msi.gama.common.interfaces.IGamlIssue.UNKNOWN_ARGUMENT;
import static msi.gaml.statements.DoStatement.DO_FACETS;
import static msi.gaml.types.Types.NO_TYPE;

import org.eclipse.emf.ecore.EObject;

import msi.gaml.compilation.GAML;
import msi.gaml.expressions.IExpression;
import msi.gaml.expressions.IVarExpression;
import msi.gaml.expressions.operators.IOperator;
import msi.gaml.statements.Arguments;
import msi.gaml.statements.Facets;
import msi.gaml.types.GamaType;
import msi.gaml.types.IType;
import msi.gaml.types.Types;
import ummisco.gama.dev.utils.COUNTER;

/**
 * Written by drogoul Modified on 10 févr. 2010
 *
 * @todo Description
 *
 */
@SuppressWarnings ({ "rawtypes" })
public class StatementDescription extends SymbolDescription {

	/** The passed args. */
	// Corresponds to the "with" facet
	protected final Arguments passedArgs;

	/**
	 * Instantiates a new statement description.
	 *
	 * @param keyword
	 *            the keyword
	 * @param superDesc
	 *            the super desc
	 * @param hasArgs
	 *            the has args
	 * @param source
	 *            the source
	 * @param facets
	 *            the facets
	 * @param alreadyComputedArgs
	 *            the already computed args
	 */
	public StatementDescription(final String keyword, final IDescription superDesc, final boolean hasArgs,
			final EObject source, final Facets facets, final Arguments alreadyComputedArgs) {
		super(keyword, superDesc, source, /* children, */ facets);
		setIf(Flag.IsInvocation, DO.equals(keyword) || INVOKE.equals(keyword));
		setIf(Flag.IsSuperInvocation, INVOKE.equals(keyword));
		passedArgs = alreadyComputedArgs != null ? alreadyComputedArgs : hasArgs ? createArgs() : null;
	}

	@Override
	protected SymbolSerializer<? extends SymbolDescription> createSerializer() {
		return STATEMENT_SERIALIZER;
	}

	@Override
	public void dispose() {
		if (isBuiltIn()) return;
		super.dispose();
		if (passedArgs != null) { passedArgs.dispose(); }
	}

	/**
	 * Creates the args.
	 *
	 * @return the arguments
	 */
	private Arguments createArgs() {
		if (!hasFacets()) return null;
		if (hasFacet(WITH)) {
			try {
				return GAML.getExpressionFactory().createArgumentMap(getAction(), getFacet(WITH), this);
			} finally {
				removeFacets(WITH);
			}
		}
		if (!isInvocation() || !hasFacetsNotIn(DO_FACETS)) return null;
		final Arguments args = new Arguments();
		visitFacets((facet, b) -> {
			if (!DO_FACETS.contains(facet)) { args.put(facet, b); }
			return true;
		});
		return args;

	}

	/**
	 * Checks if is super invocation.
	 *
	 * @return true, if is super invocation
	 */
	public boolean isSuperInvocation() { return isSet(Flag.IsSuperInvocation); }

	/**
	 * Checks if is invocation.
	 *
	 * @return true, if is invocation
	 */
	private boolean isInvocation() { return isSet(Flag.IsInvocation); }

	/**
	 * Gets the action.
	 *
	 * @return the action
	 */
	private ActionDescription getAction() {
		final String actionName = getLitteral(ACTION);
		if (actionName == null) return null;
		final TypeDescription declPlace =
				(TypeDescription) getDescriptionDeclaringAction(actionName, isSuperInvocation());
		ActionDescription executer = null;
		if (declPlace != null) { executer = declPlace.getAction(actionName); }
		return executer;
	}

	@Override
	public StatementDescription copy(final IDescription into) {
		final StatementDescription desc = new StatementDescription(getKeyword(), into, false, /* null, */ element,
				getFacetsCopy(), passedArgs == null ? null : passedArgs.cleanCopy());
		desc.originName = getOriginName();
		return desc;
	}

	@Override
	public boolean manipulatesVar(final String nm) {
		if (EQUATION.equals(getKeyword())) {
			final Iterable<IDescription> equations = getChildrenWithKeyword(EQUATION_OP);
			for (final IDescription equation : equations) {
				final IExpressionDescription desc = equation.getFacet(EQUATION_LEFT);
				desc.compile(equation);
				final IExpression exp = desc.getExpression();
				if (exp instanceof IOperator op
						&& (op.arg(0).getName().equals(nm) || op.arg(1) != null && op.arg(1).getName().equals(nm)))
					return true;
			}
		}
		return false;
	}

	/**
	 * Verify args.
	 *
	 * @param args
	 *            the args
	 * @return true, if successful
	 */
	public boolean verifyArgs(final Arguments args) {
		final ActionDescription executer = getAction();
		if (executer == null) return false;
		return executer.verifyArgs(this, args);
	}

	/**
	 * Gets the formal args.
	 *
	 * @return the formal args
	 */
	public Iterable<IDescription> getFormalArgs() { return getChildrenWithKeyword(ARG); }

	/**
	 * Gets the passed args.
	 *
	 * @return the passed args
	 */
	public Facets getPassedArgs() { return passedArgs == null ? Facets.NULL : passedArgs; }

	@Override
	public String getName() {
		String s = super.getName();
		if (s == null) {
			// Special case for aspects
			if (ASPECT.equals(getKeyword())) {
				s = DEFAULT;
			} else {
				if (REFLEX.equals(getKeyword())) {
					warning("Reflexes should be named", MISSING_NAME, getUnderlyingElement());
				}
				s = INTERNAL + getKeyword() + String.valueOf(COUNTER.GET_UNIQUE());
			}
			setName(s);
		}
		return s;
	}

	@Override
	public String toString() {
		return getKeyword() + " " + getName();
	}

	@Override
	public String getTitle() {
		final String kw = getKeyword();
		String nm = getName();
		if (nm.contains(INTERNAL)) {
			nm = getLitteral(ACTION);
			if (nm == null) { nm = "statement"; }
		}
		String in = "";
		if (getMeta().isTopLevel()) {
			final IDescription d = getEnclosingDescription();
			if (d == null) {
				in = " defined in " + getOriginName();
			} else {
				in = " of " + d.getTitle();
			}
		}
		return kw + " " + nm + " " + in;
	}

	@Override
	public IDescription validate() {
		if (isSet(Flag.Validated)) return this;
		final IDescription result = super.validate();
		if (passedArgs != null) { validatePassedArgs(); }
		return result;
	}

	/**
	 * Validate passed args.
	 *
	 * @return the arguments
	 */
	public Arguments validatePassedArgs() {
		final IDescription superDesc = getEnclosingDescription();
		passedArgs.forEachFacet((nm, exp) -> {
			if (exp != null) { exp.compile(superDesc); }
			return true;
		});
		if (isInvocation()) {
			verifyArgs(passedArgs);
		} else if (CREATE.equals(keyword)) { verifyInits(passedArgs); }
		return passedArgs;
	}

	/**
	 * Verify inits.
	 *
	 * @param ca
	 *            the ca
	 */
	private void verifyInits(final Arguments ca) {
		final SpeciesDescription denotedSpecies = getGamlType().getDenotedSpecies();
		if (denotedSpecies == null) {
			if (!ca.isEmpty()) {
				warning("Impossible to verify the validity of the arguments. Use them at your own risk.",
						UNKNOWN_ARGUMENT);
			}
			return;
		}
		ca.forEachFacet((nm, exp) -> {
			// hqnghi check attribute is not exist in both main model and
			// micro-model
			if (!denotedSpecies.hasAttribute(nm) && denotedSpecies instanceof ExperimentDescription
					&& !denotedSpecies.getModelDescription().hasAttribute(nm)) {
				// end-hqnghi
				error("Attribute " + nm + " does not exist in species " + denotedSpecies.getName(), UNKNOWN_ARGUMENT,
						exp.getTarget(), (String[]) null);
				return false;
			}
			IType<?> initType = NO_TYPE;
			IType<?> varType = NO_TYPE;
			final VariableDescription vd = denotedSpecies.getAttribute(nm);
			if (vd != null) { varType = vd.getGamlType(); }
			if (exp != null) {
				final IExpression expr = exp.getExpression();
				if (expr != null) { initType = expr.getGamlType(); }
				if (varType != NO_TYPE && !initType.isTranslatableInto(varType)) {
					if (CREATE.equals(getKeyword())) {
						final boolean isDB = getFacet(FROM) != null
								&& getFacet(FROM).getExpression().getGamlType().isAssignableFrom(Types.LIST);
						if (isDB && initType.equals(Types.STRING)) return true;
					}
					warning("The type of attribute " + nm + " should be " + varType, SHOULD_CAST, exp.getTarget(),
							varType.toString());
				}
			}

			return true;
		});

	}

	@Override
	protected IExpression createVarWithTypes(final String tag) {

		compileTypeProviderFacets();

		// Definition of the type
		IType t = super.getGamlType();
		final String kw = getKeyword();
		IType ct = t.getContentType();
		if (CREATE.equals(kw) || CAPTURE.equals(kw) || RELEASE.equals(kw)) {
			ct = t;
			t = Types.LIST;

		} else if (t == NO_TYPE) {
			if (hasFacet(VALUE)) {
				final IExpression value = getFacetExpr(VALUE);
				if (value != null) { t = value.getGamlType(); }
			} else if (hasFacet(OVER)) {
				final IExpression expr = getFacetExpr(OVER);
				if (expr != null) {
					// If of type pair, find the common supertype of key and contents
					if (Types.PAIR.isAssignableFrom(expr.getGamlType())) {
						t = GamaType.findCommonType(expr.getGamlType().getContentType(),
								expr.getGamlType().getKeyType());
					} else {
						t = expr.getGamlType().getContentType();
					}
				}
			} else if (hasFacet(FROM) && hasFacet(TO)) {
				final IExpression expr = getFacetExpr(FROM);
				if (expr != null) { t = expr.getGamlType(); }
			}
		}

		IType kt = t.getKeyType();
		// Definition of the content type and key type
		if (hasFacet(AS)) {
			ct = getTypeDenotedByFacet(AS);
		} else if (hasFacet(SPECIES)) {
			final IExpression expr = getFacetExpr(SPECIES);
			if (expr != null) {
				ct = expr.getGamlType().getContentType();
				kt = expr.getGamlType().getKeyType();
			}
		}

		return addNewTempIfNecessary(tag, GamaType.from(t, kt, ct));

	}

	/**
	 * Adds the new temp if necessary.
	 *
	 * @param facetName
	 *            the facet name
	 * @param type
	 *            the type
	 * @return the i var expression
	 */
	public IVarExpression addNewTempIfNecessary(final String facetName, final IType type) {
		final String varName = getLitteral(facetName);
		final IDescription sup = getEnclosingDescription();
		if (!(sup instanceof StatementWithChildrenDescription)) {
			error("Impossible to return " + varName, GENERAL);
			return null;
		}
		return (IVarExpression) ((StatementWithChildrenDescription) sup).addTemp(this, varName, type);
	}

	@Override
	public boolean visitChildren(final DescriptionVisitor<IDescription> visitor) {
		return true;
	}

	@Override
	public boolean visitOwnChildren(final DescriptionVisitor<IDescription> visitor) {
		return true;
	}

	@Override
	public Iterable<IDescription> getOwnChildren() { return EMPTY_LIST; }

	/**
	 * Creates the compiled args.
	 *
	 * @return the arguments
	 */
	public Arguments createCompiledArgs() {
		return passedArgs;
	}

}
