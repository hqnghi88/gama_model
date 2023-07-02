/*******************************************************************************************************
 *
 * TestAgent.java, in msi.gama.core, is part of the source code of the GAMA modeling and simulation platform (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gama.kernel.experiment;

import static msi.gaml.operators.Cast.asFloat;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.emf.common.util.URI;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.metamodel.population.IPopulation;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.experiment;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.expressions.AbstractExpression;
import msi.gaml.expressions.IExpression;
import msi.gaml.statements.IStatement;
import msi.gaml.statements.test.TestExperimentSummary;
import msi.gaml.statements.test.TestStatement;
import msi.gaml.statements.test.WithTestSummary;
import msi.gaml.types.IType;

/**
 * The Class TestAgent.
 */
@experiment (IKeyword.TEST)
@doc ("Experiments supporting the collection of success or failure of tests. Can be used in GUI or headless")
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class TestAgent extends BatchAgent implements WithTestSummary<TestExperimentSummary> {

	/** The summary. */
	// int failedModels = 0;
	TestExperimentSummary summary;

	/**
	 * Instantiates a new test agent.
	 *
	 * @param p
	 *            the p
	 * @param index
	 *            the index
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public TestAgent(final IPopulation p, final int index) throws GamaRuntimeException {
		super(p, index);
	}

	@Override
	protected IExpression defaultStopCondition() {
		return new AbstractExpression() {

			@Override
			public String serialize(final boolean includingBuiltIn) {
				return "cycle = 1";
			}

			@Override
			public Boolean _value(final IScope scope) throws GamaRuntimeException {
				return scope.getClock().getCycle() == 1;
			}

		};
	}

	@Override
	public boolean init(final IScope scope) {
		super.init(scope);
		final TestExperimentSummary summary = getSummary();
		summary.reset();
		if (!summary.isEmpty()) {
			scope.getGui().openTestView(scope, false);
			// if (!getSpecies().isHeadless())
			// scope.getGui().displayTestsResults(getScope(), summary);
		}
		return true;
	}

	@Override
	public boolean step(final IScope scope) {
		super.step(scope);
		dispose();
		return true;
	}

	@Override
	public void dispose() {
		if (dead) return;
		getScope().getGui().displayTestsResults(getScope(), summary);
		getScope().getGui().endTestDisplay();
		super.dispose();
	}

	@Override
	protected String endStatus() {
		return "Tests over: " + summary.getStringSummary();
	}

	@Override
	public void addSpecificParameters(final List<IParameter.Batch> params) {
		params.add(new ParameterAdapter("Stop condition", IExperimentPlan.TEST_CATEGORY_NAME, IType.STRING) {

			@Override
			public String value() {
				return stopCondition != null ? stopCondition.serialize(false) : "none";
			}

		});

		params.add(new ParameterAdapter("Parameter space", IExperimentPlan.TEST_CATEGORY_NAME, "", IType.STRING) {

			@Override
			public String value() {
				final Map<String, IParameter.Batch> explorable = getSpecies().getExplorableParameters();
				if (explorable.isEmpty()) return "1";
				String result = "";
				int dim = 1;
				for (final Map.Entry<String, IParameter.Batch> entry : explorable.entrySet()) {
					result += entry.getKey() + " (";
					final int entryDim = getExplorationDimension(entry.getValue());
					dim = dim * entryDim;
					result += String.valueOf(entryDim) + ") * ";
				}
				if (!result.isEmpty()) { result = result.substring(0, result.length() - 2); }
				result += " = " + dim;
				return result;
			}

			int getExplorationDimension(final IParameter.Batch p) {
				IScope scope = getScope();
				// AD TODO Issue a warning in the compilation if a batch experiment tries to explore non-int or
				// non-float values
				if (p.getAmongValue(scope) != null) return p.getAmongValue(scope).size();
				return (int) ((asFloat(scope, p.getMaxValue(scope)) - asFloat(scope, p.getMinValue(scope)))
						/ asFloat(scope, p.getStepValue(scope))) + 1;
			}

		});

	}

	@Override
	public TestExperimentSummary getSummary() {
		if (summary == null) { summary = new TestExperimentSummary(this); }
		return summary;
	}

	@Override
	public String getTitleForSummary() {
		final String mn = getSpecies().getDescription().getModelDescription().getModelFilePath();
		final String modelName = mn.substring(mn.lastIndexOf('/') + 1).replace(".experiment", "").replace(".gaml", "");
		return getSpecies().getName() + " in " + modelName;
	}

	@Override
	public URI getURI() { return getModel().getURI(); }

	@Override
	public Collection<? extends WithTestSummary<?>> getSubElements() {
		final List<TestStatement> tests = getModel().getAllTests();
		final Consumer<IStatement> filter = t -> { if (t instanceof TestStatement) { tests.add((TestStatement) t); } };
		getSpecies().getBehaviors().forEach(filter);
		return tests;
	}

}
