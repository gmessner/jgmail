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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.ArrayList;

/**
 * The <code>POP3MailMessage</code> class encapsulates the definition of
 * an email message header and body. 
 *
 * @author  Gregory M. Messner <gmessner@messners.com>
 * @version $Revision: 1.1 $
 */

public class POP3MailMessage {

	protected Map<String, String> header = null;
	protected MessagePart body[]         = null;
	protected String attachmentDir       = null;
	protected POP3 pop3Connection        = null;
	protected POP3Reader in              = null;
	protected POP3MessageInfo msgInfo    = null;
	protected int msgSize = -1;
	protected int msgTotal = 0;


	/**
	 * Create a mail message identified by <code>msgInfo</code>
	 * ready to read from the the specified POP3 connection.
	 *
	 * @param  connection  the POP3 connection to read the message from.
	 * @param  msgInfo     information about the message to read.
	 */
	public  POP3MailMessage (POP3 connection, POP3MessageInfo msgInfo) {

		this.pop3Connection = connection;
		this.attachmentDir  = connection.getAttachmentDir();
		this.in = connection.getInputStream();
		this.msgSize = msgInfo.getMessageSize();		
		this.msgInfo = msgInfo;
	}

	/**
	 * Create a mail message ready to read from the the
	 * specified POP3 connection.
	 *
	 * @param  connection   the POP3 connection to read the message from
	 */
	public  POP3MailMessage (POP3 connection) {

		this.pop3Connection = connection;
		this.attachmentDir  = connection.getAttachmentDir();
		this.in = connection.getInputStream();
	}


	/**
	 * Create a mail message ready to read from the the specified stream.
	 *
	 * @param  in  the stream to read the message from
	 */
	public POP3MailMessage (POP3Reader in) {

		this.attachmentDir = POP3.getDefaultAttachmentDir();
		this.in = in;
	}


	/**
	 * Create a mail message ready to read from the the specified stream.
	 * Attachments will be saved in the specified directory.
	 *
	 * @param  in  the stream to read the message from
	 * @param  attachmentDir  the directyory to save attachments into
	 */
	public POP3MailMessage (POP3Reader in, String attachmentDir)  {

		this.attachmentDir = attachmentDir;
		this.in = in;
	}


	/**
	 * Set the computed size of the message.
	 *
	 * @param  msg_size  the computed size of the message.
	 */
	public void setMessageSize (int msg_size) {
		this.msgSize = msg_size;
	}


	/**
	 * Return the name of the directory which attachments are saved in.
	 */
	public String getAttachmentDir () {
		return (attachmentDir);
	}


	/**
	 * Set the directory for saving attachemnts.
	 *
	 * @param  dir  the new attachment directory.
	 */
	public void setAttachmentDir (String dir) {
		attachmentDir = dir;
	}


	/**
	 * Gets a Map<String, String> that contains the name/value pairs from the
	 * message header.
	 */
	public Map<String, String> getHeader () {
		return (header);
	}


	/**
	 * Get the value associated with a header field name. Assumes that
	 * the header has been read with <code>readHeader</code>.
	 *
	 * @param  name  the name of the header field to retrieve the value for
	 */
	public String getHeaderValue (String name) {

		if (header == null) {
			return (null);
		}

		name = name.toLowerCase();

		try {
			String value = (String)header.get(name);
			return (value);
		} catch (Exception ignore) {
			return (null);
		}
	}


	/**
	 * Read the mail message.
	 *
	 * @exception  IOException If an I/O error occurs 
	 * @exception  POP3Exception  when a POP3 protocol error occurs.
	 */
	public POP3MailMessage read () throws IOException, POP3Exception {

		if (in == null) {
			throw new IOException("message stream is null");
		}

		int start = in.getBytesRead();
		header = in.readNameValuePairs();
		int total = in.getBytesRead() - start;
		if (pop3Connection != null) {
			pop3Connection.fireMailProgressEvent(
				total, total, msgSize);
		}
		incrMessageTotal(total);

		body   = readBody();
		return (this);
	}


