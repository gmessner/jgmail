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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * This class defines methods to retrieve the MIME content type for a given
 * file (or filename)
 *
 * @author  Gregory M. Messner <gmessner@messners.com>
 * @version $Revision: 1.1 $
 */

public class MimeMap {

	protected Properties mimeMap = null;
	protected static Properties mapDefaults = null;


	/**
	 * Create a <code>MimeMap</code> object from the default mappings.
	 */
	public MimeMap () {

		/*
		 * Load the defaults and any provided overrides
		 */
		loadDefaults();
		mimeMap = new Properties(mapDefaults);
	}


	/**
	 * Create a <code>MimeMap</code> object from the default mappings
	 * and then override the defaults with the mappings stored in
	 * the given filename.
	 *
	 * @param  filename  the override mappings file
	 */
	public MimeMap (String filename) throws FileNotFoundException {

		/*
		 * Load the defaults and any provided overrides
		 */
		loadDefaults();
		mimeMap = new Properties(mapDefaults);
		if (filename != null) {
			FileInputStream fis = new FileInputStream(filename);
			if (fis != null) {
				try {
					mimeMap.load(fis);
				} catch (IOException ioe) {
				}
			}
		}
	}


	protected static void loadDefaults () {

		/*
		 * Create the default mappings if not yet done
		 */
		if (mapDefaults == null) {
			try {
				Class<?> cls = Class.forName(
					"com.messners.mail.MimeMap");
				mapDefaults = new Properties();
				InputStream is = cls.getResourceAsStream("mimetype.map");
				if (is != null) {
					mapDefaults.load(is);
				}
			} catch (Exception ignore) {
			}
		}
	}


	/**
	 * Gets the MIME content type for the given filename.
	 *
	 * @param  filename  the filename to get the MIME content type for
	 * @return the MIME content type for the given filename
	 */
	public String getContentType (String filename) {

		try {
			File f = new File(filename);
			return (getContentType(f));
		} catch (Exception e) {
			return (null);
		}
	}


	/**
	 * Gets the MIME content type for the given <code>File</code> oinstance.
	 *
	 * @param  f  the File object to get the MIME content type for
	 * @return the MIME content type for the given <code>File</code> instance
	 */
	public String getContentType (File f) {

		String name = f.getName();
		int index = name.lastIndexOf(".");
		if (index >= 0) {
			name= name.substring(index + 1);
		}

		return (mimeMap.getProperty(name));
	}


	/**
	 * <p>Gets the MIME content type for the given File object.</p>
	 * This static method uses the default mapping table.
	 *
	 * @param  f  the File object to get the MIME content type for.
	 * @return the MIME content type for the given <code>File</code> instance
	 */
	public static String getContentTypeFromFile (File f) {

		String name = f.getName();
		int index = name.lastIndexOf(".");
		if (index >= 0) {
			name= name.substring(index + 1);
		}

		loadDefaults();
		return (mapDefaults.getProperty(name));
	}


	/**
	 * <p>Gets the MIME content type for the given filename.</p>
	 * This static method uses the default mapping table.
	 *
	 * @param  filename  the filename to get the MIME content type for
	 * @return the MIME content type for the given filename
	 */
	public static String getContentTypeFromFilename (String filename) {

		try {
			File f = new File(filename);
			return (getContentTypeFromFile(f));
		} catch (Exception e) {
			return (null);
		}
	}
}

