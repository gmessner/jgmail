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

import com.messners.mail.*;

/**
 * This class tests the com.messners.mail.POP3 mail classes.
 *
 * @version $Revision: 1.1 $
 */
class POP3Client implements POP3StatusListener {

	public static void main (String args[]) {

		/*
		 * Get the command line parameters
		 */
		if (args.length != 3) {
			System.err.println(
				"usage: POP3Client host username password");
			System.exit(1);
		}

		String host = args[0];
		String username = args[1];
		String password = args[2];

	   	try {
			POP3 pop3 = new POP3();

			/*
			 * Set up a listener for the POP3 commands and responses
			 */
			pop3.addStatusListener(new POP3Client());

			/*
			 * Connect and login to the POP3 server
			 */
			pop3.connect(host);
			pop3.login(username, password);

			/*
			 * Get the mailbox status (returns an object
			 * which contains the count of messages and the
			 * size in bytes of the mailbox)
			 */
			POP3MailboxInfo mailbox_info = pop3.status();

			if (mailbox_info.getMessageCount() < 1) {
				System.out.println("No messages on server");
				System.exit(0);
			}


			/*
			 * Get the information on the messages and retrieve the
			 * content of each message.
			 */
			POP3MessageInfo msg_info[] = pop3.listMessages();

			for (int i = 0; i < msg_info.length; i++) {

				POP3MailMessage msg = 
					pop3.retrieveMessage(msg_info[i]);

				System.out.println("\nMessage: " + i);
				dump(msg);

			}

			/*
			 * Logout and disconnect from the POP3 server
			 */
			pop3.logout();
			pop3.disconnect();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	static void dump (POP3MailMessage msg) throws Exception {

		msg.dumpHeader();
		MessagePart msgs[] = msg.getBody();
		for (int i = 0; i < msgs.length; i++) {
			System.out.println("");
			System.out.println("content-type: " +
				msgs[i].getContentType());

			if (msgs[i].getType() == MessagePart.TEXT) {
				System.out.println("charset: " +
					msgs[i].getCharset());
				System.out.println(msgs[i].getText());
			} else {
				System.out.println("name: " +
					msgs[i].getName());
				System.out.println("filename: " +
					msgs[i].getFilename());
				System.out.println("encoding: " +
					msgs[i].getEncoding());
			}
		}
	}

	/*
	 * This class simply implements the pop3Status() method 
	 * for MailStatusEvents
	 */ 
	private POP3Client () {
	}

	/*
	 * This method is the method called when commands are sent to and 
	 * responses read from the POP3 server.
	 */
	int last_percent = -1;
	public void pop3Status (MailStatusEvent evt) {

		if (evt.getType() == MailStatusEvent.PROGRESS) {
			int percent = (evt.getTotalBytes() * 100 + 99) / 
				evt.getMessageSize();
			if (last_percent != percent) {
				last_percent = percent;
				if (percent > 99) {
					System.err.println(".");
				} else {
					System.err.print(".");
				}
			}
		} else if (evt.getType() == MailStatusEvent.COMMAND) {
			System.err.println("<-- " + evt.getMessage());
		} else {
			System.err.println("--> " + evt.getMessage());
		}
	}
}



