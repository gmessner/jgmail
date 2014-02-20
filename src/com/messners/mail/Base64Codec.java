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
 * This class defines static methods for encoding and decoding Base64 data
 * specified in RFC-2045.
 *
 * @author  Gregory M. Messner <gmessner@messners.com>
 * @version $Revision: 1.1 $
 */

public class Base64Codec {


	/**
	 * Maps a byte to a valid Base64 byte.
	 */
	protected static final byte[] encodeMap = {

		65, 66, 67, 68, 69, 70, 71,
		72, 73, 74, 75, 76, 77, 78,
		79, 80, 81, 82, 83, 84, 85,
		86, 87, 88, 89, 90, 
	
		 97,  98,  99, 100, 101, 102, 103,
		104, 105, 106, 107, 108, 109, 110,
		111, 112, 113, 114, 115, 116, 117,
		118, 119, 120, 121, 122,

		48, 49, 50, 51, 52, 53, 54, 55, 56, 57,

		43, 47
	};

	
	/**
	 * Used to unmap a Base64 byte.
	 */
	protected static final byte decodeMap[];
	static {

		/*
		 * Fill in the decode map
		 */
		decodeMap = new byte[128];
		for (int i = 0; i < encodeMap.length; i++) {
			decodeMap[encodeMap[i]] = (byte)i;
		}
	}


	/**
	 * This class isn't meant to be instantiated.
	 */
	private Base64Codec () {
	}


	/**
	 * This method encodes the given byte[] using the Base64 encoding
	 * specified in RFC-2045.
	 *
	 * @param  data the data to encode
	 * @return the Base64 encoded <var>data</var>
	 */
	public final static byte[] encode (byte[] data) {
		return (encode(data, -1));
	}


	/**
	 * This method encodes the given byte[] using the Base64 encoding
	 * specified in RFC-2045.
	 *
	 * @param  data  the data to encode
	 * @param  dataLength  the length of the data to encode
	 * @return the Base64 encoded <var>data</var>
	 */
	public final static byte[] encode (byte[] data, int dataLength) {

		if (data == null) {
			return (null);
		}

		if (dataLength == -1) {
			dataLength = data.length;
		}

		/*
		 * Create a buffer to hold the results
		 */
		byte dest[] = new byte[((dataLength + 2) / 3) * 4];


		/*
		 * 3-byte to 4-byte conversion and 
		 * 0-63 to ascii printable conversion
		 */
		int i, j;
		int savedDataLength = dataLength;
		dataLength = dataLength - 2;
		for (i = 0, j = 0; i < dataLength; i += 3) {

			dest[j++] = encodeMap[(data[i] >>> 2) & 077];
    		dest[j++] = encodeMap[(data[i + 1] >>> 4) & 017 |
				(data[i] << 4) & 077];
    		dest[j++] = encodeMap[(data[i + 2] >>> 6) & 003 |
				(data[i + 1] << 2) & 077];
    		dest[j++] = encodeMap[data[i + 2] & 077];
		}
	
		if (i < savedDataLength) {

			dest[j++] = encodeMap[(data[i] >>> 2) & 077];

			if (i < savedDataLength - 1) {

			    dest[j++] = encodeMap[(data[i + 1] >>> 4) & 017 |
				    (data[i] << 4) & 077];
			    dest[j++] = encodeMap[(data[i + 1] << 2) & 077];

    		} else {

			    dest[j++] = encodeMap[(data[i] << 4) & 077];
			}
		}


		/*
		 * Pad with "=" characters
		 */
		for ( ; j < dest.length; j++) {
			dest[j] = (byte)'=';
		}

		return (dest);
	}


	/**
	 * This method decodes the given byte[] using the Base64 encoding
	 * specified in RFC-2045.
	 *
	 * @param  data the Base64 encoded data to decode
	 * @return the decoded <var>data</var>
	 */
	public final static byte[] decode (byte[] data) {
		return (decode(data, -1));
	}


	/**
	 * This method decodes the given byte[] using the Base64 encoding
	 * specified in RFC-2045.
	 *
	 * @param  data the Base64 encoded data to decode
	 * @param  dataLength  the length of the data to decode
	 * @return the decoded <var>data</var>
	 */
	public final static byte[] decode (byte[] data, int dataLength) {

		if (data == null) {
			return (null);
		}

		if (dataLength == -1) {
			dataLength = data.length;
		}

		/*
		 * Remove the padding on the end
		 */
		if (dataLength < 1) {
			return (null);
		}

		while (Character.isWhitespace((char)data[dataLength - 1])) {
			dataLength--;
		}

		while (data[dataLength - 1] == '=') {
			dataLength--;
		}

		/*
		 * ASCII printable to 0-63 conversion
		 */
		int newDataLength = 0;
		for (int i = 0; i < dataLength; i++) {

			char c = (char)data[i];
			if (c != '\n' && c != '\t' && c != '\r' && c != ' ') {

				data[newDataLength] = decodeMap[c];
				newDataLength++;
			}
		}


		/*
		 * Create a buffer to hold the results
		 */
		byte dest[] = new byte[(newDataLength * 3) / 4];

	
		/*
		 * 4-byte to 3-byte conversion
		 */
		int i, j;
		int destLength = dest.length - 2;
		for (i = 0, j = 0; j < destLength; i += 4, j += 3) {

			dest[j] = (byte) (((data[i] << 2) & 255) |
			 	((data[i + 1] >>> 4) & 003));
			dest[j + 1] = (byte) (((data[i + 1] << 4) & 255) |
				((data[i + 2] >>> 2) & 017));
    		dest[j + 2] = (byte) (((data[i + 2] << 6) & 255) |
			    (data[i + 3] & 077));
		}

		if (j < dest.length) {

			dest[j] = (byte) (((data[i] << 2) & 255) |
				((data[i + 1] >>> 4) & 003));
		}

		j++;
		if (j < dest.length) {
			dest[j] = (byte) (((data[i + 1] << 4) & 255) |
				((data[i + 2] >>> 2) & 017));
		}

		return (dest);
	}
}

