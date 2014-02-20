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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;

/**
 * The <code>SMTP</code> class implements the Simple Mail Transfer Protocol
 * described in RFC-821 and the message format decribed in RFC-822. It also
 * provides support for attaching files to the message in accordance with
 * RFC-2045.
 *
 * @author  Gregory M. Messner <gmessner@messners.com>
 * @version $Revision: 1.5 $
 */

public class SMTP {

	/**
	 * Default SMTP server port (as per RFC-821)
	 */
	public static final int SMTP_PORT = 25;

	/**
	 * Specifis that no authorization is accomplished when connecting.
	 */
	public static final int AUTH_NONE  = 0;

	/**
	 * Specifis that "PLAIN" authorization is accomplished when connecting.
	 */
	public static final int AUTH_PLAIN = 1;

	/**
	 * Specifis that "LOGIN" authorization is accomplished when connecting.
	 */
	public static final int AUTH_LOGIN = 2;


	protected static final String CRLF = "\r\n";
	
	protected String host = "localhost";
	protected int timeoutMsecs = 20000;
	
	protected String charset = "us-ascii";

	protected MailAddress from   = new MailAddress();
	protected MailAddress sender = new MailAddress();
	protected String senderHost  = Utilities.getLocalHostAddress();
  
	protected String subject = "";
  	protected String mailer  = "JgMail: 3.0";

	protected int authType = AUTH_NONE;
	protected String username = null;
	protected char password[] = null;

	protected ArrayList<MailAddress> toList      = new ArrayList<MailAddress>();
	protected ArrayList<MailAddress> ccList      = new ArrayList<MailAddress>();
	protected ArrayList<MailAddress> bccList     = new ArrayList<MailAddress>();
	protected ArrayList<MailAddress> replyToList = new ArrayList<MailAddress>();

	protected StringBuffer extraHeader = new StringBuffer();
	protected StringBuffer body        = new StringBuffer();
	protected ArrayList<Attachment> attachments = new ArrayList<Attachment>();

	protected String lastResponse = null;


	/**
	 * Collection of objects listening for SMTP events.
	 */
	protected ArrayList<SMTPStatusListener> smtpListeners =
			new ArrayList<SMTPStatusListener>();


	/**
	 * Creates a <code>SMTP</code> instance ready to send mail.
	 */
	public SMTP () {
	}


	/**
	 * Creates a <code>SMTP</code> instance ready to send mail to the
	 * specified SMTP host.
	 *
	 * @param host     the host name of the SMTP server.
	 */
	public SMTP (String host) {
		this.host = host;
	}


	/**
	 * Gets the mailer string. The mailer string is put in
	 * the message header field "X-Mailer". By default is
	 * "JgMail: current-version". Use <code>setMailer()</code>
	 * to modify.
	 */
	public String getMailer () {
		return (mailer);
	}


	/**
	 * Set the mailer string. The mailer string is put in
	 * the message header field "X-Mailer". 
	 */
	public void setMailer (String mailer) {
		this.mailer = mailer;
	}


	/**
	 * Sets the SMTP sender host. This is used as the host name
	 * with the HELO command.
	 *
	 * @param host     the host name of the sender.
	 */
	public void setSenderHost (String host) {
		senderHost = host;
	}


	/**
	 * Gets the SMTP sender host. This is used as the host name
	 * with the HELO command.
	 */
	public String getSenderHost () {
		return (senderHost);
	}


	/**
	 * Gets the host name of the SMTP server to connect to.
	 */
	public String getHost () {
		return (host);
	}


	/**
	 * Sets the host name of the SMTP server to connect to.
	 *
	 * @param host     the host name of the SMTP server.
	 */
	public void setHost (String host) {
		this.host = host;
	}


	/**
	 * Gets the AUTH type for the SMTP transaction.
	 *
	 * @return the AUTH type
	 */
	public int getAuthType () {
		return (authType);
	}


