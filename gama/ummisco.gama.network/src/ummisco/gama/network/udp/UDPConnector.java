/*******************************************************************************************************
 *
 * UDPConnector.java, in ummisco.gama.network, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package ummisco.gama.network.udp;

import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import msi.gama.extensions.messaging.GamaMessage;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.IList;
import msi.gaml.operators.Cast;
import ummisco.gama.network.common.Connector;
import ummisco.gama.network.common.ConnectorMessage;
import ummisco.gama.network.common.GamaNetworkException;
import ummisco.gama.network.common.socket.SocketService;

/**
 * The Class UDPConnector.
 */
public class UDPConnector extends Connector {

	/** The  udp server. */
	public static String _UDP_SERVER = "__udp_server";

	/** The is server. */
	private boolean is_server = false;
	
	/**
	 * Instantiates a new UDP connector.
	 *
	 * @param scope the scope
	 * @param as_server the as server
	 */
	public UDPConnector(final IScope scope, final boolean as_server) {
		is_server = as_server;
	}

	@Override
	public List<ConnectorMessage> fetchMessageBox(final IAgent agent) {
		return super.fetchMessageBox(agent);
	}

	@SuppressWarnings ("unchecked")
	@Override
	public Map<IAgent, LinkedList<ConnectorMessage>> fetchAllMessages() {
		for (final IAgent agt : this.receivedMessage.keySet()) {
			final IList<ConnectorMessage> m = (IList<ConnectorMessage>) agt.getAttribute("messages" + agt);
			if (m != null) {
				for (final ConnectorMessage cm : m) {
					receivedMessage.get(agt).add(cm);
				}
				m.clear();
				agt.setAttribute("message" + agt, m);
			}
		}
		return super.fetchAllMessages();
	}

	/**
	 * Open server socket.
	 *
	 * @param agent the agent
	 */
	public void openServerSocket(final IAgent agent) {
		final Integer port = Cast.asInt(agent.getScope(), this.getConfigurationParameter(SERVER_PORT));

		if (agent.getScope().getSimulation().getAttribute(_UDP_SERVER + port) == null) {
			try {
				final DatagramSocket sersock = new DatagramSocket(port);
				final MultiThreadedUDPSocketServer ssThread = new MultiThreadedUDPSocketServer(agent, sersock, this.getConfigurationParameter(PACKET_SIZE));
				ssThread.start();
				agent.getScope().getSimulation().setAttribute(_UDP_SERVER + port, ssThread);

			} catch (final BindException be) {
				throw GamaRuntimeException.create(be, agent.getScope());
			} catch (final Exception e) {
				throw GamaRuntimeException.create(e, agent.getScope());
			}
		}
	}

	@Override
	protected void connectToServer(final IAgent agent) throws GamaNetworkException {
		if (is_server) {
			openServerSocket(agent);
		}
	}

	@Override
	protected void sendMessage(final IAgent sender, final String receiver, final String cont)
			throws GamaNetworkException {
		String content = cont.replaceAll("\b\r", "@b@@r@");
		content = content.replaceAll("\n", "@n@");

		final String sport = this.getConfigurationParameter(SERVER_PORT);
		final Integer port = Cast.asInt(sender.getScope(), sport);
		final String sURL = this.getConfigurationParameter(SERVER_URL);

		try (final DatagramSocket clientSocket = new DatagramSocket();) {
			final InetAddress IPAddress = InetAddress.getByName(sURL);			
			final byte[] sendData = content.getBytes();
			final DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			sendPacket.setData(sendData);
			clientSocket.send(sendPacket);
		} catch (final Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void send(final IAgent sender, final String receiver, final GamaMessage content) {
		this.sendMessage(sender, receiver, (String) content.getContents(sender.getScope()));
	}

	@Override
	protected void subscribeToGroup(final IAgent agt, final String boxName) throws GamaNetworkException {}

	@Override
	protected void unsubscribeGroup(final IAgent agt, final String boxName) throws GamaNetworkException {}

	@Override
	protected boolean isAlive(final IAgent agent) throws GamaNetworkException {
		final String sport = this.getConfigurationParameter(SERVER_PORT);
		final Integer port = Cast.asInt(agent.getScope(), sport);
		final Thread sersock = (Thread) agent.getScope().getSimulation().getAttribute(_UDP_SERVER + port);
		if (sersock != null && sersock.isAlive()) { return true; }

		return false;
	}

	@Override
	protected void releaseConnection(final IScope scope) throws GamaNetworkException {
		final String sport = this.getConfigurationParameter(SERVER_PORT);
		final Integer port = Cast.asInt(scope, sport);
		final MultiThreadedUDPSocketServer UDPsersock =
				(MultiThreadedUDPSocketServer) scope.getSimulation().getAttribute(_UDP_SERVER + port);
		try {
			if (UDPsersock != null) {
				UDPsersock.getMyServerSocket().close();
				UDPsersock.interrupt();
				scope.getSimulation().setAttribute(_UDP_SERVER + port, null);
			}
		} catch (final Exception e) {
			throw GamaRuntimeException.create(e, scope);
		}
	}

	@Override
	public SocketService getSocketService() { 
		return null;
	}
}