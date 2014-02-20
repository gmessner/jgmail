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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * The <code>POP3</code> class implements the Post Office Protocol 3 (POP3).
 * described in RFC-1725. It also provides support for attached files 
 * in accordance with RFC-2045.
 *
 * @author  Gregory M. Messner <gmessner@messners.com>
 * @version $Revision: 1.1 $
 */

public class POP3 {

	/**
	 * Constant indicating POP3 DISCONNECTED state.
	 */
	public static final int DISCONNECTED = 0;

	/**
	 * Constant indicating POP3 AUTHORIZATION state.
	 */
	public static final int AUTHORIZATION = 1;

	/**
	 * Constant indicating POP3 TRANSACTION state.
	 */
	public static final int TRANSACTION = 2;

	/**
	 * Constant indicating POP3 UPDATE state.
	 */
	public static final int UPDATE = 3;

	/**
	 * Default POP3 server port (as per RFC-1725)
	 */
	public static final int POP3_PORT = 110;


	protected int state             = DISCONNECTED;
	protected int  timeout          = 10000;
	protected String host           = null;
	protected POP3Response response = null;
	protected String lastCommand    = null;
	protected Connection connection = null;
	protected POP3Reader in         = null;
	protected PrintWriter out       = null;

	protected static String defaultDir = Utilities.getSystemTmpDirectory();
	protected String attachmentDir = defaultDir;
	protected ArrayList<POP3StatusListener> pop3Listeners = 
			new ArrayList<POP3StatusListener>();


	/**
	 * Create a disconnected <code>POP3</code> instance. 
	 */
	public POP3 () {
	}


	/**
	 * Create a disconnected <code>POP3</code> instance pointing to the
	 * specified host.
	 *
	 * @param  host  the hostname to connect to when connecting
	 */
	public POP3 (String host) {
		this.host = host;
	}


	/**
	 * Gets the POP3Reader object for this POP3 instance.
	 *
	 * @return the POP3Reader object for this POP3 instance
	 */
	public POP3Reader getInputStream () {
		return (in);
	}


	/**
	 * Sets the default directory for saving attachemnts. All new POP3
	 * objects default to this directory. Use setAttachmentDir() to set the
	 * attachment directory for a specific POP3 object.
	 *
	 * @param  dir  the new default attachment directory
	 */
	public static void setDefaultAttachmentDir (String dir) {
		defaultDir = dir;
	}

	/**
	 * Gets the default directory for saving attachemnts. All new POP3
	 * objects default to this directory. Use setAttachmentDir() to set the
	 * attachment directory for a specific POP3 object.
	 *
	 * @return the default directory for saving attachemnts
	 */
	public static String getDefaultAttachmentDir () {
		return (defaultDir);
	}


	/**
	 * Sets the directory for saving attachemnts.
	 *
	 * @param  dir  the new attachment directory.
	 */
	public void setAttachmentDir (String dir) {
		attachmentDir = dir;
	}


	/**
	 * Gets the name of the directory which attachments are saved in.
	 *
	 * @return the name of the directory which attachments are saved in
	 */
	public String getAttachmentDir () {
		return (attachmentDir);
	}



	/**
	 * Gets the last command response returned from the server.
	 *
	 * @return the last command response returned from the server
	 */
	public String getLastResponse () {
		if (response == null) {
			return (null);
		} else {
			return (response.getResponse());
		}
	}


	/**
	 * Gets the last command sent to the server.
	 * @return the last command sent to the server
	 */
	public String getLastCommand () {
		return (lastCommand);
	}


	/**
	 * Gets the current POP3 state. Will be one of the following:<br>
	 * <pre>
	 * DISCONNECTED
	 * AUTHORIZATION
	 * TRANSACTION
	 * UPDATE
	 * </pre>
	 *
	 * @return the current POP3 state
	 */
	public int getState () {
		return (state);
	}


	/**
	 * Gets the timeout in milliseconds for reads on the connection
	 * to the server.
	 *
	 * @return the timeout in milliseconds
	 */
	public int getTimeout () {
		return (timeout);
	}


