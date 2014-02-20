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

import com.messners.mail.SMTP;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 *
 * @author  Gregory M. Messner <gmessner@messners.com>
 * @version $Revision: 1.1 $
 */
public class AuthTag extends BodyTagSupport {

    /**
     * The auth type.
     */
    private String authType;


	/**
	 * The user name.
	 */
	private String userName;


	/**
	 * The password.
	 */
	private String password;


	/**
     *  @throws JSPException if any error occurs during processing of this tag
     *  @return SKIP_BODY tell JSP to not evaluate the body of this tag again
     */
    public final int doStartTag () throws JspException {

		/*
		 * Get the containing MailTag.
		 */
        MailTag mt = (MailTag)findAncestorWithClass(
						this, MailTag.class);
        if (mt == null) {
            throw new JspException(
				"Auth tag must be nested within a mail tag");
        }

		mt.setAuthType(getAuthType(authType));
		mt.setUser(userName);
		mt.setPassword(password);

		return (SKIP_BODY);
	}


    /**
     */
    public final static int getAuthType (String authType) throws JspException {

		if (authType == null) {
			return (SMTP.AUTH_NONE);
		}

		authType = authType.toLowerCase().trim();
				
		if (authType.length() == 0 || "none".equals(authType)) {
			return (SMTP.AUTH_NONE);
		} else if ("login".equals(authType)) {
			return (SMTP.AUTH_LOGIN);
		} else if ("plain".equals(authType)) {
			return (SMTP.AUTH_PLAIN);
		} else {
			throw new JspException("invlaid SMTP auth type: " + authType);
		}
    }


    /**
     * Sets the auth type. Must be none, login, or plain.
     *
     * @param type  the auth type
     */
    public final void setType (String type) {
		authType = type;
    }


	/**
	 * Sets the user name for the SMTP authorization.
	 *
	 * @param user the user name
	 */
	public void setUser (String user) {
		userName = user;
	}


	/**
	 * Sets the password for the SMTP authorization.
	 *
	 * @param password the password
	 */
	public void setPassword (String password) {
		this.password = password;
	}
}
