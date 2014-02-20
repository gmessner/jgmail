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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

/**
 * The <code>POP3Reader</code> class is used to read data from a POP3
 * server connection.
 *
 * @author  Gregory M. Messner <gmessner@messners.com>
 * @version $Revision: 1.1 $
 */

public class POP3Reader extends BufferedReader {

	protected int total = 0;


	/**
	 * Constructor to create A POP3Reader from the InputStream of a socket.
	 *
	 * @param  in  InputStream from a socket connected to a POP3 server.
	 */
	public POP3Reader (InputStream in) {
		super(new InputStreamReader(in));
	}


	/**
	 * Read a line of text. A line is considered to be terminated by
	 * any one of a line feed ('\n'), a carriage return ('\r'), or a
	 * carriage return followed immediately by a linefeed. Additionally,
	 * a lone period "." indicates EOF and null will be returned.
	 *
	 * @return A String containing the contents of the line, not
	 * including any line-termination characters, or null if
	 * the end of the stream has been reached 
	 * @exception  IOException If an I/O error occurs 
	 */
	public String readLine () throws IOException {

		StringBuffer buf  = new StringBuffer(80);
		boolean endOfLine = false;
		boolean lastWasCr = false;
		int bytesRead = 0;
		do {
			int c = read();
			switch (c) {

			    case -1:
					endOfLine = true;
					break;

				case '\r':
					lastWasCr = true;
					bytesRead++;
					break;

				case '\n':
					bytesRead++;
					if (lastWasCr) {
						endOfLine = true;
					} else {
						buf.append((char)'\n');
					}
					break;

				default:
					bytesRead++;
					if (lastWasCr) {
						buf.append((char)'\r');
						lastWasCr = false;
					}
					buf.append((char)c);
					break;
			}

		} while (!endOfLine);
		
		if (bytesRead == 0) {
			return (null);
		}
		
		incrBytesRead(bytesRead);
		String line = buf.toString();
		if (line.equals(".")) {
			return (null);
		} else {
			return (line);
		}
	}	

	

	/*
	 * States for reading/parsing the message header
	 */
   	static protected final int NAME  = 0;
	static protected final int VALUE = 1;
	static protected final int EOL   = 2;
	static protected final int FOLD  = 3;
	static protected final int EOH   = 4;
	static protected final int BODY  = 5;

	/**
	 * Read the message until the end of the header section,
	 * which is denoted by a blank line. We extract all the
	 * header fields and save them in the hashtable which
	 * is returned.
	 *
	 * @return a HashTable containing the header name/value pairs 
	 * @exception  IOException If an I/O error occurs 
	 */
	public Map<String, String> readHeader () throws IOException {
		return (readNameValuePairs());
	}


	/**
	 * This method reads a message or multipart message header and returns
	 * a hashtable containg the name value pairs in the header.
	 *
	 * @return a HashTable containing the header name/value pairs 
	 */
	protected synchronized Map<String, String> readNameValuePairs ()
		throws IOException {

		Map<String, String> header = new HashMap<String, String>(3);

		int bytesRead = 0;
		String name   = null;
		String value  = null;
		StringBuffer nameBuf  = new StringBuffer();
		StringBuffer valueBuf = new StringBuffer();
		int state = NAME;
		while (state != BODY) {

			/*
			 * Read in the next character
			 */
			int c = read();
			if (c == -1) {
				break;
			}

			bytesRead++;

			switch (state) {
			  case NAME:
				if (c != ':') {
					nameBuf.append((char)c);
				} else {
					name = nameBuf.toString();
					name = name.toLowerCase();
					nameBuf.setLength(0);
					state = VALUE;
				}

				break;

			  case VALUE:
				if (c == '\r') {
					state = EOL;
				} else {
					valueBuf.append((char)c);
				}

				break;


			  case EOH:
				state = BODY;
				break;


			  case EOL:
				switch (c) {
				
				  /*
				   * Another CR means end of header
				   */
				  case '\r':
					state = EOH;
					break;

				  /*
				   * White space means we're folding in a value
				   */
				  case '\t':
				  case ' ':
					state = FOLD;
					break;

				  /*
				   * A LF is not a state change unless at EOH
				   */
				  case '\n':
					break;

				  /*
				   * Any other character means we've come to
				   * another name (field). If we have a value
				   * pending add it to the hashtable.
				   */
				  default:
					if (valueBuf.length() > 0) {
						value =
						    valueBuf.toString().trim();
						valueBuf.setLength(0);
						header.put(name, value);
					}

					state = NAME;
					nameBuf.append((char)c);
					break;
				}

				break;

			  /*
			   * Folding in a value
			   */
			  case FOLD:

				/*
				 * If c is a CR then switch to EOL state, 
				 * otherwise, if it is not a newline, space,
				 * or tab swicth to VALUE state and append c
				 * to the value buffer
				 */
				if (c == '\r') {
					state = EOL;
				} else if (c != '\n' && c != ' ' && c != '\t') {
					state = VALUE;
					if (valueBuf.length() > 0) {
						valueBuf.append(' ');
					}
					valueBuf.append((char)c);
				}

				break;
			}
		}

		incrBytesRead(bytesRead);
		
		/*
		 * Do we have a pending name value pair? If so add it to the
		 * hashtable
		 */
		if (valueBuf.length() > 0) {
			header.put(name, valueBuf.toString().trim());
		}

		if (header.isEmpty()) {
			return (null);
		}

		return (header);
	}


	/**
	 * Increment the count of bytes read.
	 *
	 * @param  bytes  The number of bytes to increment by.
	 */
	protected synchronized void incrBytesRead (int bytes) {
			total += bytes;
	}


	/**
	 * Gets the count of bytes read.
	 *
	 * @return the count of bytes read
	 */
	public synchronized int getBytesRead () {
			return (total);
	}
}

