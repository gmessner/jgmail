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
 * This class defines the event that will occur when a command is sent
 * to or a response is recieved from a SMTP/POP3 server.
 *
 * @see     com.messners.mail.POP3
 * @see     com.messners.mail.SMTP
 * @author  Gregory M. Messner <gmessner@messners.com>
 * @version $Revision: 1.1 $
 */

public class MailStatusEvent extends java.util.EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constant to specify that a command to the 
	 * mail server generated the event.
	 */
	public static final int COMMAND    = 0;

	/**
	 * Constant to specify that a response from the
	 * mail server generated the event.
	 */
	public static final int RESPONSE   = 1;

	/**
	 * Constant to specify the amount of progress sending or receiving
	 * a message from the mail server generated the event.
	 */
	public static final int PROGRESS   = 2;

	/**
	 * Constant to specify that sending or receiving mail has started
	 * and generated the event.
	 */
	public static final int START      = 3;

	/**
	 * Constant to specify that sending or receiving mail has completed 
	 * and generated the event.
	 */
	public static final int DONE       = 4;

	/**
	 * Constant to specify that an exception generated the event.
	 */
	public static final int EXCEPTION  = 5;


	protected int type  = COMMAND;
	protected int total = -1;
	protected int size  = -1;
	protected int thisChunk = -1;
	protected String message = null;


	/*
	 * Constructor for a mail status event.
	 *
	 * @param  source  The source of the event.
	 * @param  type    The type of event 
	 *		    (COMMAND|RESPONSE|START|DONE|EXCEPTION).
	 * @param  message The message for the event (either the command to
	 * or response from the server).
	 */
	public MailStatusEvent (Object source, int type, String message) {

		super(source);
		this.type = type;
		this.message = message;
	}


	/*
	 * Constructor for a mail status event.
	 *
	 * @param  source  the source of the event
	 */
	public MailStatusEvent (Object source, 
		int thisChunk, int total, int size) {

		super(source);
		this.type = PROGRESS;
		this.size = size;
		this.total = total;
		this.thisChunk = thisChunk;
	}


	/*
	 * Constructor for a mail status event.
	 *
	 * @param  source  the source of the event
	 */
	public MailStatusEvent (Object source, int thisChunk, int total) {

		super(source);
		this.type = PROGRESS;
		this.total = total;
		this.thisChunk = thisChunk;
	}


	/**
	 * Gets the event message. This will be either the command sent to
	 * or the response recieved from the server.
	 *
	 * @return the event message
	 */
	public String getMessage () {
		return (message);
	}


	/**
	 * Gets the type for this event. Will be either
	 * COMMAND, RESPONSE, START, DONE, or EXCEPTION.
	 *
	 * @return the event type, will be one of 
	 * COMMAND, RESPONSE, START, DONE, or EXCEPTION
	 */
	public int getType () {
		return (type);
	}


	/**
	 * Gets the total bytes processed.
	 *
	 * @return the total bytes processed
	 */
	public int getTotalBytes () {
		return (total);
	}


	/**
	 * Gets the size of this message.
	 *
	 * @return the size of this message
	 */
	public int getMessageSize () {
		return (size);
	}


	/**
	 * Gets the bytes processed for this event.
	 *
	 * @return the bytes processed for this event
	 */
	public int getBytes () {
		return (thisChunk);
	}
}