	/**
	 * Sets the timeout in milliseconds for reads on the connection
	 * to the server.
	 *
	 * @param  timeout  The new read timeout.
	 * @exception  IOException  When an error occurs setting the timeout
	 * on the connection.
	 */
	public void setTimeout (int timeout) throws IOException {

		this.timeout = timeout;

		if (connection != null) {
			connection.setTimeout(timeout);
		}
	}


	/**
	 * Connect to the POP3 server (on port 110).
	 *
	 * @return true on a successful connection
	 * @exception IOException when an error occurs connecting.
	 */
	public synchronized boolean connect () throws IOException {

		return (connect(host));
	}


	/**
	 * Connect to the specified POP3 server (on port 110).
	 *
	 * @param  host  hostname of the POP3 server.
	 * @return true on a successful connection
	 * @exception  IOException when an error occurs connecting.
	 */
	public synchronized boolean connect (String host) throws IOException {

		/*
		 * Are we already connected?
		 */
		if (connection != null) {
			return (true);
		}

		/*
		 * Gets a connection to the POP3 server
		 */
		Connection connection;
		try {

			connection = Connection.getInstance();

		} catch (Exception e) {

			IOException ioe = new IOException();
			ioe.fillInStackTrace();
			throw (ioe);
		}

		connection.open(host, POP3_PORT);
		connection.setTimeout(timeout);

		/*
		 * Gets an input stream to read data from the server, and
		 * an output stream to write data to the server.
		 */
		POP3Reader in = new POP3Reader(connection.getInputStream());
		PrintWriter out = new PrintWriter(connection.getOutputStream(), true);

	    
		/*
		 * Save all the just created objects we do this so
		 * exception handling is streamlined
		 */
		this.in = in;
		this.out = out;


		/*
		 * When a client connects to a POP3 server the server
		 * sends a message back. See if all is ok.
		 */
		response = getResponse();
		if (response.isOk()) {

			/*
			 * We are now in the AUTHORIZATION state
			 */
			state = AUTHORIZATION;

			return (true);

		} else {

			return (false);
		}
	}


	/**
	 * Disconnect from the POP3 server.
	 */
	public void disconnect () throws IOException {

		if (connection != null) {
			try {
		        	connection.close();
			} catch (IOException ioe) {
				throw (IOException)ioe.fillInStackTrace();
			} finally {
		  		state = DISCONNECTED;
				connection = null;
				in = null;
				out = null;
			}
		}
	}



	/**
	 * Login the specified user with the specified password.
	 *
	 * Login to the POP3 server with the given username and password.
	 * You must first connect to the server with connect before attempting
	 * to login. A login attempt is only valid if the client is in the
   	 * AUTHORIZATION state. After logging in, the client enters the 
	 * TRANSACTION state. 
	 *
	 * @param username  The account name being logged in to. 
     * @param password  The plain text password of the account. 
	 * @return true on a successful login
	 *
	 * @exception IOException  If a network I/O error occurs
	 * @exception POP3Exception  If not currently in AUTHORIZATION state
	 * or login is unsuccessful
	 */
	public synchronized boolean login (String username, String password)
		throws IOException, POP3Exception {

		if (state != AUTHORIZATION) {
			throw new POP3Exception("not in AUTHORIZATION state");
		}

	  	sendCommand("USER " + username);
	  	response = getResponse();
		if (!response.isOk()) {
			throw new POP3Exception(response.getResponse());
		}
			
		sendCommand("PASS " + password);
	  	response = getResponse();
		if (response.isOk()) {
			state = TRANSACTION;
			return (true);
		} else {
			throw new POP3Exception(response.getResponse());
		}
	}