	/**
	 * Read the body of the message. The body may contain both
	 * inline content and attachments.  We assume that the header
	 * has been read prior to calling this method.
	 *
	 * @exception  IOException  If an I/O error occurs 
	 * @exception  POP3Exception  when a POP3 protocol error occurs.
	 */
	protected synchronized MessagePart [] readBody ()
		throws IOException, POP3Exception  {

		if (body != null) {
			return (body);
		}

		if (in == null) {
			throw new IOException("message stream is null");
		}

		if (header == null) {
			throw new POP3Exception("header not read yet");
		}

		/*
		 * Is this a multipart message?
		 */
		String content_type = getHeaderValue("content-type");
		if (content_type == null) {
			content_type = "text/plain";
		}

		String tmp = content_type.toLowerCase();
		if (tmp.indexOf("multipart") >= 0) {
			return (parseMultipart(content_type));
		}

		/*
		 * Is this a single attachment with no body?
		 */
		String content_disp = getHeaderValue("content-disposition");
		if (content_disp != null) {
			tmp = content_disp.toLowerCase();
			if (tmp.indexOf("attachment") >= 0) {
				return (parseSingleAttachment());
			}
		}

		/*
		 * If we got here the message is a non-attachement message or
		 * a message with a single attachment and no body.  Read the
		 * body of the message.
		 */
		StringBuffer buf = new StringBuffer();
		boolean first_line = true;
		int last_total = in.getBytesRead();
		while (true) {
			
			String line = in.readLine();
			if (line == null) {
				break;
			}

			if (first_line) {
				first_line = false;
				buf.append(line);
			} else {
				buf.append("\n" + line);
			}


			int bytes = in.getBytesRead() - last_total;
			int msgTotal = incrMessageTotal(bytes);
			last_total += bytes;
			if (pop3Connection != null) {
				pop3Connection.fireMailProgressEvent(
					bytes, msgTotal, msgSize);
			}
		}
	
		MessagePart body[] = new MessagePart[1];
		body[0] = new MessagePart(buf.toString(), content_type);
		return (body);	
	}


	/**
	 * Get the body of the message. The body may contain both
	 * inline content and attachments.
	 */
	public synchronized MessagePart [] getBody () {
		return (body);
	}


	/**
	 * Read in a multipart message and save the body and files contained
	 * in the boundaries.
	 *
	 * @exception  IOException If an I/O error occurs 
	 */
	protected MessagePart [] parseMultipart (String content_type)
		throws IOException, POP3Exception  {

		/*
		 * Parse the boundary string out of the content type
		 */
		String tmp = content_type.toLowerCase();
		int index = tmp.indexOf("boundary=");
		if (index == -1) {
			throw new POP3Exception(
				"no boundary found in multipart message");
		}

		String boundary = content_type.substring(index + 9).trim();
		if ((boundary = Utilities.removeQuotes(boundary)) == null) {	
			throw new POP3Exception(
				"malformed boundary in multipart message");
		}

		/*
		 * Get the length of the boundary string, we'll need it later
		 */
		int boundary_len = boundary.length();

		/*
		 * Now read each boundary and create a MessagePart object
		 * to hold the text or file info
		 */
		ArrayList<MessagePart> parts = new ArrayList<MessagePart>();
		OutputStream outfile = null;
		MessagePart part = null;
		String base64_extra = null;
		boolean attachment = false;
		boolean is_base64 = false;
		int last_total = in.getBytesRead();
		byte line_separator[] = Utilities.getLineSeparator().getBytes();
		while (true) {

			String line = in.readLine();
			if (line == null) {
				break;
			}

			int bytes = in.getBytesRead() - last_total;
			int msgTotal = incrMessageTotal(bytes);
			last_total += bytes;
			if (pop3Connection != null) {
				pop3Connection.fireMailProgressEvent(
					bytes, msgTotal, msgSize);
			}

			/*
			 * Are we on a new boundary?
			 */
			if (line.indexOf(boundary) == 2 &&
				line.substring(0, 2).equals("--")) {

				/*
				 * If we have a message part to finish up, do it
				 */
				if (part != null) {
					if (attachment && outfile != null) {
						outfile.close();
						outfile = null;
					}

					parts.add(part);
					part = null;
					base64_extra = null;
					attachment = false;
					is_base64 = false;
				}


				/*
				 * See if this is the terminating boundary,
				 * if so we are all done here
				 */
				if (line.indexOf("--", boundary_len + 2) ==
					boundary_len + 2) {

					continue;
				}


				/*
				 * Parse the message part header
				 */
				part = parseParts();
				if (part == null) {
					continue;
				}

				bytes = in.getBytesRead() - last_total;
				msgTotal = incrMessageTotal(bytes);
				last_total += bytes;
				if (pop3Connection != null) {
					pop3Connection.fireMailProgressEvent(
						bytes, msgTotal, msgSize);
				}

				if (part.getType() == MessagePart.FILE) {
					String encoding = part.getEncoding();
					if (encoding != null &&
						encoding.equals("base64")) {
							is_base64 = true;
					} else {
						is_base64 = false;
					}

					outfile = new FileOutputStream(part.getFilename());
					attachment = true;
					base64_extra = null;
				} else {
					attachment = false;
				}

			} else if (part == null) {
				continue;
			} else if (is_base64 && attachment) {

				if (base64_extra != null) {
					line = base64_extra + line;
				}

				/*
				 * The base 64 Codec only works with
				 * data lengths divisible by 4
				 */
				int len = line.length();
				int left_over = len % 4;
				if (left_over != 0) {
					base64_extra = line.substring(
							len - left_over);
					line = line.substring(
							0, len - left_over);	
				} else {
					base64_extra = null;
				}

				byte buf[] = Base64Codec.decode(
						line.getBytes());
				if (buf != null) {
					outfile.write(buf);
				}

			} else if (attachment) {

				byte buf[] = line.getBytes();
				if (buf != null) {
					outfile.write(buf);
					outfile.write(line_separator);
				}
				
			} else {
				part.append(line);
			}
		}

		/*
		 * Convert the ArrayList of MessagePart to MessagePart[]
		 */
		int num_parts = parts.size();
		if (num_parts < 1) {
			return (null);
		}

		MessagePart body[] = new MessagePart[num_parts];
		parts.toArray(body);
		return (body);	
	}


