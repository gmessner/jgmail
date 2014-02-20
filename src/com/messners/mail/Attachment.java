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

import java.io.File;
import java.io.InputStream;
import java.net.URL;

/**
 * This class defines an email attachment.
 *
 * @author Gregory M. Messner <gmessner@messners.com>
 * @version $Revision: 1.1 $
 */

public class Attachment {

	private String mimeType;
	private String name;
	private Object data;


	public Attachment (String name, String mimeType, File file) {

		this.name = name;
		this.mimeType = mimeType;
		this.data = file;
	}


	public Attachment (String name, String mimeType, InputStream dataStream) {

		this.name = name;
		this.mimeType = mimeType;
		this.data = dataStream;
	}


	public Attachment (String name, String mimeType, String data) {

		this.name = name;
		this.mimeType = mimeType;
		this.data = data;
	}


	public Attachment (String name, String mimeType, URL url) {

		this.name = name;
		this.mimeType = mimeType;
		this.data = url;
	}


	public String getType () {
		return (mimeType);
	}


	public String getName () {
		return (name);
	}


	public Object getData () {
		return (data);
	}


	public boolean isFile () {
		return (data instanceof File);
	}


	public boolean isInputStream () {
		return (data instanceof InputStream);
	}


	public boolean isUrl () {
		return (data instanceof URL);
	}


	public boolean isString () {
		return (data instanceof String);
	}
}
