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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 *
 * @author  Gregory M. Messner <gmessner@messners.com>
 * @version $Revision: 1.1 $
 */
public class FromTag extends BodyTagSupport {


	/**
     *  @throws JSPException if any error occurs during processing of this tag
     *  @return SKIP_BODY tell JSP to not evaluate the body of this tag again
     */
    public final int doAfterBody () throws JspException {

		/*
		 * Get the containing MailTag.
		 */
        MailTag mt = (MailTag)findAncestorWithClass(
						this, MailTag.class);
        if (mt == null) {
            throw new JspException(
				"from tag must be nested within a mail tag");
        }

        BodyContent body = getBodyContent();
        String from = body.getString();
        body.clearBody();

        if (from != null) {

            from = from.trim();
            if (from.length() > 0) {
                mt.setFrom(from);
                return SKIP_BODY;
            }
        }

        throw new JspException("from tag cannot be empty");
    }
}
