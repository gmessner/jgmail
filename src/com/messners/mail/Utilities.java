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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class defines various utilitiy methods that are used throughout
 * the <em>com.messners.mail</em> package.
 *
 * @author Gregory M. Messner <gmessner@messners.com>
 * @version $Revision: 1.1 $
 */

public class Utilities {

	/**
	 * Native system tmp directory.
	 */
	private static String systemTmpDir = null;


	/**
	 * Native line separator ("\n" on Unix).
	 */
	protected static String lineSeparator = 
		getProperty("line.separator");


	/**
	 * Get the native line separator ("\n" on Unix).
	 */
	public static String getLineSeparator () {
		return (lineSeparator);
	}


	/**
	 * Storage for the is_windows flag, indicating whether we are running
	 * on a Windows OS.
	 */
	protected static boolean isWindows = true;
	static {
		String os = getProperty("os.name");
		if (os != null) {
			os = os.toLowerCase();
			if (os.indexOf("windows") < 0) {
				isWindows = false;
			}
		}
	}


	/**
	 * Gets the the is_windows flag, indicating whether we are running
	 * on a Windows OS.
	 */
	public static boolean isWindows () {
		return (isWindows);
	}

	
	/**
	 * Returns a date-time string formatted in accordance with RFC 822
	 * for the current time.
	 */
	public static String getFormattedDate () {
		return (getFormattedDate(null));
	}


	/**
	 * Returns a date-time string formatted in accordance with RFC 822.
	 *
	 * @param  datetime  The <code>Date</code> object to format.
	 * the string for. Use null for the current date/time.
	 */
	public static String getFormattedDate (java.util.Date datetime) {

		java.text.SimpleDateFormat df = 
			new java.text.SimpleDateFormat(
			"EEE, dd MMM yyyy HH:mm:ss z", java.util.Locale.US);
		
		if (datetime == null) {
			datetime = new java.util.Date();
		}

		return (df.format(datetime));
	}


	/**
	 * Returns a Date object parsed from the date-time string formatted
	 * in accordance with RFC 822.
	 *
	 * @param  str  the string to parse
	 */
	public static java.util.Date formattedDateToDate (String str) {
		
		java.text.SimpleDateFormat df = 
			new java.text.SimpleDateFormat(
			"EEE, dd MMM yyyy HH:mm:ss z", java.util.Locale.US);

		java.text.ParsePosition pos = new java.text.ParsePosition(0);
		try {
			return (df.parse(str, pos));
		} catch (Exception ignore) {
			return (null);
		}
	}


	/**
	 * Remove the beginning and trailing quotes from a string.
	 *
	 * @param  in  the string to remove the quotes from.
	 * @return the de-quotified string. If the string is malformed 
	 * (begins with but does not end with a quote) null is returned.
	 */
	public static String removeQuotes (String in) {

		if (in == null) {
			return (null);
		}


		/*
		 * Parse off quotes
		 */
		in = in.trim();
		int index = in.indexOf("\"");
		if (index == 0) {
			index = in.indexOf("\"", 1);
			if (index < 0) {
				return (null);
			}

			in = in.substring(1, index);
		}

		return (in);
	}


	/**
	 * Create a unique file in the specified directory based on
	 * <code>name</code>.
	 *
	 * @param  directory  the directory for the unique file
	 * @param  name       the original filename for the unique file.
	 * @exception IOException when an exception occurs.
	 */
	public static final synchronized String createUniqueFile (
		String directory, String name) throws IOException {

		File f = getUniqueFile(directory, name, 0);
		FileOutputStream fos = new FileOutputStream(f);
		fos.close();
		fos = null;
		return (f.getPath());
	}


	/**
	 * Generate a unique filename in the specified directory.
	 *
	 * @param  directory  the directory for the unique file
	 * @param  name       the original filename for the unique file.
	 */
	public static final String getUniqueFilename (
		String directory, String name) {

		File f = getUniqueFile(directory, name, 0);
		return (f.getPath());
	}


