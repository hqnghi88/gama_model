/*******************************************************************************************************
 *
 * FsmStateStatement.java, in msi.gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gaml.architecture.finite_state_machine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.FluentIterable;

import msi.gama.common.interfaces.IGamlIssue;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.facet;
import msi.gama.precompiler.GamlAnnotations.facets;
import msi.gama.precompiler.GamlAnnotations.inside;
import msi.gama.precompiler.GamlAnnotations.symbol;
import msi.gama.precompiler.GamlAnnotations.usage;
import msi.gama.precompiler.ISymbolKind;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaMapFactory;
import msi.gama.util.IMap;
import msi.gaml.architecture.finite_state_machine.FsmStateStatement.StateValidator;
import msi.gaml.compilation.IDescriptionValidator;
import msi.gaml.compilation.ISymbol;
import msi.gaml.compilation.annotations.validator;
import msi.gaml.descriptions.IDescription;
import msi.gaml.descriptions.SkillDescription;
import msi.gaml.descriptions.SpeciesDescription;
import msi.gaml.expressions.IExpression;
import msi.gaml.expressions.IExpressionFactory;
import msi.gaml.operators.Cast;
import msi.gaml.statements.AbstractStatementSequence;
import msi.gaml.types.IType;

/**
 * The Class FsmStateStatement.
 *
 * @author drogoul
 */

@symbol (
		name = FsmStateStatement.STATE,
		kind = ISymbolKind.BEHAVIOR,
		with_sequence = true,
		unique_name = true)
@inside (
		symbols = IKeyword.FSM,
		kinds = { ISymbolKind.SPECIES, ISymbolKind.EXPERIMENT, ISymbolKind.MODEL })
@facets (
		value = { @facet (
				name = FsmStateStatement.INITIAL,
				type = IType.BOOL,
				optional = true,
				doc = @doc ("specifies whether the state is the initial one (default value = false)")),
				@facet (
						name = FsmStateStatement.FINAL,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("specifies whether the state is a final one (i.e. there is no transition from this state to another state) (default value= false)")),
				@facet (
						name = IKeyword.NAME,
						type = IType.ID,
						optional = false,
						doc = @doc ("the identifier of the state")) },
		omissible = IKeyword.NAME)