	/**
	 * Sets the AUTH type for the SMTP transaction. Must be AUTH_NONE,
	 * AUTH_PLAIN, or AUTH_LOGIN.
	 *
	 * @param authType  the new AUTH type
	 */
	public void setAuthType (int authType) {
		this.authType = authType;
	}


	/**
	 * Gets the username to use with the AUTH command.
	 *
	 * @return the AUTH username
	 */
	public String getUserName () {
		return (username);
	}


	/**
	 * Sets the username to use with the AUTH command.
	 *
	 * @param username the AUTH username
	 */
	public void setUserName (String username) {
		this.username = username;
	}


	/**
	 * Gets the password to use with the AUTH command.
	 *
	 * @return the AUTH password
	 */
	public char[] getPassword () {
		return (password);
	}


	/**
	 * Sets the password to use with the AUTH command.
	 *
	 * @param password the AUTH password 
	 */
	public void setPassword (char password[]) {
		this.password = password;
	}


	/**
	 * Gets the socket timeout in milliseconds. 
	 *
	 * @return the socket timeout in milliseconds
	 */
	public int getTimeout () {
		return (timeoutMsecs);
	}


	/**
	 * Sets the socket timeout. 
	 *
	 * @param  msecs  the number of milliseconds for the timeout,
	 * -1 will disable the timeout
	 */
	public void setTimeout (int msecs) {
		timeoutMsecs = msecs;
	}


	/**
	 * Sets the character set string.
	 *
	 * @param  charset  new character set string.
	 */
	public void setCharset (String charset) {
		this.charset = charset;
	}


	/**
	 * Gets the character set string.
	 *
	 * @return the character set String
	 */
	public String getCharset () {
		return (charset);
	}


	/**
	 * Gets the last response recieved from the SMTP server.
	 *
	 * @return the last response recieved from the SMTP server
	 */
	public String getLastResponse () {
		return (lastResponse);
	}


	/**
	 * Sets the From entry in the header.
	 *
	 * @param fullname   the full name of the user
	 * @param address    the address of the user
	 */
	public void setFrom (String fullname, String address) {
		from.setFullname(fullname);
		from.setAddress(address);
	}


	/**
	 * Reset the contents of the message. Do this prior to
	 * Sending the next message.
	 */
	public void resetMessage () {

		subject = "";

		clearRecipients();
		clearReplyTo();
		clearHeaders();
		clearBody();
		clearAttachments();

		lastResponse = null;
	}


	/**
	 * Clear the all the message headers.
	 */
	public void clearHeaders () {
		extraHeader.setLength(0);
	}


	/**
	 * Clear the all the recipients lists (To, Cc, Bcc).
	 */
	public void clearRecipients () {
		clearToRecipients();
		clearCcRecipients();
		clearBccRecipients();
	}


	/**
	 * Clear the "to" recipients list.
	 */
	public void clearToRecipients () {
		toList.clear();
	}


	/**
	 * Clear the "cc" recipients list.
	 */
	public void clearCcRecipients () {
		ccList.clear();
	}


	/**
	 * Clear the "bcc" recipients list.
	 */
	public void clearBccRecipients () {
		bccList.clear();
	}


	/**
	 * Clear the reply to list from the message.
	 */
	public void clearReplyTo () {
		attachments.clear();
	}


	/**
	 * Clear the all the attachments from the message.
	 */
	public void clearAttachments () {
		attachments.clear();
	}


	/**
	 * Clear the body (text) of the message.
	 */
	public void clearBody () {
		body.setLength(0);
	}

		
	/**
	 * Sets the To section. Can be called multiple times to send
	 * to multiple recipients.
	 *
	 * @param  fullname   the full name of the user
	 * @param  address    the address of the user
	 * @return true if the address was successfully added
	 */
	public boolean addTo (String fullname, String address) {

		MailAddress entry = new MailAddress(fullname, address);
		if (entry.isValid()) {
			toList.add(entry);
			return (true);
		} else {
			return (false);
		}
  	}