	/**
	 * Reads and parses the information for 1 part of a multipart message.
	 *
	 * @exception  IOException If an I/O error occurs 
	 */
	protected MessagePart parseParts () throws IOException {

		Map<String, String> parts;
		try {
			if ((parts = in.readNameValuePairs()) == null) {
				return (null);
			}
		} catch (Exception e) {
			return (null);
		}

		return (parseHeader(parts));
	}



	/**
	 * Read in a single attachment with no body.
	 *
	 * @exception  IOException If an I/O error occurs 
	 */
	protected MessagePart [] parseSingleAttachment ()
		throws IOException, POP3Exception  {


		OutputStream outfile = null;
		MessagePart part = parseHeader(header);
		if (part == null) {
			return (null);
		}

		String base64_extra = null;
		boolean attachment = false;
		boolean is_base64 = false;
		int last_total = in.getBytesRead();
		byte line_separator[] = Utilities.getLineSeparator().getBytes();

		if (part.getType() == MessagePart.FILE) {
			String encoding = part.getEncoding();
			if (encoding != null && encoding.equals("base64")) {
				is_base64 = true;
			}

			outfile = new FileOutputStream(part.getFilename());
			attachment = true;
		}


		while (true) {

			String line = in.readLine();
			if (line == null) {
				break;
			}

			int bytes = in.getBytesRead() - last_total;
			int msgTotal = incrMessageTotal(bytes);
			last_total += bytes;
			if (pop3Connection != null) {
				pop3Connection.fireMailProgressEvent(
					bytes, msgTotal, msgSize);
			}

			if (is_base64 && attachment) {

				if (base64_extra != null) {
					line = base64_extra + line;
				}

				/*
				 * The base 64 Codec only works with
				 * data lengths divisible by 4
				 */
				int len = line.length();
				int left_over = len % 4;
				if (left_over != 0) {
					base64_extra = line.substring(
							len - left_over);
					line = line.substring(
							0, len - left_over);	
				} else {
					base64_extra = null;
				}

				byte buf[] = Base64Codec.decode(line.getBytes());
				if (buf != null) {
					outfile.write(buf);
				}

			} else if (attachment) {

				byte buf[] = line.getBytes();
				if (buf != null) {
					outfile.write(buf);
					outfile.write(line_separator);
				}
				
			} else {
				part.append(line);
			}
		}

		if (outfile != null) {
			outfile.close();
		}

		MessagePart body[] = new MessagePart[1];
		body[0] = part;
		return (body);	
	}
	


	protected MessagePart parseHeader (Map<String, String> parts) throws IOException {
			
		String content_type = parts.get("content-type");
		String content_disp = parts.get("content-disposition");
		String enc = parts.get("content-transfer-encoding");
		
		/*
		 * If part has no content-disposition item then it's
		 * an inline message
		 */
		if (content_disp == null) {
			return (new MessagePart("", content_type));
		}

		/*
		 * See if this content-disp has more than 1 part
		 */
		int index = content_disp.indexOf(";");
		if (index < 0) {
			return (new MessagePart("", content_type));
		}

		String part2 = content_disp.substring(index + 1);
		if (part2 != null) {
			part2 = Utilities.removeQuotes(part2.trim());
		}

		/*
		 * See if there is a filename in the content-disposition
		 */
		if (part2 != null && part2.length() > 0) {

			index = part2.indexOf("filename");
			if (index >= 0) {
				String filename = part2.substring(index + 9);
				if (filename != null) {

					filename = Utilities.removeQuotes(filename.trim());
					filename = Utilities.createUniqueFile(
											attachmentDir, filename);
					return (new MessagePart(filename, content_type, enc));
				}
			}
		}

		return (new MessagePart("", content_type));
	}


	/**
	 * Utility function which simply writes the message header
	 * to System.out.
	 *
	 * @exception  IOException If an I/O error occurs 
	 */
	public void dumpHeader () throws IOException {

		if (header == null) {
			return;
		}

		for (String key : header.keySet()) {
			String value = header.get(key);
			System.out.println(key + ": " + value);	
		}
	}


	/**
	 * Increment the total count of bytes read for this message.
	 *
	 * @param  by_bytes  The number of bytes to increment the total by.
	 */
	protected synchronized int incrMessageTotal (int by_bytes) {
		msgTotal += by_bytes;
		return (msgTotal);
	}


	/**
	 * Get the computed size of this message.
	 */
	public synchronized int getMessageSize () {
		return (msgSize);
	}


	/**
	 * Get the total count of bytes processed for this message to date.
	 */
	public synchronized int getMessageTotal () {
		return (msgTotal);
	}
}
