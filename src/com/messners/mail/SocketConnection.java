/*
 *   Copyright (c) 1995-2003 by Gregory M. Messner
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *   For more information contact the author at: gmessner@messners.com
 *
 */

package com.messners.mail;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Provides an implementation of Connection that wraps a java.net.Socket
 * instance. This class is used for connections on all non-J2ME platforms.
 *
 * @see com.messners.mail.Connection
 * @author Gregory M. Messner <gmessner@messners.com>
 * @version $Revision: 1.1 $
 */

public class SocketConnection extends Connection {

	/**
	 * This implementation wraps a standard java.net.Socket instance.
	 */
	private Socket socket;


	/**
	 * Sets the timeout in milliseconds for reads on the connection
	 * to the server.
	 *
	 * @param	timeout	the new read timeout
	 * @exception	IOException	when an error occurs setting the timeout
	 * on the connection.
	 */
	public void setTimeout (int timeout) throws IOException {

		if (socket != null) {
			socket.setSoTimeout(timeout);
		}
	}


	/**
	 * Open a connection.
	 *
	 * @param  host  the hostname or IP address for the connection
	 * @param  port  the port number for the connection
	 * @exception IOException
	 */
	public void open (String host, int port) throws IOException {

		socket = new Socket(host, port);
	}


	/**
	 * Close the connection.
	 *
	 * @exception IOException
	 */
	public void close () throws IOException {

		if (socket != null) {
			socket.close();
			socket = null;
		}
	}


	/**
	 * Gets the InputStream associated with this connection.
	 *
	 * @return the InputStream associated with this connection
	 * @exception IOException
	 */
	public InputStream getInputStream () throws IOException {

		if (socket != null) {
			return (socket.getInputStream());
		} else {
			return (null);
		}
	}


	/**
	 * Gets the OutputStream associated with this connection.
	 *
	 * @return the OutputStream associated with this connection
	 * @exception IOException
	 */
	public OutputStream getOutputStream () throws IOException {

		if (socket != null) {
			return (socket.getOutputStream());
		} else {
			return (null);
		}
	}
}
