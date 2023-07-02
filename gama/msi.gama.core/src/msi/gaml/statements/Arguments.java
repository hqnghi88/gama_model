/*******************************************************************************************************
 *
 * Arguments.java, in msi.gama.core, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package msi.gaml.statements;

import java.util.Map;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.runtime.IScope;
import msi.gaml.descriptions.ConstantExpressionDescription;
import msi.gaml.expressions.IExpression;

/**
 * @author drogoul
 */
public class Arguments extends Facets {

	/** The caller. */
	/*
	 * The caller represents the agent in the context of which the arguments need to be evaluated.
	 */
	ThreadLocal<IAgent> caller = new ThreadLocal<>();

	/**
	 * Instantiates a new arguments.
	 *
	 * @param args the args
	 */
	public Arguments(final Arguments args) {
		super(args);
		if (args != null) { setCaller(args.caller.get()); }
	}

	/**
	 * Instantiates a new arguments.
	 *
	 * @param caller the caller
	 * @param args the args
	 */
	/*
	 * A constructor that takes a caller and arguments defined as a map <string, values>. Values are then transformed
	 * into a constant expression
	 */
	public Arguments(final IAgent caller, final Map<String, Object> args) {
		setCaller(caller);
		args.forEach((k, v) -> put(k, ConstantExpressionDescription.create(v)));
	}

	/**
	 * Instantiates a new arguments.
	 */
	public Arguments() {}

	@Override
	public Arguments cleanCopy() {
		final Arguments result = new Arguments();
		result.setCaller(caller.get());
		for (final Facet f : facets) {
			result.facets.add(f.cleanCopy());
		}
		return result;
	}

	/**
	 * Resolve against.
	 *
	 * @param scope the scope
	 * @return the arguments
	 */
	public Arguments resolveAgainst(final IScope scope) {
		final Arguments result = new Arguments();
		result.setCaller(caller.get());
		for (final Facet f : facets) {
			final IExpression exp = getExpr(f.key);
			if (exp != null) { result.put(f.key, exp.resolveAgainst(scope)); }
		}
		return result;
	}

	/**
	 * Sets the caller.
	 *
	 * @param caller the new caller
	 */
	public void setCaller(final IAgent caller) {
		this.caller.set(caller);
	}

	/**
	 * Gets the caller.
	 *
	 * @return the caller
	 */
	public IAgent getCaller() {
		return caller.get();
	}

	@Override
	public void dispose() {
		super.dispose();
		caller.set(null);
	}

}