	/**
	 * Adds a recipient to the CC list. Can be called multiple times
	 * to send mail to multiple recipients.
	 *
	 * @param  fullname   The full name of the user
	 * @param  address    The address of the user
	 * @return true if the address was successfully added
	 */
	public boolean addCc (String fullname, String address) {
		MailAddress entry = new MailAddress(fullname, address);
		if (entry.isValid()) {
			ccList.add(entry) ;
			return (true);
		} else {
			return (false);
		}
  	}


	/**
	 * Adds a recipient to the BCC list. Can be called multiple times
	 * to send mail to multiple recipients.
	 *
	 * @param  fullname   the full name of the user
	 * @param  address    the address of the user
	 * @return true if the address was successfully added
	 */
	public boolean addBcc (String fullname, String address) {
		MailAddress entry = new MailAddress(fullname, address);
		if (entry.isValid()) {
			bccList.add(entry);
			return (true);
		} else {
			return (false);
		}
	}


	/**
	 * Sets the Reply-To adresses. Can be called multiple times to set
	 * the Reply-To to multiple addresses.
	 *
	 * @param fullname   the full name of the user
	 * @param address    the address of the user
	 */
	public void addReplyTo (String fullname, String address) {   
		MailAddress entry = new MailAddress(fullname, address);
		replyToList.add(entry) ;
  	}


	/**
	 * Sets additional fields into the message header. See RFC822.
	 * Examples follow:<br>
	 * <pre>
	 *    Field name         Value format
	 *    -----------------  ----------------------------------
	 *    Comments           *text
	 *    Encrypted          1#2word
	 *    Resent-Date        date-time     
	 *    Resent-To          1#address     
	 *    Resent-cc          1#address      
	 *    Resent-bcc         #address
	 *    Message-ID         msg-id (msg-id="<" addr-spec ">")
	 *    Resent-Message-ID  msg-id
	 *    References         *(phrase/msg-id)
	 *    Keywords           #phrase
	 * </pre>
	 *
	 * @param  name   name of field to add to the header
	 & @param  value  value of field to add to header
	 */
	public void addHeaderField (String name, String value) {
		extraHeader.append(name + ": " + value + CRLF);
	}


	/**
	 * Sets the Subject header field.  Subsequent calls will override.
	 *
	 * @param subject     the Subject
	 */
	public void setSubject (String subject) {
    		this.subject = subject;
	}


	/**
	 * Sets the body of the message.
	 *
	 * @param  body  the string to set as the body of the message
	 */
	public void setBody (String body) {
		this.body.setLength(0);
		this.body.append(body);
	}


	/**
	 * Appends to the body of the message.
	 *
	 * @param  text  the string to append to the message body
	 */
	public void addBody (String text) {
		body.append(text);
	}


	/**
	 * Appends the contents of the file to the body of the message.
	 *
	 * @param  filename  the name of the file
	 * @exception FileNotFoundException when the filename doesn't exist
	 * @exception SecurityException when the file is not readable
	 */
	public void addBodyFromFile (String filename) throws IOException {

		/*
		 * First verify that the file exists and is readable
		 */
		File f = new File(filename);
		if (!f.exists()) {
			throw new FileNotFoundException();
		}

		if (!f.canRead()) {
			throw new SecurityException("file not readable");
		}

		/*
		 * Now read the file and append iot to the body buffer
		 */
		BufferedReader in = new BufferedReader(new FileReader(f));
		String line;
		while ((line = in.readLine()) != null) {
	  		body.append(line + CRLF);
		}

		in.close();
	}


	/**
	 * Adds the specified Attachment instance to the attachments list.
	 *
	 * @param attachment the Attachment instance to add
	 */
	public void addAttachment (Attachment attachment) {
		attachments.add(attachment);
	}


	/**
	 * Attaches the stream to the message using MIME and base64 encoding.
	 *
	 * @param  stream  the InputStream to add as an attachment
	 */
	public void addAttachment (InputStream stream) {
		addAttachment(new Attachment(null, null, stream));
	}


