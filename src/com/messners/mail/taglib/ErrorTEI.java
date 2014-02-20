/*
 *   Copyright (c) 2003 by Gregory M. Messner
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

package com.messners.mail.taglib;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;

/**
 * TagExtraInfo for the <b>error</b> tag.  Allows the use of 
 * &lt;jsp:getProperty/&gt; with the <b>error</b> tag script
 * variable id.
 *
 * @author  Gregory M. Messner <gmessner@messners.com>
 * @version $Revision: 1.1 $
 * @see     ErrorTag
 */
public class ErrorTEI extends TagExtraInfo {

    public final VariableInfo[] getVariableInfo (TagData data) {

		VariableInfo vinfo[] = new VariableInfo[1];
	    vinfo[0] = new VariableInfo(
			data.getAttributeString("id"),
			"com.messners.mail.taglib.ErrorTag",
			true,
			VariableInfo.NESTED);

		return (vinfo);
    }
}