	/**
	 * Logout from the POP3 server. 
	 *
	 * @return true on a successful logout
	 * @exception IOException  If a network I/O error occurs in the
	 * process of logging out. 
	 * @exception POP3Exception  when a POP3 protocol error occurs.
	 */
	public synchronized boolean logout ()
		throws IOException, POP3Exception {

	  	sendCommand("QUIT");
	  	response = getResponse();
		if (state == TRANSACTION) {
			state = UPDATE;
			return (true);
		} else {
			return (false);
		}
	}


	/**
	 * Gets the mailbox status. A status attempt can only succeed if
	 * the client is in the TRANSACTION state . Returns a POP3MailboxInfo
	 * instance containing the number of messages in the mailbox and the
	 * total size of the messages in bytes. Returns null if the status 
	 * the attempt fails. 
	 *
	 * @return A POP3MailboxInfo instance containing the number of
	 * messages in the mailbox and the total size of the messages in bytes.
	 * Returns null if the status the attempt fails. 
	 *
	 * @exception POP3Exception If not in the POP3 TRANSACTION state.
	 * @exception IOException If a network I/O error occurs in the
	 * process of sending the command.  
	 */
	public synchronized POP3MailboxInfo status ()
		throws IOException, POP3Exception {

		if (state != TRANSACTION) {
			throw new POP3Exception("not in TRANSACTION state");
		}

  		sendCommand("STAT");
	  	response = getResponse();
		if (!response.isOk()) {
			return (null);
		}
		
		int num_messages = response.getIntToken(1);
		int size = response.getIntToken(2);
		return (new POP3MailboxInfo(num_messages, size));
	}



	/**
	 * List information for specified message.
	 *
	 * @param  message_id  The message id of the message to list.
	 * @return the information for specified message
	 *
	 * @exception POP3Exception If not in the POP3 TRANSACTION state.
	 * @exception IOException If a network I/O error occurs in the
	 * process of sending the command.  
	 */
	public synchronized POP3MessageInfo listMessage (int message_id)
		throws IOException, POP3Exception {

		if (state != TRANSACTION) {
			throw new POP3Exception("not in TRANSACTION state");
		}

		sendCommand("LIST " + message_id);
	  	response = getResponse();

		int message_num = response.getIntToken(1);
		int size = response.getIntToken(2);
		return (new POP3MessageInfo(message_num, size));
	}
			


	/**
	 * List information for all messages on the server.
	 *
	 * @return list of information for all messages on the server
	 * @exception POP3Exception If not in the POP3 TRANSACTION state.
	 * @exception IOException If a network I/O error occurs in the
	 * process of sending the command.  
	 */
	public synchronized POP3MessageInfo[] listMessages () 
		throws IOException, POP3Exception {

		if (state != TRANSACTION) {
			throw new POP3Exception("not in TRANSACTION state");
		}

  		sendCommand("LIST");
	  	response = getResponses();
		if (!response.isOk()) {
			return (null);
		}

		String responses[] = response.getMultipleResponses();
		POP3MessageInfo msgInfo[] =
			new POP3MessageInfo[responses.length];
		for (int i = 0; i < responses.length; i++) {

			StringTokenizer st = new StringTokenizer(responses[i]);
			if (st.countTokens() < 2) {
				throw new POP3Exception(
					"malformed LIST response");
			}

			String num  = st.nextToken();
			String size = st.nextToken();
			msgInfo[i] = new POP3MessageInfo(
				Integer.parseInt(num),
				Integer.parseInt(size));
		}

		return (msgInfo);
	}


	/**
	 * Gets the uidl of the specified mail message.
	 *
	 * @param  msgnum   the message number of the message to retrieve
	 * the uidl for
	 * @return the uidl of the specified mail message
	 * @exception POP3Exception If not in the POP3 TRANSACTION state.
	 * @exception IOException If a network I/O error occurs in the
	 * process of sending the command.  
	 */
	public synchronized POP3MessageInfo listUniqueIdentifier (int msgnum)
		throws IOException, POP3Exception {

		if (state != TRANSACTION) {
			throw new POP3Exception("not in TRANSACTION state");
		}

		sendCommand("UIDL " + msgnum);
  		response = getResponse();
		if (!response.isOk()) {
			return (null);
		}

		int message_num = response.getIntToken(1);
		String identifier = response.getToken(2);
		return (new POP3MessageInfo(message_num, identifier));
	}



