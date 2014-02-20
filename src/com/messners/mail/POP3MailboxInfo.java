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
 * POP3MailboxInfo is used to return information about messages stored
 * on a POP3 server.
 *
 * This info is returned in response to a status command. It contains the
 * number of messages in the mailbox, the size of the mailbox in bytes.
 *
 * @author  Gregory M. Messner <gmessner@messners.com>
 * @version $Revision: 1.1 $
 */

public class POP3MailboxInfo {

	protected int size  = 0;
	protected int count = 0;


	public POP3MailboxInfo () {
	}


	/**
	 * Construct a POP3MailboxInfo object which represents a mailbox with
	 * the specified count of messages and size in bytes.
	 */
	public POP3MailboxInfo (int count, int size) {
		this.count = count;
		this.size  = size;
	}


	/**
	 * Get the count of messages in the mailbox.
	 */
	public int getMessageCount () {
		return (count);
	}


	/**
	 * Get the size in bytes of the mailbox.
	 */
	public int getMailboxSize () {
		return (size);
	}
}
	

