/*******************************************************************************************************
 *
 * TabuSearch.java, in msi.gama.core, is part of the source code of the GAMA modeling and simulation platform (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gama.kernel.batch.optimization;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.kernel.batch.StoppingCriterion;
import msi.gama.kernel.batch.StoppingCriterionMaxIt;
import msi.gama.kernel.experiment.BatchAgent;
import msi.gama.kernel.experiment.IExperimentPlan;
import msi.gama.kernel.experiment.IParameter;
import msi.gama.kernel.experiment.ParameterAdapter;
import msi.gama.kernel.experiment.ParametersSet;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.facet;
import msi.gama.precompiler.GamlAnnotations.facets;
import msi.gama.precompiler.GamlAnnotations.inside;
import msi.gama.precompiler.GamlAnnotations.symbol;
import msi.gama.precompiler.GamlAnnotations.usage;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.ISymbolKind;
import msi.gama.runtime.IScope;
import msi.gama.runtime.concurrent.GamaExecutorService;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.descriptions.IDescription;
import msi.gaml.expressions.IExpression;
import msi.gaml.operators.Cast;
import msi.gaml.types.IType;

/**
 * The Class TabuSearch.
 */
@symbol (
		name = IKeyword.TABU,
		kind = ISymbolKind.BATCH_METHOD,
		with_sequence = false,
		concept = { IConcept.BATCH, IConcept.ALGORITHM })
@inside (
		kinds = { ISymbolKind.EXPERIMENT })
@facets (
		value = { @facet (
				name = IKeyword.NAME,
				type = IType.ID,
				optional = false,
				internal = true,
				doc = @doc ("The name of the method. For internal use only")),
				@facet (
						name = TabuSearch.ITER_MAX,
						type = IType.INT,
						optional = true,
						doc = @doc ("number of iterations. this number corresponds to the number of \"moves\" in the parameter space. For each move, the algorithm will test the whole neighborhood of the current solution, each neighbor corresponding to a particular set of parameters and thus to a run. Thus, there can be several runs per iteration (maximum: 2^(number of parameters)).")),
				@facet (
						name = HillClimbing.INIT_SOL,
						type = IType.MAP,
						optional = true,
						doc = @doc ("init solution: key: name of the variable, value: value of the variable")),
				@facet (
						name = TabuSearch.LIST_SIZE,
						type = IType.INT,
						optional = true,
						doc = @doc ("size of the tabu list")),
				@facet (
						name = IKeyword.MAXIMIZE,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("the value the algorithm tries to maximize")),
				@facet (
						name = IKeyword.MINIMIZE,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("the value the algorithm tries to minimize")),
				@facet (
						name = IKeyword.AGGREGATION,
						type = IType.LABEL,
						optional = true,
						values = { IKeyword.MIN, IKeyword.MAX, "avr" },
						doc = @doc ("the agregation method")) },
		omissible = IKeyword.NAME)
@doc (
		value = "This algorithm is an implementation of the Tabu Search algorithm. See the wikipedia article and [batch161 the batch dedicated page].",
		usages = { @usage (
				value = "As other batch methods, the basic syntax of the tabu statement uses `method tabu` instead of the expected `tabu name: id` : ",
				examples = { @example (
						value = "method tabu [facet: value];",
						isExecutable = false) }),
				@usage (
						value = "For example: ",
						examples = { @example (
								value = "method tabu iter_max: 50 tabu_list_size: 5 maximize: food_gathered;",
								isExecutable = false) }) })
public class TabuSearch extends ALocalSearchAlgorithm {

	/** The Constant ITER_MAX. */
	protected static final String ITER_MAX = "iter_max";

	/** The Constant LIST_SIZE. */
	protected static final String LIST_SIZE = "tabu_list_size";

	/** The tabu list size. */
	int tabuListSize = 5;

	/** The stopping criterion. */
	StoppingCriterion stoppingCriterion = new StoppingCriterionMaxIt(50);

	/**
	 * Instantiates a new tabu search.
	 *
	 * @param species
	 *            the species
	 */
	public TabuSearch(final IDescription species) {
		super(species);
		initParams();

	}

	/**
	 * Keep sol.
	 *
	 * @param neighborSol
	 *            the neighbor sol
	 * @param neighborFitness
	 *            the neighbor fitness
	 * @param bestFitnessAlgo
	 *            the best fitness algo
	 * @return true, if successful
	 */
	public boolean keepSol(final ParametersSet neighborSol, final Double neighborFitness,
			final Double bestFitnessAlgo) {
		final boolean neighFitnessGreaterThanBest = neighborFitness > bestFitnessAlgo;
		if (isMaximize() && neighFitnessGreaterThanBest || !isMaximize() && !neighFitnessGreaterThanBest) // setBestFitness(neighborFitness);
			return true;
		return false;
	}

