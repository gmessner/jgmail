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
 * The <code>MessagePart</code> class encapsulates the definition of
 * an email message body. 
 *
 * @author  Gregory M. Messner <gmessner@messners.com>
 * @version $Revision: 1.1 $
 */

public class MessagePart {

	/**
	 * Specifies that this MessagePart was the text of the mail message.
	 */
	public static final int TEXT = 0;

	/**
	 * Specifies that this MessagePart was a file attachment.
	 */
	public static final int FILE = 1;


	protected int type;
	protected String text        = null;
	protected String name        = null;
	protected String filename    = null;
	protected String contentType = null;
	protected String encoding    = null;
	protected String charset     = null;

	protected StringBuffer buf = new StringBuffer();
	
	protected MessagePart () {
	}

	
	/**
	 * Constructs a <code>MessagePart</code> object that contains 
	 * plain text content.
	 *
	 * @param  text  the text of the message body
	 */
	public MessagePart (String text) {

		type = TEXT;
		append(text);
		contentType = "plain/text";
		charset = "us-ascii";
	}


	/**
	 * Constructs a <code>MessagePart</code> object that contains 
	 * a message body of the specified content type.
	 *
	 * @param  text  the text of the message body
	 * @param  content_type  the MIME type for the content
	 */
	public MessagePart (String text, String content_type) {

		this.type = TEXT;
		append(text);
		parseContentType(content_type, "charset");
	}


	/**
	 * Constructs a <code>MessagePart</code> object that contains 
	 * a message body from a file attachment.
	 *
	 * @param  name  the original name of the attached file
	 * @param  filename  the filename of the attachment
	 * @param  content_type  the MIME type for the content
	 * @param  encoding      the MIME type encoding for the content
	 */
	public MessagePart (String name, String filename,
		String content_type, String encoding) {

		this.type = FILE;
		this.name = name;
		this.filename = filename;
		this.encoding = encoding;

		parseContentType(content_type, "charset");
	}


	/**
	 * Constructs a <code>MessagePart</code> object that contains 
	 * a message body from a file attachment.
	 *
	 * @param  filename  the filename of the attachment
	 * @param  content_type  the MIME type for the content
	 * @param  encoding      the MIME type encoding for the content
	 */
	public MessagePart (String filename, 
		String content_type, String encoding) {

		this.type = FILE;
		this.encoding = encoding;
		this.filename = filename;

		parseContentType(content_type, "name");
	}


	/**
	 * Append the string to the body of this message.
	 *
	 * @param  s  the string to append
	 */
	public void append (String s) {

		if (buf.length() > 0) {
			buf.append('\n');
		}

		buf.append(s);
		text = null;
	}
	

	/**
	 * Get the encoding type for this message body.
	 */
	public String getEncoding () {
		return (encoding);
	}
		

	/**
	 * Get the type of this message body. Will be either <code>FILE</code>
	 * or <code>TEXT</code>
	 */
	public int getType () {
		return (type);
	}


	/**
	 * Get the text of the message body.
	 */
	public String getText () {

		if (this.text == null) {
			this.text = buf.toString();
		}

		return (text);
	}


	/**
	 * Get the MIME type (content type) of the message body or attachment.
	 */
	public String getContentType () {
		return (contentType);
	}

	
	/**
	 * Get the name of the attachment.
	 */
	public String getName () {
		return (name);
	}


	/**
	 * Get the charset for the content type.
	 */
	public String getCharset () {
		return (charset);
	}


	/**
	 * Get the filename of the attachment.
	 */
	public String getFilename () {
		return (filename);
	}


	/**
	 * Get the content type and charset or name from a string
	 *
	 * @param  contentType  a string containing the content-type and
	 *                      possibly the charset or name of the content
	 * @param  lookfor      specifies whether to look for the "charset"
	 *                      or "name" in the content_type string
	 */
	protected void parseContentType (String contentType, String lookfor) {

		/*
		 * See if this content-type has more than 1 part
		 */
		int index = contentType.indexOf(";");
		if (index < 0) {
			this.contentType = contentType.trim();
			return;
		}

		String part2 = contentType.substring(index + 1);
		if (part2 != null) {
			part2 = part2.trim();
		}
		
		/*
		 * Save the content-type string
		 */
		this.contentType = contentType.substring(0, index).trim();

		if (part2 == null || part2.length() == 0) {
			return;
		}


		/*
		 * See if there is a match of lookfor in the content-type
		 */
		index = part2.indexOf(lookfor);
		if (index < 0) {
			return;
		}


		part2 = part2.substring(index + lookfor.length() + 1);
		if (part2 != null) {
			part2 = Utilities.removeQuotes(part2.trim());
		}

		if (part2 == null || part2.length() == 0) {
			return;
		}

		if (lookfor.equals("charset")) {
			charset = part2;
		} else if (lookfor.equals("name")) {
			name = part2;
		}
	}
}

