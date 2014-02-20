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

import java.io.IOException;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

/**
 *
 * @author  Gregory M. Messner <gmessner@messners.com>
 * @version $Revision: 1.1 $
 */
public class ErrorTag extends BodyTagSupport {

	private Exception exception;
	private boolean dumpException;

    /**
     *  implementation of the method from the tag interface that tells the JSP
     *  page what to do when the start tag is encountered
     *
     *  @throws JSPException  thrown when an error occurs while processing the
     *                        body of this method
     *
     *  @return - SKIP_BODY if no errors exist, EVAL_BODY_BUFFERED 
	 *  if errors do exist
     *
     */
    public final int doStartTag() throws JspException {

		SendTag st = (SendTag)findAncestorWithClass(this, SendTag.class);
		if (st == null) {
	   		throw new JspException(
				"error tag must be nested within a send tag");
		} 
		
		/*
		 * If no exception when the email was sent then return SKIP_BODY
		 */
		exception = st.getError();
		if (exception == null) {
			return (SKIP_BODY);
		}

		/*
		 * Save this so that it can ge used as a script variable
		 */
		pageContext.setAttribute(id, this, PageContext.PAGE_SCOPE);

		return (EVAL_BODY_BUFFERED);
    }


   	/**
     *  @throws JSPException if any error occurs during processing of this tag
     *  @return EVAL_BODY_BUFFERED
     */
    public final int doAfterBody () throws JspException {

		if (exception == null) {
		    return (SKIP_BODY);
		}

		return (EVAL_BODY_BUFFERED);
    }


    public final int doEndTag () throws JspException {

		try {

		    if (bodyContent != null) {
				bodyContent.writeOut(bodyContent.getEnclosingWriter());
			}

		} catch (IOException ioe) {
	   		throw new JspException("I/O Error: " + ioe.getMessage());
		}

		return (EVAL_PAGE);
    }


    /**
     * <p>Gets the message from the error.  Use jsp:getProperty as follows:</p>
     * 
     * &lt;jsp:getProperty name=<i>"id"</i> property="error"/&gt;
     *
     * @return the message from the error (exception)
     */
    public final String getError () {

		if (exception == null) {
			return (null);
		}

		return (exception.getMessage());
    }


    /**
     * Releases the script variable at end of error tag.
     */
    public final void release () {

		if (id != null && id.length() > 0) {
			pageContext.removeAttribute(id, PageContext.PAGE_SCOPE);
		}
    }
}
