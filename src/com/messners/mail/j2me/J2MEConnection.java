/*
 * Copyright(c) 1995-2003 Gregory M. Messner
 * All rights reserved
 *
 * Permission to use, copy, modify and distribute this material for
 * non-commercial personal and educational use without fee is hereby
 * granted, provided that the above copyright notice and this permission
 * notice appear in all copies, and that the name of Gregory M. Messner
 * not be used in advertising or publicity pertaining to this material
 * without the specific, prior written permission of Gregory M. Messner
 * or an authorized representative.
 *
 * GREGORY M. MESSNER MAKES NO REPRESENTATIONS AND EXTENDS NO WARRANTIES,
 * EXPRESSED OR IMPLIED, WITH RESPECT TO THE SOFTWARE, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * ANY PARTICULAR PURPOSE, AND THE WARRANTY AGAINST INFRINGEMENT OF PATENTS
 * OR OTHER INTELLECTUAL PROPERTY RIGHTS. THE SOFTWARE IS PROVIDED "AS IS",
 * AND IN NO EVENT SHALL GREGORY M. MESSNER BE LIABLE FOR ANY DAMAGES,
 * INCLUDING ANY LOST PROFITS OR OTHER INCIDENTAL OR CONSEQUENTIAL DAMAGES
 * RELATING TO THE SOFTWARE.
 */

package com.messners.mail.j2me;

import com.messners.mail.Connection;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

/**
 * Provides an implementation of Connection that wraps a java.net.Socket
 * javax.microedition.io.StreamConnection instance. This class is used
 * for connections on all J2ME platforms.
 *
 * @see     com.messners.mail.Connection
 * @author  Gregory M. Messner <gmessner@messners.com>
 * @version $Revision: 1.2 $
 */

public class J2MEConnection extends Connection {


	/**
	 * For J2ME use a StreamConnection.
	 */
	private StreamConnection socket;


	/**
	 * Sets the timeout in milliseconds for reads on the connection
	 * to the server.
	 *
	 * @param  timeout	the new read timeout
	 * @exception	IOException	when an error occurs setting the timeout
	 * on the connection.
	 */
	public void setTimeout (int timeout) throws IOException {
	}


	/**
	 * Open a connection.
	 *
	 * @param  host  the hostname or IP address for the connection
	 * @param  port  the port number for the connection
	 * @exception IOException
	 */
	public void open (String host, int port) throws IOException {

		String url = "socket://" + host + ":" + port;
		socket = (StreamConnection)Connector.open(url);
	}


	/**
	 * Close the connection.
	 *
	 * @exception IOException
	 */
	public void close() throws IOException {

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
	public InputStream getInputStream() throws IOException {

		if (socket != null) {
			return (socket.openInputStream ());
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
			return (socket.openOutputStream());
		} else {
			return (null);
		}
	}
}
