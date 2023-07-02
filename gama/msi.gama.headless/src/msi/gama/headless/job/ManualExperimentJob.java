/*******************************************************************************************************
 *
 * ManualExperimentJob.java, in msi.gama.headless, is part of the source code of the GAMA modeling and simulation
 * platform (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gama.headless.job;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.java_websocket.WebSocket;

import msi.gama.headless.core.GamaHeadlessException;
import msi.gama.headless.core.HeadlessSimulationLoader;
import msi.gama.headless.core.RichExperiment;
import msi.gama.headless.core.RichOutput;
import msi.gama.headless.listener.GamaWebSocketServer;
import msi.gama.headless.listener.ServerExperimentController;
import msi.gama.kernel.experiment.ExperimentPlan;
import msi.gama.kernel.experiment.IParameter;
import msi.gama.kernel.model.IModel;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.IMap;
import msi.gama.util.file.json.GamaJsonList;
import msi.gaml.compilation.GAML;
import msi.gaml.compilation.GamlCompilationError;
import msi.gaml.expressions.IExpressionFactory;
import msi.gaml.types.Types;

/**
 * The Class ExperimentJob.
 */
public class ManualExperimentJob extends ExperimentJob {

	/** The server. */
	public GamaWebSocketServer server;

	/** The socket. */
	public WebSocket socket;

	/** The params. */
	public GamaJsonList params;

	/** The end cond. */
	public String endCond = "";

	/** The controller. */
	public ServerExperimentController controller;

	/** The play command. */
	public IMap<String, Object> playCommand;

	/**
	 * Instantiates a new manual experiment job.
	 *
	 * @param sourcePath
	 *            the source path
	 * @param exp
	 *            the exp
	 * @param gamaWebSocketServer
	 *            the gama web socket server
	 * @param sk
	 *            the sk
	 * @param p
	 *            the p
	 * @param console
	 *            the console
	 * @param status
	 *            the status
	 * @param dialog
	 *            the dialog
	 */
	public ManualExperimentJob(final String sourcePath, final String exp, final GamaWebSocketServer gamaWebSocketServer,
			final WebSocket sk, final GamaJsonList p, final boolean console, final boolean status, final boolean dialog,
			final boolean runtime) {
		// (final String sourcePath, final String exp, final long max, final String untilCond,
		// final double s)
		super(sourcePath, exp, 0, "", 0);
		server = gamaWebSocketServer;
		socket = sk;
		params = p;
		controller = new ServerExperimentController(this, socket, console, status, dialog, runtime);
	}

	@Override
	public void doStep() {
		this.step = simulator.step();
	}

	@Override
	public void doBackStep() {
		this.step = simulator.backStep();
	}

	@Override
	public void load() throws IOException, GamaHeadlessException {
		System.setProperty("user.dir", this.sourcePath);
		final List<GamlCompilationError> errors = new ArrayList<>();
		final IModel mdl = HeadlessSimulationLoader.loadModel(new File(this.sourcePath), errors);
		this.modelName = mdl.getName();
		this.simulator = new RichExperiment(mdl);
	}

	/**
	 * Load and build with json.
	 *
	 * @throws InstantiationException
	 *             the instantiation exception
	 * @throws IllegalAccessException
	 *             the illegal access exception
	 * @throws ClassNotFoundException
	 *             the class not found exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws GamaHeadlessException
	 *             the gama headless exception
	 */
	public void loadAndBuildWithJson() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
			IOException, GamaHeadlessException {

		this.load();
		this.setup();
		controller.setExperiment(simulator.getModel().getExperiment(experimentName));
		simulator.setup(experimentName, this.seed, params, this);
		initEndContion(endCond);
	}

	/**
	 * Inits the param.
	 *
	 * @param p
	 *            the p
	 */
	@SuppressWarnings ("unchecked")
	public void initParam(final GamaJsonList p) {
		params = p;
		if (params != null) {
			final ExperimentPlan curExperiment = (ExperimentPlan) simulator.getExperimentPlan();
			for (var param : params.listValue(null, Types.MAP, false)) {
				IMap<String, Object> m = (IMap<String, Object>) param;
				String type = m.get("type").toString();
				Object v = m.get("value");
				if ("int".equals(type)) { v = Integer.valueOf("" + m.get("value")); }
				if ("float".equals(type)) { v = Double.valueOf("" + m.get("value")); }

				final IParameter.Batch b = curExperiment.getParameterByTitle(m.get("name").toString());
				if (b != null) {
					curExperiment.setParameterValueByTitle(curExperiment.getExperimentScope(), m.get("name").toString(),
							v);
				} else {
					curExperiment.setParameterValue(curExperiment.getExperimentScope(), m.get("name").toString(), v);
				}

			}
		}
	}

	/**
	 * Inits the end contion.
	 *
	 * @param cond
	 *            the cond
	 */
	// Initialize the enCondition
	public void initEndContion(final String cond) {
		if (cond == null || "".equals(cond)) {
			endCondition = IExpressionFactory.FALSE_EXPR;
		} else {
			endCondition = GAML.getExpressionFactory().createExpr(cond, simulator.getModel().getDescription());
			// endCondition = GAML.compileExpression(untilCond, simulator.getSimulation(),
			// true);
		}
		if (endCondition.getGamlType() != Types.BOOL)
			throw GamaRuntimeException.error("The until condition of the experiment should be a boolean",
					simulator.getSimulation().getScope());
	}

	@Override
	public void exportVariables() {
		final int size = this.listenedVariables.length;
		if (size == 0) return;
		for (int i = 0; i < size; i++) {
			final ListenedVariable v = this.listenedVariables[i];
			if (this.step % v.frameRate == 0) {
				final RichOutput out = simulator.getRichOutput(v);
				if (out == null || out.getValue() == null) {} else if (out.getValue() instanceof BufferedImage) {
					try (ByteArrayOutputStream out1 = new ByteArrayOutputStream()) {
						BufferedImage bi = (BufferedImage) out.getValue();

						ImageIO.write(bi, "png", out1);

						byte[] array1 = out1.toByteArray();
						byte[] array2 = { (byte) 0 };
						byte[] array3 = { (byte) i };
						byte[] joinedArray = Arrays.copyOf(array1, array1.length + array2.length + array3.length);
						System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
						System.arraycopy(array3, 0, joinedArray, array1.length + array2.length, array3.length);

						ByteBuffer byteBuffer = ByteBuffer.wrap(joinedArray);
						if (!socket.isClosing() && !socket.isClosed()) { socket.send(byteBuffer); }
						// server.broadcast(byteBuffer);
						byteBuffer.clear();

					} catch (IOException e) {
						e.printStackTrace();
					}
					// v.setValue(writeImageInFile((BufferedImage) out.getValue(), v.getName(), v.getPath()), step,
					// out.getType());
				} else {
					byte[] array1 = (out.getName() + ": " + out.getValue().toString()).getBytes();
					byte[] array2 = { (byte) 1 };
					byte[] array3 = { (byte) i };
					byte[] joinedArray = Arrays.copyOf(array1, array1.length + array2.length + array3.length);
					System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
					System.arraycopy(array3, 0, joinedArray, array1.length + array2.length, array3.length);

					ByteBuffer byteBuffer = ByteBuffer.wrap(joinedArray);
					if (!socket.isClosing() && !socket.isClosed()) { socket.send(byteBuffer); }
					v.setValue(out.getValue(), out.getStep(), out.getType());
				}
			} else {
				v.setValue(null, this.step);
			}
		}
		// if (this.outputFile != null) {
		// this.outputFile.writeResultStep(this.step, this.listenedVariables);
		// }

	}

}