	/**
	 * Attaches the stream to the message using MIME and base64 encoding.
	 *
	 * @param  stream  the InputStream to add as an attachment
	 */
	public void addAttachment (InputStream stream, String type) {
		addAttachment(new Attachment(null, type, stream));
	}
	

	/**     
	 * Attaches the contents of the URL to the message using 
	 * MIME and base64 encoding.
	 *
	 * @param  url  the URL to add as an attachment
	 */
	public void addAttachment (URL url) {
		addAttachment(new Attachment(null, null, url));
	}


	/**     
	 * Attaches the contents of the URL to the message using 
	 * MIME and base64 encoding.
	 *
	 * @param  url  the URL to add as an attachment
	 * @param  type the MIME type for the attachment
	 */
	public void addAttachment (URL url, String type) {

		if (url != null) {
			addAttachment(new Attachment(null, type, url));
		}
	}


	/**
	 * Attaches the file to the message using MIME and base64 encoding.
	 *
	 * @param  filename  the name of the file to attach.
	 * @exception FileNotFoundException when the filename doesn't exist.
	 * @exception SecurityException wheb the file is not readable.
	 */
	public void addAttachment (String filename) throws IOException {
		addAttachment(filename, null);
	}



	/**
	 * Attaches the file to the message using MIME and base64 encoding.
	 *
	 * @param  filename  the name of the file to attach.
	 * @param  type      the MIME type for the attachment
	 * @exception FileNotFoundException when the filename doesn't exist.
	 * @exception SecurityException wheb the file is not readable.
	 */
	public void addAttachment (String filename, String type) 
			throws IOException {

		File f = new File(filename);
		addAttachment(f, type);
	}


	/**
	 * Attaches the file to the message using MIME and base64 encoding.
	 *
	 * @param  f  the File instance to attach
	 * @exception FileNotFoundException when the filename doesn't exist
	 * @exception SecurityException wheb the file is not readable
	 */
	public void addAttachment (File f) throws IOException {
		addAttachment(f, null);
	}


	/**
	 * Attaches the file to the message using MIME and base64 encoding.
	 *
	 * @param  f  the File instance to attach
	 * @param  type  the MIME type for the attachment
	 * @exception FileNotFoundException when the filename doesn't exist
	 * @exception SecurityException wheb the file is not readable
	 */
	public void addAttachment (File f, String type) throws IOException {

		/*
		 * First verify that the file exists and is readable
		 */
		if (!f.exists()) {
			throw new FileNotFoundException();
		}

		if (!f.canRead()) {
			throw new SecurityException("file not readable");
		}

		/*
		 * Now add this file to the list of attachments
		 */
		attachments.add(new Attachment(null, type, f));
	}


	/**
	 * Reads the response from the server and checks that it starts with
	 * the expected response code.
	 *
	 * @param  expected   the expected response code
	 *
	 * @exception IOException if an IO error occurs while reading the socket
	 * @exception SMTPException when the server does not return the
	 * expected response code.
	 */
	private void readAndCheck (String expected, BufferedReader in)
		throws IOException, SMTPException {

		while (true) {

			lastResponse = in.readLine();
			if (lastResponse == null) {
			   	throw new SMTPException(
					"empty response from server");
			}

			fireMailStatusEvent(
				MailStatusEvent.RESPONSE, lastResponse);
		
			if (lastResponse.length() < 4) {
			   	throw new SMTPException(
					"malformed response from server");
			} else if (!lastResponse.startsWith(expected)) {
			   	throw new SMTPException(
					"got response code \"" + 
					lastResponse.substring(0, 3) + "\" " +
					"expected \"" + expected + "\"");
			}

			/*
			 * Continue reading until a space follows the
			 * numeric code
			 */
			if (lastResponse.charAt(3) == ' ') {
				break;
			}
		}
	}