	/**
	 * Generate a <code>File</code> object that points to a unique
	 * filename in the specified directory.
	 *
	 * @param  directory  the directory for the unique file
	 * @param  name       the original filename for the unique file.
	 */
	public static final File getUniqueFile (
		String directory, String name) {
	
		return (getUniqueFile(directory, name, 0));
	}


	/**
	 * This method does the actual work of generating the unique filename.
	 */
	private static File getUniqueFile (
		String directory, String name, int times) {

		if (times == 0) {
			File f = new File(directory, name);
			if (!f.exists())
				return (f);
		}

		times++;

		String tmpname;
		int index = name.lastIndexOf(".");
		if (index >= 0) {
			tmpname = name.substring(0, index) + 
						times + name.substring(index);
		} else {
			tmpname = name + times;
		}
				
		File f = new File(directory, tmpname);
		if (!f.exists())
			return (f);
			
		return (getUniqueFile(directory, name, times));	
	}


	/**
	 * Splits up a delimited string into an array of strings.
	 *
	 * @param  list  The delimited string to split up.
	 * @param  delimiter  The delimiter to split the string up on.
	 */
	public static String [] splitDelimitedString (
		String list, String delimiter) {

		ArrayList<String> fields = new ArrayList<String>();

		list = list.trim();
		if (delimiter == null) {
			delimiter = ";";
		}

		int first_delim = 0;
		int next_delim = list.indexOf(delimiter, 1);
		while (next_delim >= 0) {
			fields.add(
				list.substring(first_delim, next_delim));
			first_delim = next_delim + 1;
			next_delim = list.indexOf(delimiter, first_delim);
		}

		/*
		 * Add the last (or only) field
		 */
		fields.add(list.substring(first_delim));

		/*
		 * Convert the ArrayList into String []
		 */
		String strings[] = new String[fields.size()];
		fields.toArray(strings);
		return (strings);
	}


	/**
	 * Gets the host address for the local machine.
	 *
	 * @return a string with the local hostname or null on error
	 */
	public static String getLocalHostAddress () {

		java.net.InetAddress addr = null;
		try {
			addr = java.net.InetAddress.getLocalHost();
		} catch (Exception ignore) {
			return (null);
		}

		return (addr.getHostAddress());
	}


	/**
	 * Gets the host name for the local machine.
	 *
	 * @return a string with the local hostname or null on error
	 */
	public static String getLocalHostName () {

		java.net.InetAddress addr = null;
		try {
			addr = java.net.InetAddress.getLocalHost();
		} catch (Exception ignore) {
			return (null);
		}

		return (addr.getHostName());
	}


	/**
	 * Gets the temp directory for the OS.
	 *
	 * #return the temp directory for the OS
	 */
	public static String getSystemTmpDirectory () {

		if (systemTmpDir != null) {
			return (systemTmpDir);
		}
		
		String dir = getProperty("java.io.tmpdir");
		if (dir == null) {
			dir = getProperty("TMP");
		}
		if (dir == null) {
			dir = getProperty("TEMP");
		}
		if (dir == null) {
			dir = getProperty("TMPDIR");
		}

		if (dir != null) {
			systemTmpDir = dir;
			return (dir);
		}

		
		if (!isWindows()) {
			systemTmpDir = "/tmp";
			return (systemTmpDir);
		}

		String drive = getProperty("SystemDrive");
		if (drive != null) {
			char c = drive.charAt(drive.length() - 1);
			if (c != '\\' && c != '/') {
				systemTmpDir = drive + "\\TEMP";
			} else {
				systemTmpDir = drive + "TEMP";
			}

			return (systemTmpDir);
		} 

		systemTmpDir = "\\TEMP";
		return (systemTmpDir);
	}


	public static String getProperty (String key) {

		/*
		 * First make sure it is OK to do a getProperty()
		 */
		try {
			SecurityManager security = System.getSecurityManager();
			if (security != null) {
				security.checkPropertyAccess(key);
			}

		} catch (SecurityException se) {
			return (null);
		} catch (Exception e) {
			return (null);
		}

		return (System.getProperty(key));
	}


	/**
	 * This class is not meant to be instantiated.
	 */
	private Utilities () {
	}
}