@validator (StateValidator.class)
@doc (
		value = "A state, like a reflex, can contains several statements that can be executed at each time step by the agent.",
		usages = { @usage (
				value = "Here is an exemple integrating 2 states and the statements in the FSM architecture:",
				examples = { @example (
						value = "state s_init initial: true {",
						isExecutable = false),
						@example (
								value = "	enter { ",
								isExecutable = false),
						@example (
								value = "		write \"Enter in\" + state;",
								isExecutable = false),
						@example (
								value = "	}",
								isExecutable = false),
						@example (
								value = "",
								isExecutable = false),
						@example (
								value = "	write state;",
								isExecutable = false),
						@example (
								value = "",
								isExecutable = false),
						@example (
								value = "	transition to: s1 when: (cycle > 2) {",
								isExecutable = false),
						@example (
								value = "		write \"transition s_init -> s1\";",
								isExecutable = false),
						@example (
								value = "	}",
								isExecutable = false),
						@example (
								value = "",
								isExecutable = false),
						@example (
								value = "	exit {",
								isExecutable = false),
						@example (
								value = "		write \"EXIT from \"+state;",
								isExecutable = false),
						@example (
								value = "	}",
								isExecutable = false),
						@example (
								value = "}",
								isExecutable = false),
						@example (
								value = "state s1 {",
								isExecutable = false),
						@example (
								value = "",
								isExecutable = false),
						@example (
								value = "	enter {write 'Enter in '+state;}",
								isExecutable = false),
						@example (
								value = "",
								isExecutable = false),
						@example (
								value = "	write state;",
								isExecutable = false),
						@example (
								value = "",
								isExecutable = false),
						@example (
								value = "	exit {write 'EXIT from '+state;}",
								isExecutable = false),
						@example (
								value = "}",
								isExecutable = false) }) },
		see = { FsmStateStatement.ENTER, FsmStateStatement.EXIT, FsmTransitionStatement.TRANSITION })
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class FsmStateStatement extends AbstractStatementSequence {

	/** The Allowed architectures. */
	static List<String> AllowedArchitectures = Arrays.asList(IKeyword.USER_CONTROLLED, IKeyword.USER_FIRST,
			IKeyword.USER_INIT, IKeyword.USER_LAST, IKeyword.USER_ONLY);

	/**
	 * The Class StateValidator.
	 */
	public static class StateValidator implements IDescriptionValidator {

		/**
		 * Method validate()
		 *
		 * @see msi.gaml.compilation.IDescriptionValidator#validate(msi.gaml.descriptions.IDescription)
		 */
		@Override
		public void validate(final IDescription description) {
			// Verify that the state is inside a species with fsm control
			final SpeciesDescription species = description.getSpeciesContext();
			final String keyword = description.getKeyword();
			final SkillDescription control = species.getControl();
			if (!FsmArchitecture.class.isAssignableFrom(control.getJavaBase())) {
				if (STATE.equals(keyword)) {
					description.error("A state can only be defined in an fsm-controlled or user-controlled species",
							IGamlIssue.WRONG_CONTEXT);
					return;
				}
				if (control.getJavaBase() == FsmArchitecture.class) {
					description.error("A " + description.getKeyword()
							+ " can only be defined in a user-controlled species (one of" + AllowedArchitectures + ")",
							IGamlIssue.WRONG_CONTEXT);
					return;
				}
			}
			if (!Assert.nameIsValid(description)) return;

			final IExpression expr = description.getFacetExpr(INITIAL);
			if (IExpressionFactory.TRUE_EXPR.equals(expr)) {
				assertNoOther(description, INITIAL);
			} else {
				// See Issue #2866
				// expr = description.getFacetExpr(FINAL);
				// if (IExpressionFactory.TRUE_EXPR.equals(expr)) {
				// assertNoOther(description, FINAL);
				// } else {
				assertAtLeastOne(description, INITIAL);
				// }
			}

		}

		/**
		 * Assert no other.
		 *
		 * @param desc
		 *            the desc
		 * @param facet
		 *            the facet
		 */
		private void assertNoOther(final IDescription desc, final String facet) {
			final IDescription sd = desc.getEnclosingDescription();
			if (!(sd instanceof SpeciesDescription)) return;
			for (final IDescription child : ((SpeciesDescription) sd).getBehaviors()) {
				if (child.equals(desc) || !STATE.equals(child.getKeyword())) { continue; }
				final IExpression expr = child.getFacetExpr(facet);
				if (IExpressionFactory.TRUE_EXPR.equals(expr)) {
					final String error = "Only one " + facet + " state is allowed.";
					child.error(error, IGamlIssue.DUPLICATE_DEFINITION, facet, TRUE);
				}
			}
		}

		/**
		 * Assert at least one.
		 *
		 * @param desc
		 *            the desc
		 * @param facet
		 *            the facet
		 */
		private void assertAtLeastOne(final IDescription desc, final String facet) {
			final IDescription sd = desc.getEnclosingDescription();
			if (!(sd instanceof SpeciesDescription)) return;
			for (final IDescription child : ((SpeciesDescription) sd).getBehaviors()) {
				final String s = child.getKeyword();
				if (STATE.equals(s) || USER_PANEL.equals(s)) {
					final IExpression expr = child.getFacetExpr(facet);
					if (expr == null) { continue; }
					if (IExpressionFactory.TRUE_EXPR.equals(expr)) return;
				}
			}
			final String error = "No " + facet + " state defined";
			sd.error(error, IGamlIssue.MISSING_DEFINITION, sd.getUnderlyingElement(), desc.getKeyword(), facet, TRUE);
		}
	}

	/** The Constant STATE_MEMORY. */
	public static final String STATE_MEMORY = "state_memory";

	/** The Constant INITIAL. */
	public static final String INITIAL = "initial";

	/** The Constant FINAL. */
	public static final String FINAL = "final";

	/** The Constant STATE. */
	protected static final String STATE = "state";

	/** The Constant ENTER. */
	public static final String ENTER = "enter";

	/** The Constant EXIT. */
	public static final String EXIT = "exit";

	/** The enter actions. */
	private FsmEnterStatement enterActions = null;

	/** The exit actions. */
	private FsmExitStatement exitActions = null;

	/** The transitions. */
	List<FsmTransitionStatement> transitions = new ArrayList<>();

	/** The transitions size. */
	private int transitionsSize;

	/** The is initial. */
	boolean isInitial;

	/** The is final. */
	boolean isFinal;

	/**
	 * Instantiates a new fsm state statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public FsmStateStatement(final IDescription desc) {
		super(desc);
		setName(getLiteral(IKeyword.NAME)); // A VOIR
		isInitial = Cast.asBool(null, getLiteral(FsmStateStatement.INITIAL));
		isFinal = Cast.asBool(null, getLiteral(FsmStateStatement.FINAL));
	}

	@Override
	public void setChildren(final Iterable<? extends ISymbol> children) {
		for (final ISymbol c : children) {
			if (c instanceof FsmEnterStatement) {
				enterActions = (FsmEnterStatement) c;
			} else if (c instanceof FsmExitStatement) {
				exitActions = (FsmExitStatement) c;
			} else if (c instanceof FsmTransitionStatement) { transitions.add((FsmTransitionStatement) c); }
		}

		transitionsSize = transitions.size();
		super.setChildren(FluentIterable.from(children)
				.filter(each -> (each != enterActions && each != exitActions && !transitions.contains(each))));
	}

	/**
	 * Begin execution.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	protected boolean beginExecution(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = scope.getAgent();
		if (scope.interrupted()) return false;
		final Boolean enter = (Boolean) agent.getAttribute(ENTER);
		IMap<String, Object> memory = (IMap) agent.getAttribute(STATE_MEMORY);
		if (enter || memory == null) {
			memory = GamaMapFactory.create();
			agent.setAttribute(STATE_MEMORY, memory);
		} else {
			memory.forEach((k, v) -> scope.addVarWithValue(k, v));
		}
		if (enter) {
			if (enterActions != null) { scope.execute(enterActions); }
			if (agent.dead()) return false;
			agent.setAttribute(ENTER, false);
		}
		return true;
	}

	/**
	 * Body execution.
	 *
	 * @param scope
	 *            the scope
	 * @return the object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	protected Object bodyExecution(final IScope scope) throws GamaRuntimeException {
		return super.privateExecuteIn(scope);
	}

	/**
	 * Evaluate transitions.
	 *
	 * @param scope
	 *            the scope
	 * @return the string
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	protected String evaluateTransitions(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = scope.getAgent();
		for (int i = 0; i < transitionsSize; i++) {
			final FsmTransitionStatement transition = transitions.get(i);

			if (transition.evaluatesTrueOn(scope)) {
				final String futureState = transition.getName();
				haltOn(scope);
				scope.execute(transition);
				scope.setAgentVarValue(agent, STATE, futureState);
				return futureState;
			}
		}
		if (agent != null && !agent.dead()) { scope.saveAllVarValuesIn((Map) agent.getAttribute(STATE_MEMORY)); }
		return name;

	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		if (!beginExecution(scope)) return null;
		bodyExecution(scope);
		return evaluateTransitions(scope);
	}

	/**
	 * Halt on.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public void haltOn(final IScope scope) throws GamaRuntimeException {
		if (exitActions != null) { scope.execute(exitActions); }
	}

	/**
	 * Gets the exit statement.
	 *
	 * @return the exit statement
	 */
	public FsmExitStatement getExitStatement() { return exitActions; }

	/**
	 * Checks for exit actions.
	 *
	 * @return true, if successful
	 */
	public boolean hasExitActions() {
		return exitActions != null;
	}

	/**
	 * Checks if is initial.
	 *
	 * @return true, if is initial
	 */
	public boolean isInitial() { return isInitial; }

	/**
	 * Checks if is final.
	 *
	 * @return true, if is final
	 */
	public boolean isFinal() { return isFinal; }

}
