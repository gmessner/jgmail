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

import com.messners.mail.SMTPException;

import java.io.IOException;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

/**
 *
 * @author  Gregory M. Messner <gmessner@messners.com>
 * @version $Revision: 1.2 $
 */
public class SendTag extends BodyTagSupport {

	/**
	 * Indicates whether the mail should be sent in the background or not.
	 * Defaults to <code>true</code>.
	 */
	private boolean background = true;


	/**
	 * Holds the error from a send,
	 */
	private Exception error;


	/**
     *  @throws JSPException if any error occurs during processing of this tag
     *  @return SKIP_BODY
     */
    public final int doStartTag () throws JspException {

		/*
		 * Get the containing MailTag.
		 */
        MailTag mt = (MailTag)findAncestorWithClass(
						this, MailTag.class);
        if (mt == null) {
            throw new JspException(
				"server tag must be nested within a mail tag");
        }


		try {
			mt.send(background);
		} catch (IOException ioe) {
			mt.setError(true);
			error = ioe;
           	throw new JspException("I/O error: " + ioe.getMessage());
		} catch (SMTPException se) {
			mt.setError(true);
			error = se;
           	throw new JspException("SMTP error: " + se.getMessage());
		}

	    return (SKIP_BODY);
    }


	/**
     *  @throws JSPException if any error occurs during the processing of 
	 *  the body of this tag
     *  @return EVAL_PAGE 
     */
    public final int doEndTag () throws JspException {

		/*
		 * Did an error occur with initial sending of the email, if
		 * so output the error message in the body
		 */
		if (error != null) {

			try {

	       		if (bodyContent != null) {
		    		bodyContent.writeOut(bodyContent.getEnclosingWriter());
				}

		    } catch (IOException ioe) {
				throw new JspException("I/O Error: " + ioe.getMessage());
			}
			
	    	error = null;
		}
	
		return (EVAL_PAGE);
    }


    /**
     * Sets the do in background flag.
     */
    public final void setBackground (String value) {
		background = new Boolean(value).booleanValue();
    }


    /**
     */
    public final Exception getError () {
        return (error);
    }
}