	/**
	 * Gets the uidl of all the messages.
	 *
	 * @return the uidl of all the messages
	 * @exception POP3Exception If not in the POP3 TRANSACTION state.
	 * @exception IOException If a network I/O error occurs in the
	 * process of sending the command.  
	 */
	public synchronized POP3MessageInfo[] listUniqueIdentifiers ()
		throws IOException, POP3Exception {

		if (state != TRANSACTION) {
			throw new POP3Exception("not in TRANSACTION state");
		}

	  	sendCommand("UIDL");
	  	response = getResponses();
		if (!response.isOk()) {
			return (null);
		}

		String responses[] = response.getMultipleResponses();
		POP3MessageInfo msgInfo[] =
			new POP3MessageInfo[responses.length];
		for (int i = 0; i < responses.length; i++) {

			StringTokenizer st = new StringTokenizer(responses[i]);
			if (st.countTokens() < 2) {
				throw new POP3Exception(
					"malformed LIST response");
			}

			String num = st.nextToken();
			String uid = st.nextToken();
			msgInfo[i] = new POP3MessageInfo(
				Integer.parseInt(num), uid);
		}

		return (msgInfo);
	}



	/**
	 * Gets the contents of the message from the server.
	 *
	 * @param  msgInfo   the message <code>POP3MessageInfo</code> object 
	 * for the message to retrieve.
	 * @return the contents of the message from the server
	 * @exception POP3Exception If not in the POP3 TRANSACTION state.
	 * @exception IOException If a network I/O error occurs in the
	 * process of sending the command.  
	 */
	public synchronized POP3MailMessage retrieveMessage (
		POP3MessageInfo msgInfo)
		throws IOException, POP3Exception {

		getMessageReader(msgInfo.getMessageNumber());
		POP3MailMessage msg = new POP3MailMessage(this, msgInfo);
		return (msg.read());
	}


	/**
	 * Gets the contents of the message from the server.
	 *
	 * @param  msgnum   the message number of the message to retrieve.
	 * @return the contents of the message from the server
	 * @exception POP3Exception If not in the POP3 TRANSACTION state.
	 * @exception IOException If a network I/O error occurs in the
	 * process of sending the command.  
	 */
	public synchronized POP3MailMessage retrieveMessage (int msgnum) 
		throws IOException, POP3Exception {

		getMessageReader(msgnum);
		POP3MailMessage msg = new POP3MailMessage(this);
		return (msg.read());
	}
		



	/**
	 * Gets a stream to read the contents of the message from the server.
	 * Does not actually read the message, rather it returns a stream to
	 * read it with.
	 *
	 * @param  msgnum   the message number of the message to get the
	 * stream reader for.
	 * @return a stream to read the contents of the message from the server
	 * @exception POP3Exception If not in the POP3 TRANSACTION state.
	 * @exception IOException If a network I/O error occurs in the
	 * process of sending the command.  
	 */
	public synchronized POP3Reader getMessageReader (int msgnum) 
		throws IOException, POP3Exception {

		if (state != TRANSACTION) {
			throw new POP3Exception("not in TRANSACTION state");
		}

  		sendCommand("RETR " + msgnum);
  		response = getResponse();
		if (!response.isOk()) {
			throw new POP3Exception("Bad response: " +
				response.getResponse());
		}

		return (in);
	}


	/**
	 * Gets a stream to read the contents of the message from the server.
	 * Does not actually read the message, rather it returns a stream to
	 * read it with.
	 *
	 * @param  msgInfo   the message <code>POP3MessageInfo</code> object 
	 * for the message to get the stream reader for.
	 * @return a stream to read the contents of the message from the server
	 * @exception POP3Exception If not in the POP3 TRANSACTION state.
	 * @exception IOException If a network I/O error occurs in the
	 * process of sending the command.  
	 */
	public synchronized POP3Reader getMessageReader (POP3MessageInfo msgInfo)
			throws IOException, POP3Exception {

		return (getMessageReader(msgInfo.getMessageNumber()));
	}


