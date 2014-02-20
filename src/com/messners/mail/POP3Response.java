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

/**
 * The <code>POP3Reponse</code> class is used to read a response to a
 * POP3 server command. It is usually only used internally by POP3 objects.
 *
 * @author  Gregory M. Messner <gmessner@messners.com>
 * @version $Revision: 1.1 $
 */
public class POP3Response {


	protected String cmdResponse    = null;
	protected String cmdResponses[] = null;
	protected String tokens[] = null;
	protected boolean ok = false;


	/**
	 * Construct a POP3Response based on the passed in string.
	 *
	 * @param  line  A response read from a POP3 server.
	 */
	public POP3Response (String line) {
		parseFirstLine(line);
	}


	/**
	 * Construct a POP3Response based on the passed in string array.
	 *
	 * @param  lines  Multiline responses read from a POP3 server.
	 */
	public POP3Response (String lines[]) {

		parseFirstLine(lines[0]);
		cmdResponses = new String[lines.length - 1];
		for (int i = 0; i < cmdResponses.length; i++) {
			cmdResponses[i] = lines[i + 1];
		}
	}


	/**
	 * Parse the first line of a response from a POP3 server.
	 *
	 * @param  line  A response read from a POP3 server.
	 */
	private void parseFirstLine (String line) {
		
		cmdResponse = line;
		if (line.trim().startsWith("+OK")) {
			ok = true;

			java.util.StringTokenizer st = 
				new java.util.StringTokenizer(line);
			tokens = new String[st.countTokens()];

			int i = 0;
			while (st.hasMoreTokens()) {
				tokens[i++] = st.nextToken();
			}
		}
	}


	/**
	 * Returns the response read from the POP3 server.
	 */
	public String getResponse () {
		return (cmdResponse);
	}


	/**
	 * Returns the multiline responses read from the POP3 server.
	 */
	public String [] getMultipleResponses () {
		return (cmdResponses);
	}


	/**
	 * Returns the token from the specified index in the response string.
	 *
	 * @param  index  The index of the token to return.
	 */
	public String getToken (int index) 
		throws ArrayIndexOutOfBoundsException {

		if (tokens == null || index < 0 || index >= tokens.length) {
			throw new ArrayIndexOutOfBoundsException();
		}

		return (tokens[index]);
	}


	/**
	 * Returns an interger value (token) from a response string.
	 *
	 * @param  index  the index of the token to return the integer value
	 * for.
	 */
	public int getIntToken (int index) 
		throws ArrayIndexOutOfBoundsException {

		return (Integer.parseInt(getToken(index)));
	}
	

	/**
	 * Returns the status of the response read from the POP3 server.
	 */
	public boolean isOk () {
		return (ok);
	}
}

