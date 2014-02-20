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
 * POP3MessageInfo is used to return information about messages stored
 * on a POP3 server. Its fields are used to mean slightly different
 * things depending on the information being returned.<p>
 *
 * In response to a message listings, number contains the message number,
 * size contains the size of the message in bytes, and identifier is null.<p>
 *
 * In response to unique identifier listings, number contains the message
 * number, size is undefined, and identifier contains the message's unique
 * identifier. 
 *
 * @author  Gregory M. Messner <gmessner@messners.com>
 * @version $Revision: 1.1 $
 */

public class POP3MessageInfo {

	protected int size   = 0;
	protected int number = 0;
	protected String identifier = null;


	/**
	 * Package internal use constructor.
	 */
	protected POP3MessageInfo () {
	}


	/**
	 * Construct a POP3MessageInfo object representing a response from
	 * a message listing.
	 *
	 * @param  number  the message number for this message.
	 * @param  size    the size for this message.
	 */
	public POP3MessageInfo (int number, int size) {
		this.number = number;
		this.size   = size;
	}


	/**
	 * Construct a POP3MessageInfo object representing a response to
	 * a unique identifier listing.
	 *
	 * @param  number  the message number for this message.
	 * @param  identifier  the unique identifier for this message.
	 */
	public POP3MessageInfo (int number, String identifier) {
		this.number = number;
		this.identifier = identifier;
	}

	
	/**
	 * Gets the size in bytes of the message.
	 *
	 * @return the size in bytes of the message
	 */
	public int getMessageSize () {
		return (size);
	}


	/**
	 * Sets the size in bytes of the message.
	 *
	 * @param  size  the size of the message in bytes.
	 */
	public void setMessageSize (int size) {
		this.size = size;
	}


	/**
	 * Gets the unique identifier for this message.
	 *
	 * @return the unique identifier for this message
	 */
	public String getUniqueIdentifier () {
		return (identifier);
	}


	/**
	 * Sets the unique identifier for this message.
	 *
	 * @param  identifier  the unique identifier for this message.
	 */
	public void setUniqueIdentifier (String identifier) {
		this.identifier = identifier;
	}


	/**
	 * Gets the number of this message.
	 *
	 * @return the number of this message
	 */
	public int getMessageNumber () {
		return (number);
	}


	/**
	 * Sets the number of this message.
	 *
	 * @param  number  the number for this message.
	 */
	public void setMessageNumber (int number) {
		this.number = number;
	}
}