	/**
	 * Gets the top n lines of a mail message<br>
	 *
	 * The array of strings obtained are the lines of the
	 * mail headers and the top N lines of the indicated mail msg.
	 * The lines have CR/LF striped, any leading "." fixed up
	 * and the ending "." removed.
	 *
	 * @return the header lines
	 * @exception POP3Exception If not in the POP3 TRANSACTION state.
	 * @exception IOException If a network I/O error occurs in the
	 * process of sending the command.  
	 */
	public synchronized String [] retrieveMessageTop (int msgnum, int n) 
		throws IOException, POP3Exception {

		if (state != TRANSACTION) {
			throw new POP3Exception("not in TRANSACTION state");
		}

	  	sendCommand("TOP " + msgnum + " " + n);
	  	response = getResponses();

		return (response.getMultipleResponses());
	}


	/**
	 * Mark the mail message for deletion. Mail message will be
	 * deleted when QUIT is issued.
	 *
	 * @return true if message was deleted
	 * @exception POP3Exception If not in the POP3 TRANSACTION state.
	 * @exception IOException If a network I/O error occurs in the
	 * process of sending the command.  
	 */
	public synchronized boolean deleteMessage (POP3MessageInfo msg)
		throws IOException, POP3Exception {

		return (deleteMessage(msg.getMessageNumber()));
	}


	/**
	 * Mark the mail message for deletion. Mail message will be
	 * deleted when QUIT is issued.
	 *
	 * @return true if message delete mark was successful
	 * @exception POP3Exception If not in the POP3 TRANSACTION state.
	 * @exception IOException If a network I/O error occurs in the
	 * process of sending the command.  
	 */
	public synchronized boolean deleteMessage (int msgnum)
		throws IOException, POP3Exception {

		if (state != TRANSACTION) {
			throw new POP3Exception("not in TRANSACTION state");
		}

	  	sendCommand("DELE " + msgnum);
		response = getResponse();
		return (response.isOk());
	}


	/**
	 * Reset the mail messages that have been marked for deletion.
	 * Nothing will be deleted if QUIT is issued next.
	 *
	 * @return true if command was successfully sent
	 * @exception POP3Exception If not in the POP3 TRANSACTION state.
	 * @exception IOException If a network I/O error occurs in the
	 * process of sending the command.  
	 */
	public synchronized boolean reset () 
		throws IOException, POP3Exception {

		if (state != TRANSACTION) {
			throw new POP3Exception("not in TRANSACTION state");
		}


	  	sendCommand("RSET");
		response = getResponse();
		return (response.isOk());
	}


	/**
	 * Does not do anything but it will keep the server active.
	 *
	 * @return true if command was successfully sent
	 * @exception POP3Exception If not in the POP3 TRANSACTION state.
	 * @exception IOException If a network I/O error occurs in the
	 * process of sending the command.  
	 */
	public synchronized boolean noop ()
		throws IOException, POP3Exception {

		if (state != TRANSACTION) {
			throw new POP3Exception("not in TRANSACTION state");
		}

	  	sendCommand("NOOP");
	  	response = getResponse();
		return (response.isOk());
	}


	/**
	 * Send the passed command to the Server.
	 *
	 * @exception IOException If a network I/O error occurs in the
	 * process of sending the command.  
	 */
	protected void sendCommand (String command) throws IOException {

		if (state == DISCONNECTED) {
			throw new IOException("not connected");
		}

		if (command.startsWith("PASS")) {
			fireMailStatusEvent(
				MailStatusEvent.COMMAND, "PASS ********");
		} else {
			fireMailStatusEvent(MailStatusEvent.COMMAND, command);
		}

		out.println(command);
		lastCommand = command;
	}


