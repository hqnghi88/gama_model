/*******************************************************************************************************
 *
 * GamlExpressionFactory.java, in msi.gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gaml.expressions;

import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.filter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import com.google.common.collect.Iterables;

import msi.gama.common.interfaces.IGamlIssue;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.runtime.IExecutionContext;
import msi.gama.util.IMap;
import msi.gaml.compilation.GAML;
import msi.gaml.descriptions.ActionDescription;
import msi.gaml.descriptions.IDescription;
import msi.gaml.descriptions.IExpressionDescription;
import msi.gaml.descriptions.OperatorProto;
import msi.gaml.descriptions.SpeciesDescription;
import msi.gaml.descriptions.StringBasedExpressionDescription;
import msi.gaml.expressions.data.ListExpression;
import msi.gaml.expressions.data.MapExpression;
import msi.gaml.expressions.operators.PrimitiveOperator;
import msi.gaml.expressions.types.SkillConstantExpression;
import msi.gaml.expressions.types.SpeciesConstantExpression;
import msi.gaml.expressions.types.TypeExpression;
import msi.gaml.expressions.units.UnitConstantExpression;
import msi.gaml.expressions.variables.AgentVariableExpression;
import msi.gaml.expressions.variables.EachExpression;
import msi.gaml.expressions.variables.GlobalVariableExpression;
import msi.gaml.expressions.variables.MyselfExpression;
import msi.gaml.expressions.variables.SelfExpression;
import msi.gaml.expressions.variables.SuperExpression;
import msi.gaml.expressions.variables.TempVariableExpression;
import msi.gaml.factories.DescriptionFactory;
import msi.gaml.statements.ActionStatement;
import msi.gaml.statements.Arguments;
import msi.gaml.types.IType;
import msi.gaml.types.Signature;
import msi.gaml.types.Types;

/**
 * The static class ExpressionFactory.
 *
 * @author drogoul
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamlExpressionFactory implements IExpressionFactory {

	/**
	 * The Interface ParserProvider.
	 */
	public interface ParserProvider {

		/**
		 * Gets the.
		 *
		 * @return the i expression compiler
		 */
		IExpressionCompiler get();
	}

	/** The parser. */
	static ThreadLocal<IExpressionCompiler> parser;

	/**
	 * Register parser provider.
	 *
	 * @param f
	 *            the f
	 */
	public static void registerParserProvider(final ParserProvider f) {
		parser = new ThreadLocal() {
			@Override
			protected IExpressionCompiler initialValue() {
				return f.get();
			}
		};
	}

	/**
	 * Gets the parser.
	 *
	 * @return the parser
	 */
	// @Override
	private IExpressionCompiler getParser() { return parser.get(); }

	@Override
	public void resetParser() {
		parser.get().dispose();
		parser.remove();
	}

	/**
	 * Method createUnit()
	 *
	 * @see msi.gaml.expressions.IExpressionFactory#createUnit(java.lang.Object, msi.gaml.types.IType, java.lang.String)
	 */
	@Override
	public UnitConstantExpression createUnit(final Object value, final IType t, final String name, final String doc,
			final String deprecated, final boolean isTime, final String[] names) {
		final UnitConstantExpression exp = UnitConstantExpression.create(value, t, name, doc, isTime, names);
		if (deprecated != null && !deprecated.isEmpty()) { exp.setDeprecated(deprecated); }
		return exp;

	}

	@Override
	public ConstantExpression createConst(final Object val, final IType type) {
		return createConst(val, type, null);
	}

	@Override
	public SpeciesConstantExpression createSpeciesConstant(final IType type) {
		if (type.getGamlType() != Types.SPECIES) return null;
		final SpeciesDescription sd = type.getContentType().getSpecies();
		if (sd == null) return null;
		return new SpeciesConstantExpression(sd.getName(), type);
	}

	@Override
	public ConstantExpression createConst(final Object val, final IType type, final String name) {
		if (type.getGamlType() == Types.SPECIES) return createSpeciesConstant(type);
		if (type == Types.SKILL) return new SkillConstantExpression((String) val, type);
		if (val == null) return NIL_EXPR;
		if (val instanceof Boolean) return (Boolean) val ? TRUE_EXPR : FALSE_EXPR;
		return new ConstantExpression(val, type, name);
	}

	@Override
	public UnitConstantExpression getUnitExpr(final String unit) {
		return GAML.UNITS.get(unit);
	}

	@Override
	public IExpression createExpr(final IExpressionDescription ied, final IDescription context) {
		return getParser().compile(ied, context);
	}

	@Override
	public IExpression createExpr(final String s, final IDescription context) {
		if (s == null || s.isEmpty()) return null;
		return getParser().compile(StringBasedExpressionDescription.create(s), context);
	}

	@Override
	public IExpression createExpr(final String s, final IDescription context,
			final IExecutionContext additionalContext) {
		if (s == null || s.isEmpty()) return null;
		return getParser().compile(s, context, additionalContext);
	}

	@Override
	public Arguments createArgumentMap(final ActionDescription action, final IExpressionDescription args,
			final IDescription context) {
		if (args == null) return null;
		return getParser().parseArguments(action, args.getTarget(), context, false);
	}

	@Override
	public IExpression createVar(final String name, final IType type, final boolean isConst, final int scope,
			final IDescription definitionDescription) {
		return switch (scope) {
			case IVarExpression.GLOBAL -> GlobalVariableExpression.create(name, type, isConst,
					definitionDescription.getModelDescription());
			case IVarExpression.AGENT -> new AgentVariableExpression(name, type, isConst, definitionDescription);
			case IVarExpression.TEMP -> /*
										 * TODO AD possibility to optimize the code if the variable is not changed
										 * anywhere in the code
										 */ new TempVariableExpression(name, type, definitionDescription);
			case IVarExpression.EACH -> new EachExpression(name, type);
			case IVarExpression.SELF -> new SelfExpression(type);
			case IVarExpression.SUPER -> new SuperExpression(type);
			case IVarExpression.MYSELF -> new MyselfExpression(type, definitionDescription);
			default -> null;
		};
	}

	@Override
	public IExpression createList(final Iterable<? extends IExpression> elements) {
		return ListExpression.create(elements);
	}

	/**
	 * Creates a new GamlExpression object.
	 *
	 * @param elements
	 *            the elements
	 * @return the i expression
	 */
	public IExpression createList(final IExpression[] elements) {
		return ListExpression.create(elements);
	}

	@Override
	public IExpression createMap(final Iterable<? extends IExpression> elements) {
		return MapExpression.create(elements);
	}

	@Override
	public boolean hasOperator(final String op, final IExpression... args) {
		// If arguments are invalid, we have no match
		if (args == null || args.length == 0) return false;
		for (final IExpression exp : args) { if (exp == null) return false; }
		// If the operator is not known, we have no match
		if (!GAML.OPERATORS.containsKey(op)) return false;
		return hasOperator(op, new Signature(args));
	}

	@Override
	public boolean hasExactOperator(final String op, final IExpression arg) {
		// If arguments are invalid, we have no match
		// If the operator is not known, we have no match
		if (arg == null || !GAML.OPERATORS.containsKey(op)) return false;
		Signature sig = new Signature(arg).simplified();
		return any(GAML.OPERATORS.get(op).keySet(), si -> sig.equals(si));
	}

	/**
	 * Checks for operator.
	 *
	 * @param op
	 *            the op
	 * @param sig
	 *            the sig
	 * @return true, if successful
	 */
	@Override
	public boolean hasOperator(final String op, final Signature s) {
		// If arguments are invalid, we have no match
		// If the operator is not known, we have no match
		if (s == null || s.size() == 0 || !GAML.OPERATORS.containsKey(op)) return false;
		final IMap<Signature, OperatorProto> ops = GAML.OPERATORS.get(op);
		Signature sig = s.simplified();
		// Does any known operator signature match with the signatue of the expressions ?
		boolean matches = any(ops.keySet(), si -> sig.matchesDesiredSignature(si));
		if (!matches) {
			// Check if a varArg is not a possibility
			matches = any(ops.keySet(), si -> Signature.varArgFrom(sig).matchesDesiredSignature(si));
		}
		return matches;
	}

	@Override
	public IExpression createOperator(final String op, final IDescription context, final EObject eObject,
			final IExpression... args) {

		if (!hasOperator(op, args)) {
			final IMap<Signature, OperatorProto> ops = GAML.OPERATORS.get(op);
			final Signature userSignature = new Signature(args).simplified();
			StringBuilder msg = new StringBuilder("No operator found for applying '").append(op).append("' to ")
					.append(userSignature);
			if (ops != null) {
				msg.append(" (operators available for ").append(Arrays.toString(ops.keySet().toArray())).append(")");
			}
			context.error(msg.toString(), IGamlIssue.UNMATCHED_OPERANDS, eObject);
			return null;
		}
		// We get the possible sets of types registered in OPERATORS
		final IMap<Signature, OperatorProto> ops = GAML.OPERATORS.get(op);
		// We create the signature corresponding to the arguments
		// 19/02/14 Only the simplified signature is used now
		Signature userSignature = new Signature(args).simplified();
		final Signature originalUserSignature = userSignature;
		// If the signature is not present in the registry
		if (!ops.containsKey(userSignature)) {
			final Signature[] matching = Iterables.toArray(
					filter(ops.keySet(), s -> originalUserSignature.matchesDesiredSignature(s)), Signature.class);
			final int size = matching.length;
			if (size == 0)
				// It is a varArg, we call recursively the method
				return createOperator(op, context, eObject, createList(args));
			if (size == 1) {
				// Only one choice
				userSignature = matching[0];
			} else {
				// Several choices, we take the closest
				int distance = Integer.MAX_VALUE;
				for (final Signature s : matching) {
					final int dist = s.distanceTo(originalUserSignature);
					if (dist == 0) {
						userSignature = s;
						break;
					}
					if (dist < distance) {
						distance = dist;
						userSignature = s;
					}
				}
			}

			// We coerce the types if necessary, by wrapping the original
			// expressions in a casting expression
			final IType[] coercingTypes = userSignature.coerce(originalUserSignature, context);

			for (int i = 0; i < coercingTypes.length; i++) {
				final IType t = coercingTypes[i];
				if (t != null) {
					// Emits an info when a float is truncated. See Issue 735.
					if (t.id() == IType.INT) {
						context.info("'" + args[i].serialize(false) + "' will be  truncated to int.",
								IGamlIssue.UNMATCHED_OPERANDS, eObject);
					}
					args[i] = createAs(context, args[i], createTypeExpression(t));
				}
			}
		}

		final OperatorProto proto = ops.get(userSignature);
		return createDirectly(context, eObject, proto, args);
	}

	@Override
	public IExpression createAs(final IDescription context, final IExpression toCast, final IExpression type) {
		return OperatorProto.AS.create(context, null, toCast, type);
	}

	/**
	 * Creates a new GamlExpression object.
	 *
	 * @param context
	 *            the context
	 * @param eObject
	 *            the e object
	 * @param proto
	 *            the proto
	 * @param args
	 *            the args
	 * @return the i expression
	 */
	private IExpression createDirectly(final IDescription context, final EObject eObject, final OperatorProto proto,
			final IExpression... args) {
		// We finally make an instance of the operator and init it with the arguments
		final IExpression copy = proto.create(context, eObject, args);
		if (copy != null) {
			// We verify that it is not deprecated
			final String ged = proto.getDeprecated();
			if (ged != null) {
				context.warning(proto.getName() + " is deprecated: " + ged, IGamlIssue.DEPRECATED, eObject);
			}
		}
		return copy;
	}

	@Override
	public IExpression createAction(final String op, final IDescription callerContext, final ActionDescription action,
			final IExpression call, final Arguments arguments) {
		if (action.verifyArgs(callerContext, arguments))
			return new PrimitiveOperator(callerContext, action, call, arguments, call instanceof SuperExpression);
		return null;
	}

	/**
	 * Method createCastingExpression()
	 *
	 * @see msi.gaml.expressions.IExpressionFactory#createCastingExpression(msi.gaml.types.IType)
	 */
	@Override
	public IExpression createTypeExpression(final IType type) {
		return new TypeExpression(type);
	}

	@Override
	public IExpression createTemporaryActionForAgent(final IAgent agent, final String action,
			final IExecutionContext tempContext) {
		final SpeciesDescription context = agent.getSpecies().getDescription();
		final ActionDescription desc = (ActionDescription) DescriptionFactory.create(IKeyword.ACTION, context,
				Collections.EMPTY_LIST, IKeyword.TYPE, IKeyword.UNKNOWN, IKeyword.NAME, TEMPORARY_ACTION_NAME);
		final List<IDescription> children = getParser().compileBlock(action, context, tempContext);
		for (final IDescription child : children) { desc.addChild(child); }
		desc.validate();
		context.addChild(desc);
		final ActionStatement a = (ActionStatement) desc.compile();
		agent.getSpecies().addTemporaryAction(a);
		return getParser().compile(TEMPORARY_ACTION_NAME + "()", context, null);
	}

}