	@Override
	public ParametersSet findBestSolution(final IScope scope) throws GamaRuntimeException {
		initializeTestedSolutions();
		final List<ParametersSet> tabuList = new ArrayList<>();
		ParametersSet bestSolutionAlgo = this.solutionInit;
		tabuList.add(bestSolutionAlgo);
		final double currentFitness =
				(Double) currentExperiment.launchSimulationsWithSolution(bestSolutionAlgo).get(IKeyword.FITNESS).get(0);
		testedSolutions.put(bestSolutionAlgo, currentFitness);
		setBestSolution(new ParametersSet(bestSolutionAlgo));
		setBestFitness(currentFitness);

		int nbIt = 0;
		final Map<String, Object> endingCritParams = new Hashtable<>();
		endingCritParams.put("Iteration", Integer.valueOf(nbIt));
		while (!stoppingCriterion.stopSearchProcess(endingCritParams)) {
			// scope.getGui().debug("TabuSearch.findBestSolution while stoppingCriterion " + endingCritParams);
			final List<ParametersSet> neighbors = neighborhood.neighbor(scope, bestSolutionAlgo);
			neighbors.removeAll(tabuList);
			if (neighbors.isEmpty()) {
				if (tabuList.isEmpty()) { break; }
				neighbors.add(tabuList.get(scope.getRandom().between(0, tabuList.size() - 1)));
			}
			double bestFitnessAlgo;

			if (isMaximize()) {
				bestFitnessAlgo = Double.NEGATIVE_INFINITY;
			} else {
				bestFitnessAlgo = Double.POSITIVE_INFINITY;
			}
			ParametersSet bestNeighbor = null;

			nbIt++;

			if (GamaExecutorService.shouldRunAllSimulationsInParallel(currentExperiment)
					&& !currentExperiment.getParametersToExplore().isEmpty()) {
				Map<ParametersSet, Double> result = testSolutions(neighbors);
				for (ParametersSet p : result.keySet()) {
					if (keepSol(p, result.get(p), bestFitnessAlgo)) { bestNeighbor = p; }
				}
			} else {
				for (final ParametersSet neighborSol : neighbors) {
					if (neighborSol == null) { continue; }
					Double neighborFitness = testedSolutions.get(neighborSol);
					if ((neighborFitness != null) && (neighborFitness != Double.MAX_VALUE)) { continue; }
					neighborFitness = (Double) currentExperiment.launchSimulationsWithSolution(neighborSol)
							.get(IKeyword.FITNESS).get(0);
					testedSolutions.put(neighborSol, neighborFitness);

					if (keepSol(neighborSol, neighborFitness, bestFitnessAlgo)) { bestNeighbor = neighborSol; }

				}
			}

			if (bestNeighbor == null) { break; }
			bestSolutionAlgo = bestNeighbor;
			tabuList.add(bestSolutionAlgo);
			if (tabuList.size() > tabuListSize) { tabuList.remove(0); }
			// currentFitness = bestFitnessAlgo;
			endingCritParams.put("Iteration", Integer.valueOf(nbIt));
		}
		// DEBUG.LOG("Best solution : " + currentSol + " fitness : "
		// + currentFitness);

		return getBestSolution();
	}

	/** The iter max. */
	int iterMax = 50;
	//
	// @Override
	// public void initializeFor(final IScope scope, final BatchAgent agent) throws GamaRuntimeException {
	// super.initializeFor(scope, agent);
	//
	// }

	@Override
	public void initParams(final IScope scope) {
		final IExpression maxIt = getFacet(ITER_MAX);
		if (maxIt != null) {
			iterMax = Cast.asInt(scope, maxIt.value(scope));
			stoppingCriterion = new StoppingCriterionMaxIt(iterMax);
		}
		final IExpression listsize = getFacet(LIST_SIZE);
		if (listsize != null) { tabuListSize = Cast.asInt(scope, listsize.value(scope)); }
	}

	@Override
	public void addParametersTo(final List<IParameter.Batch> params, final BatchAgent agent) {
		super.addParametersTo(params, agent);
		params.add(new ParameterAdapter("Tabu list size", BatchAgent.CALIBRATION_EXPERIMENT, IType.INT) {

			@Override
			public Object value() {
				return tabuListSize;
			}

		});
		params.add(
				new ParameterAdapter("Maximum number of iterations", BatchAgent.CALIBRATION_EXPERIMENT, IType.FLOAT) {

					@Override
					public Object value() {
						return iterMax;
					}

				});
	}

}