	/**
	 * Appends the specified recipients to the message header buffer.
	 */
	private void appendAddress (String type, ArrayList addresses, 
		StringBuffer hdr) {

		int num_addresses = addresses.size();
		if (num_addresses < 1) {
			return;
		}

		hdr.append(CRLF + type + ": ");
		for (int i = 0; i < num_addresses; i++) {
			MailAddress ma = (MailAddress)addresses.get(i);
			String address = ma.getFullAddress();
			if (address.length() < 1) {
				break;
			}
			if (i > 0) {
				hdr.append(", ");
			}

			hdr.append(ma.getFullAddress());
		}
	}


	/**
	 * Outputs the message header data.
	 *
	 * @exception  IOException    when an I/O errors happen on the socket
	 * @exception  SMTPException  when an SMTP specific error occurs
	 */
	private void outputHeader (PrintWriter out, BufferedReader in)
		throws IOException, SMTPException {

		/*
		 * Build the standard header and output it
		 */
		StringBuffer hdr = new StringBuffer();

		if (sender.getAddress() != null) {
			hdr.append("X-Sender: " + sender.getAddress() + CRLF);
		}

		if (mailer != null) {
			hdr.append("X-Mailer: " + mailer + CRLF);
		}

		hdr.append("Date: " + Utilities.getFormattedDate(new java.util.Date()));

		appendAddress("To", toList, hdr);
		appendAddress("CC", ccList, hdr);
		appendAddress("BCC", bccList, hdr);
//		appendAddress("Reply-To", replyToList, hdr);

		hdr.append(CRLF + "From: " + from.getFullAddress());
		hdr.append(CRLF + "Subject: " + subject);

		out.write(hdr.toString());

		/*
		 * Output any extra header fields
		 */
		String headers = extraHeader.toString();
		if (headers.length() > 0) {
			out.write(CRLF + headers);
		}

		out.flush();
	}


	/**
	 * Send the RCPT TO: command for the specified recipients.
	 *
	 * @exception  IOException    when an I/O errors happen on the socket
	 * @exception  SMTPException  when an SMTP specific error occurs
	 */
	private void outputRecipients (ArrayList addresses, PrintWriter out, 
		BufferedReader in) throws IOException, SMTPException {

		for (int i = 0; i < addresses.size(); i++) {
			MailAddress ma = (MailAddress)addresses.get(i);
			send(out, "RCPT TO: " + ma.getPathAddress());
			readAndCheck("250", in);
		}
	}


	/**
	 * Send a command to the server.
	 *
	 * @param  out      the PrintWriter to send the command on.
	 * @param  command  the command to send.
	 *
	 * @exception  IOException    when an I/O errors happen on the socket
	 * @exception  SMTPException  when an SMTP specific error occurs
	 */
	private void send (PrintWriter out, String command) 
		throws IOException, SMTPException {

		fireMailStatusEvent(MailStatusEvent.COMMAND, command);
		out.print(command);
		out.print(CRLF);
		out.flush();
	}