	/**
	 * Gets the next response to a previously sent command from the server.
	 *
	 * @return the response to a previously sent command from the server
	 * @exception IOException If a network I/O error occurs reading
	 * the response from the server.
	 */
	POP3Response getResponse () throws IOException {

		String line = in.readLine();
		if (line == null) {
			throw new IOException(
				"no data while reading POP3 response");
		}

		fireMailStatusEvent(MailStatusEvent.RESPONSE, line);
		return (new POP3Response(line));
	}
	


	/**
	 * Gets the responses to a previously sent command from the server.
	 * This is used when more than one line is expected. The last line
	 * of output should be ".\r\n"
	 *
	 * @return the responses to a previously sent command from the server
	 * @exception IOException If a network I/O error occurs reading
	 * the response from the server.
	 */
	POP3Response getResponses () throws IOException {
        
		String line = in.readLine();
		fireMailStatusEvent(MailStatusEvent.RESPONSE, line);

		line = line.trim();
		if (!line.startsWith("+OK")) {
			return (new POP3Response(line));
		}
    	      
		ArrayList<String> v = new ArrayList<String>();
		v.add(line);
		while (true) {
			line = in.readLine();
			if (line == null) {
				break;
			}

			fireMailStatusEvent(MailStatusEvent.RESPONSE, line);
			v.add(line);
        }

		String responses[] = new String[v.size()];
		v.toArray(responses);
		return (new POP3Response(responses));
	}


	/**
	 * Add a POP3 event listener.
	 *
	 * @param  l  The POP3StatusListener to add.
	 */
	public synchronized void addStatusListener (POP3StatusListener l) {

		if (!pop3Listeners.contains(l)) {
	   		pop3Listeners.add(l);
		}
	}


	/**
	 * Remove a POP3 event listener.
	 *
	 * @param  l  The POP3StatusListener to remove.
	 */
	public synchronized void removeStatusListener (POP3StatusListener l) {

		if (!pop3Listeners.contains(l)) {
	   		pop3Listeners.remove(l);
		}
	}


	/**
	 * Notify listening objects of mail progress events.
	 *
	 * @param   bytes_read  The number of bytes read for this event.
	 * @param   total  The total number of bytes read.
	 */
	public void fireMailProgressEvent (
		int bytes_read, int total, int msg_size) {

		/*
		 * Create the event object
		 */
		MailStatusEvent evt = new MailStatusEvent(
			this, bytes_read, total, msg_size);

		/*
		 * Make a copy of the listener object vector so that it cannot
		 * be changed while we are firing events
		 */
		ArrayList v;
		synchronized(this) {
			if (pop3Listeners.size() < 1) {
				return;
			}

			v = (ArrayList) pop3Listeners.clone();
	  	}



	  	/*
		 * Fire the event to all listeners
		 */
	  	int count = v.size();
	  	for (int i = 0; i < count; i++) {
			POP3StatusListener l =
				(POP3StatusListener)v.get(i);
			l.pop3Status(evt);
	  	}
	}


	/**
	 * Notify listening objects of POP3 events.
	 *
	 * @param  type     The type of POP3 event (COMMAND | RESPONSE)
	 * @param  message  The POP3 command or response message.
	 */
	public void fireMailStatusEvent (int type, String message) {

		/*
		 * Make a copy of the listener object vector so that it cannot
		 * be changed while we are firing events
		 */
		ArrayList<POP3StatusListener> v;
		synchronized(this) {
			if (pop3Listeners.size() < 1) {
				return;
			}

			v = new ArrayList<POP3StatusListener>(pop3Listeners.size());
			for (POP3StatusListener l : pop3Listeners) {
				v.add(l);
			}
	  	}


		/*
		 * Create the event object
		 */
		MailStatusEvent evt = new MailStatusEvent(this, type, message);


	  	/*
		 * Fire the event to all listeners
		 */
	  	int count = v.size();
	  	for (int i = 0; i < count; i++) {
			POP3StatusListener l = v.get(i);
			l.pop3Status(evt);
	  	}
	}
}

