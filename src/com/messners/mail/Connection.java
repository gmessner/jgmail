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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This is the base class used to abstract a Socket connection. It is required
 * due to the different type of Socket connections used by J2ME and other types
 * of Java.
 *
 * @author  Gregory M. Messner <gmessner@messners.com>
 * @version $Revision: 1.1 $
 */
public abstract class Connection {

	/**
	 * Indicates whether this is a J2ME VM. We use this 
	 * decide which implementation of Connection to use.
	 */
	protected static boolean isMicroEdition;
	static {
		isMicroEdition = (System.getProperty(
			"microedition.configuration") != null);
	}


	/**
	 * Sets the timeout in milliseconds for reads on the connection
	 * to the server.
	 *
	 * @param  timeout  the new read timeout
	 * @exception  IOException  when an error occurs setting the timeout
	 * on the connection.
	 */
	public abstract void setTimeout (int timeout) throws IOException;


	/**
	 * Open a connection.
	 *
	 * @param  host  the hostname or IP address for the connection
	 * @param  port  the port number for the connection
	 * @exception IOException
	 */
	public abstract void open (String host, int port) throws IOException;


	/**
	 * Close the connection.
	 *
	 * @exception IOException
	 */
	public abstract void close () throws IOException;


	/**
	 * Get the InputStream associated with this connection.
	 *
	 * @exception IOException
	 */
	public abstract InputStream getInputStream () throws IOException;


	/**
	 * Get the OutputStream associated with this connection.
	 *
	 * @exception IOException
	 */
	public abstract OutputStream getOutputStream () throws IOException;


	/**
	 * Get an instance of a platform specific Connection implementation.
	 * Uses the "microedition.platform" property to decide which class
	 * to instantiate.
	 */
	public static Connection getInstance () throws Exception {

		String classname;
		if (isMicroEdition) {
			classname = "com.messners.mail.j2me.J2MEConnection";
		} else {
			classname = "com.messners.mail.SocketConnection";
		}

		return ((Connection)(Class.forName(classname).newInstance()));
	}
}