	/**
	 * Send the message. Assumes that the message has been previously
	 * setup with calls to <code>setFrom(), addTo(), setBody()</code>
	 * and similar methods.
	 *
	 * @exception  IOException    when an I/O errors happen on the socket
	 * @exception  SMTPException  when an SMTP specific error occurs
	 */
	public synchronized void send () throws IOException, SMTPException {

		/*
		 * Make sure we have a from and at least one destination
		 */
		if (from.getAddress() == null) {
			throw new SMTPException("no from address specified");
		}

		if (toList.size() == 0) {
			throw new SMTPException("no to destination specified");
		}


		/*
		 * Connect to the SMTP server
		 */
		Connection c;
		try {

			c = Connection.getInstance();

		} catch (Exception e) {

			IOException ioe = new IOException();
			ioe.fillInStackTrace();
			throw (ioe);
		}

		c.open(host, SMTP_PORT);
		if (timeoutMsecs != -1) {
			c.setTimeout(timeoutMsecs);
		}

		boolean connected = false;
		PrintWriter out = null;
		BufferedReader in = null;

		try {

			out = new PrintWriter(c.getOutputStream(), true);
			in = new BufferedReader(
				new InputStreamReader(c.getInputStream()));

			/*
			 * As soon as we connect the SMTP dialog begins
			 */
			readAndCheck("220", in);
			connected = true;
	
			String senderHost = this.senderHost;
			if (senderHost == null) {
				String tmp[] = Utilities.splitDelimitedString(
					from.getAddress(), "@");
				if (tmp.length == 2) {
					senderHost = tmp[1];
				} else {
					senderHost = "localhost";
				}
			}
			

			/*
			 * Do we need to do an AUTH command?
			 */
			if ((authType == AUTH_PLAIN ||
					authType == AUTH_LOGIN) &&
					username != null && password != null) {

				send(out, "EHLO " + senderHost) ;
				readAndCheck("250", in);

				if (authType == AUTH_PLAIN) {
					plainAuth(out, in);
				} else {
					loginAuth(out, in);
				}

			} else {

				send(out, "HELO " + senderHost) ;
				readAndCheck("250", in);
			}
	
			send(out, "MAIL FROM: " + from.getPathAddress());

			try {
				readAndCheck("250", in);
			} catch (SMTPException se) {
				
				/*
				 * Try the "MAIL FROM" again without the 
				 * full name in the FROM address
				 */
				if (lastResponse != null &&
						lastResponse.length() >= 4) {

					send(out, "MAIL FROM: <" +
						from.getAddress() + ">");
					readAndCheck("250", in);
				}
			}
		
			/*
			 * Output the recipients
			 */
			outputRecipients(toList,  out, in);
			outputRecipients(ccList,  out, in);
			outputRecipients(bccList, out, in);
	
	
			/*
			 * Now setup to output the header and message body
			 */
			send(out, "DATA") ;
			readAndCheck("354", in);
	
			/*
			 * Build the header and output it
			 */
			outputHeader(out, in);
	
			/*
			 * Now send the message body.
			 */
			outputMessagePart(out, in);

		} finally {

			/*
			 * End the SMTP session.
			 */
			if (connected) {
				String saved_rsp = lastResponse;
				try {
					send(out, "QUIT");
					readAndCheck("221", in);

					out.close();
					in.close();
				} catch (Exception ignore) {
				}

				lastResponse = saved_rsp;
			}

			try {
				c.close() ;
			} catch (Exception ignore) {
			}
		}
	}


	/**
	 * Does a AUTH PLAIN command.
	 */
	protected void plainAuth (PrintWriter out, BufferedReader in)
			throws IOException, SMTPException {

		StringBuffer buf = new StringBuffer();
		buf.append("\000");
		buf.append(username);
		buf.append("\000");
		buf.append(password);
		byte bytes[] = buf.toString().getBytes();
		byte encoded[] = Base64Codec.encode(bytes);

		send(out, "AUTH PLAIN " + new String(encoded));
		readAndCheck("235", in);
	}


	/**
	 * Does a AUTH LOGIN command.
	 */
	protected void loginAuth (PrintWriter out, BufferedReader in)
			throws IOException, SMTPException {

		byte bytes[];
		bytes = this.username.getBytes();
		byte username[] = Base64Codec.encode(bytes);
		bytes = new String(this.password).getBytes();
		byte password[] = Base64Codec.encode(bytes);

		send(out, "AUTH LOGIN");
		readAndCheck("334", in);
		out.write(new String(username) + CRLF);
		out.flush();
		readAndCheck("334", in);
		out.write(new String(password) + CRLF);
		out.flush();
		readAndCheck("235", in);
	}


