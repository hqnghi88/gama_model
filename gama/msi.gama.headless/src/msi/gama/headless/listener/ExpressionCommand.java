package msi.gama.headless.listener;

import org.java_websocket.WebSocket;

import msi.gama.headless.core.GamaServerMessageType;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.runtime.ExecutionScope;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import msi.gama.util.IMap;
import msi.gaml.compilation.GAML;
import msi.gaml.compilation.GamlIdiomsProvider;
import ummisco.gama.dev.utils.DEBUG;

public class ExpressionCommand implements ISocketCommand {
	@Override
	public CommandResponse execute(final WebSocket socket, IMap<String, Object> map) {

		final String exp_id = map.get("exp_id") != null ? map.get("exp_id").toString() : "";
		final Object expr = map.get("expr");
		final String socket_id = map.get("socket_id") != null ? map.get("socket_id").toString()
				: ("" + socket.hashCode());
		final boolean escaped = map.get("escaped") == null ? false : Boolean.parseBoolean("" + map.get("escaped"));
		final GamaWebSocketServer gamaWebSocketServer = (GamaWebSocketServer) map.get("server");
		DEBUG.OUT("expresion");
		DEBUG.OUT(exp_id);
		DEBUG.OUT(expr);

		if (exp_id == "" || expr == null) {
			return new CommandResponse(GamaServerMessageType.MalformedRequest,
					"For 'expression', mandatory parameters are: 'exp_id' and 'expr'", map, false);
		}

		var gama_exp = gamaWebSocketServer.get_listener().getExperiment(socket_id, exp_id);
		if (gama_exp != null && gama_exp.getSimulation() != null) {

//			final boolean wasPaused = gama_exp.controller.isPaused();
//			gama_exp.controller.directPause();
//			while(gama_exp.controller.running) {
////				System.out.println("request pause");
//			}
			final String res = processInput(gama_exp.controller.getExperiment().getCurrentSimulation().getAgent(),
					expr.toString());
			if (res == null || res.length() == 0 || res.startsWith("> Error: ")) {//
				return new CommandResponse(GamaServerMessageType.UnableToExecuteRequest, res, map, false);

			}
//			if (!wasPaused) {
//				gama_exp.controller.userStart();
//			}
			return new CommandResponse(GamaServerMessageType.CommandExecutedSuccessfully, res, map, escaped);

		} else {
			return new CommandResponse(GamaServerMessageType.UnableToExecuteRequest, "Wrong exp_id " + exp_id, map,
					false);
		}
	}

	protected String processInput(final IAgent agt, final String s) {
		String result = null;
		IAgent agent = agt;// = getListeningAgent();
		if (agent == null) {
			agent = GAMA.getPlatformAgent();
		}
		final IScope scope = new ExecutionScope(agent.getScope().getRoot(), " in console");// agent.getScope();
		if (!agent.dead()) {
			final var entered = s.trim();
//			var error = false;
			if (entered.startsWith("?")) {
				result = GamlIdiomsProvider.getDocumentationOn(entered.substring(1));
			} else {
				try {
					final var expr = GAML.compileExpression(s, agent, false);
					if (expr != null) {
						result = "" + scope.evaluate(expr, agent).getValue();// StringUtils.toGaml(scope.evaluate(expr,
																				// agent).getValue(), true);
					}
				} catch (final Exception e) {
//					error = true;
					result = "> Error: " + e.getMessage();
				} finally {
					agent.getSpecies().removeTemporaryAction();
				}
			}
			// append(result, error, true);
//			if (!error && GAMA.getExperiment() != null) {
//				GAMA.getExperiment().refreshAllOutputs();
//			}
		}
		return result;

	}
}
