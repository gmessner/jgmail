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
 * This class defines an event listener for event fired from
 * <code>POP3</code> objects. These events
 * are either a command to or response from the POP3 server.
 *
 * @see      com.messners.mail.POP3
 *
 * @author   Gregory M. Messner <gmessner@messners.com>
 * @version $Revision: 1.1 $
 */

public interface POP3StatusListener extends java.util.EventListener {

	/**
	 * This method is called when a POP3 event occurs
	 */
	public void pop3Status (MailStatusEvent evt);
}