	/*
	 * Output the body of the message. This method takes care of
	 * setting the MIME types if any attachements are specified.
	 *
	 * @exception  IOException    when an I/O errors happen on the socket
	 * @exception  SMTPException  when an SMTP specific error occurs
	 */
	private void outputMessagePart (PrintWriter out, BufferedReader in)
		throws IOException, SMTPException {

		/*
		 * Output the MIME version header field
		 */
		out.write(CRLF + "Mime-Version: 1.0");

		/*
		 * Do we have attachments? If so output with boundary.
		 */
		if (attachments.size() > 0) {
			/*
			 * Construct a boundary
			 */
			java.util.Date d = new java.util.Date();
			String boundary = new String(
				"=====================_" + d.getTime() + "==_");	

			out.write(CRLF + "Content-Type: multipart/mixed; " +
				"boundary=\"" + boundary + "\"");

			/*
			 * Always a blank line between header and body
			 */
			out.write(CRLF);

			/*
			 * Output the body text
			 */
			out.write(CRLF + "--" + boundary);
			out.write(CRLF + "Content-Type: text/plain; " +
				"charset=\"" + getCharset() + "\"");
			out.write(CRLF);
			out.write(CRLF + body.toString());
			out.flush();
			
			/*
			 * Output the attachments
			 */
			outputAttachments(out, boundary);

			/*
			 * And the closing boundary
			 */
			out.write(CRLF + "--" + boundary + "--");
		} else {
			out.write(CRLF + "Content-Type: text/plain; " +
				"charset=\"" + getCharset() + "\"");

			/*
			 * Always a blank line between header and body
			 */
			out.write(CRLF);

			out.write(CRLF + body.toString());
		}

		/*
		 * Send a lone period(.). This indicates the end of
		 * the body text.
		 */
		out.write(CRLF + "." + CRLF);
		out.flush();
		readAndCheck("250", in);
	}

	
	/*
	 * Output all the file attachments. 
	 *
	 * @exception  IOException    when an I/O errors happen on the socket
	 * @exception  SMTPException  when an SMTP specific error occurs
	 */
	private void outputAttachments (PrintWriter out, String boundary)
		throws IOException, SMTPException {

		for (int i = 0; i < attachments.size(); i++) {

			Attachment attachment = (Attachment)attachments.get(i);
			String name = attachment.getName();
			String type = attachment.getType();
			Object obj  = attachment.getData();

			/*
			 * Lookup the content type for the attachment
			 */
			if (obj instanceof File) {

				File f = (File)obj;
				if (type == null) {
					type = MimeMap.getContentTypeFromFile(f);
				}

				if (name == null) {
					name = f.getName();
				}

			} else if (obj instanceof URL) {

				if (name == null) {
					name = ((URL)obj).getPath();
				}

			} else if (obj instanceof InputStream) {
			} else if (obj instanceof String) {
			} else {
				continue;
			}


			/*
			 * Output the boundary
			 */
			out.write(CRLF + "--" + boundary);


			if (type == null) {
				type = "application/octet-stream";
			}

			out.write(CRLF + "Content-Type: " + type);
			if (name != null) {
				out.write("; name=\"" + name + "\"");
			}

			out.write(CRLF + "Content-Transfer-Encoding: base64");
			out.write(CRLF + "Content-Disposition: attachment");
			if (name != null) {
				out.write("; filename=\"" + name + "\"");
			}

			out.write(CRLF);
			out.write(CRLF);

			/*
			 * Now output the contents base64 encoded
			 */
			if (obj instanceof File) {
				outputAttachment((File)obj, out);
			} else if (obj instanceof InputStream) {
				outputAttachment((InputStream)obj, out);
			} else if (obj instanceof String) {
				String s = (String)obj;
				outputAttachment(new ByteArrayInputStream(s.getBytes()), out);
			} else if (obj instanceof URL) {
				InputStream in = ((URL)obj).openStream();
				outputAttachment(in, out);
			}

			out.flush();
		}
	}


	/*
	 * Output a single file attachment.
	 *
	 * @exception  IOException    when an I/O errors happen on the file 
	 */
	private void outputAttachment (File f, PrintWriter out)
		throws IOException {

		FileInputStream fis = new FileInputStream(f);
		outputAttachment(fis, out);
		fis.close();
	}


	/*
	 * Output a single InputStream attachment.
	 *
	 * @exception  IOException    when an I/O errors happen on the file 
	 */
	private void outputAttachment (InputStream in, PrintWriter out)
		throws IOException {

		/*
		 * Output in 48 byte chunks. This gives a nice 64 byte wide
		 * encoded string.
		 */
		byte buf[] = new byte[48];
		int n;
		while ((n = in.read(buf)) > 0) {

			byte encoded[];
			if (n < 48) {
				byte tmp[] = new byte[n];
				for (int i = 0; i < n; i++) {
					tmp[i] = buf[i];
				}
				encoded = Base64Codec.encode(tmp);
			} else {
				encoded = Base64Codec.encode(buf);
			}

			out.write(new String(encoded) + CRLF);
		}
	}


	/**
	 * Adds a SMTP event listener.
	 *
	 * @param  l  the SMTPStatusListener to add
	 */
	public synchronized void addStatusListener (SMTPStatusListener l) {

		if (!smtpListeners.contains(l)) {
	   		smtpListeners.add(l);
		}
	}


	/**
	 * Removes a SMTP event listener.
	 *
	 * @param  l  the SMTPStatusListener to remove
	 */
	public synchronized void removeStatusListener (SMTPStatusListener l) {
	   	smtpListeners.remove(l);
	}


	/**
	 * Notify listening objects of SMTP events.
	 *
	 * @param  type     The type of SMTP event (COMMAND | RESPONSE)
	 * @param  message  The SMTP command or response message.
	 */
	public void fireMailStatusEvent (int type, String message) {

		/*
		 * Make a copy of the listener object ArrayList so that it cannot
		 * be changed while we are firing events
		 */
		ArrayList<SMTPStatusListener> v;
		synchronized(this) {
			if (smtpListeners.size() < 1) {
				return;
			}

			v = new ArrayList<SMTPStatusListener>(smtpListeners.size());
			for (SMTPStatusListener l : smtpListeners) {
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
			SMTPStatusListener l = v.get(i);
	     	l.smtpStatus(evt);
	  	}
	}


	/**
	 * Send the mail message in the background. This is done by creating
	 * a thread and invoking send() in that thread's run() method.
	 * Status and execptions are reported to any registered
	 * SMTPStatusListeners.
	 */
	public synchronized void backgroundSend () {

		Thread thread = new Thread(new SMTPWorker(this));
		thread.setPriority(Thread.MAX_PRIORITY - 1);
		thread.start();
	}


	/**
	 * Objects of this class are used to send the mail in the background.
	 */
	protected class SMTPWorker implements Runnable {

		private SMTP smtp = null;

		protected SMTPWorker (SMTP smtp) {
			this.smtp = smtp;
		}

		public void run () {

			try {

				smtp.send();
				smtp.fireMailStatusEvent(
					MailStatusEvent.DONE, "Done");

			} catch (Exception e) {

				smtp.fireMailStatusEvent(
					MailStatusEvent.EXCEPTION,
					e.getMessage());
			}
		}
	}


	/**
	 * Class to describe an E-mail address. Conatins the full name
	 * and address of an E-mail recipient or sender.
	 */
	protected class MailAddress {

		String fullname  = null;
		String address   = null;

		MailAddress () {
		}

		MailAddress (String fullname, String address) {
			if (fullname != null) {
				this.fullname = fullname.trim();
			}

			if (address != null) {
				this.address  = address.trim();
			}
		}


		String getFullAddress () {
		
			if (fullname == null) {
				return (address);
			} else {
				return (fullname + " <" + address + ">");
			}
		}


		String getPathAddress () {
		
			if (fullname == null) {
				return ("<" + address + ">");
			} else {
				return (fullname + " <" + address + ">");
			}
		}


		String getFullname () {
			return (fullname);
		}


		void setFullname (String fullname) {
			this.fullname = fullname;
		}

		
		String getAddress () {
			return (address);
		}


		void setAddress (String address) {
			this.address = address;
		}

		
		boolean isValid () {
			if (address == null || address.length() < 1) {
				return (false);
			} else {
				return (true);
			}
		}
	}
}
