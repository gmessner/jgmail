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
import com.messners.mail.SMTPException;

import java.util.*;
import java.io.IOException;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

/**
 * The MailTag allows you to send an email using JSP.
 *
 * @author  Gregory M. Messner <gmessner@messners.com>
 * @version $Revision: 1.3 $
 */
public class MailTag extends BodyTagSupport {

	/*
	 * This SMTP instance is will contain the send mail configuration.
	 */
	private SMTP smtp = new SMTP();


    /**
     * This flag will be set if an error occurs.
     */
    private boolean isError = false;


    /**
     * If this is set to the authentication type.
     */
    private String authTypeString;
    private int authType = SMTP.AUTH_NONE;


    /**
     *
     * @return EVAL_BODY_BUFFERED informs JSP to evaluate the body of this tag
     * @throws JspException if any error occurs during processing of this tag
     */
    public final int doStartTag () throws JspException {

		if (authTypeString != null) {
			smtp.setAuthType(AuthTag.getAuthType(authTypeString));
		} else {
			smtp.setAuthType(authType);
		}

        return (EVAL_BODY_BUFFERED);
    }


	public int doEndTag () throws JspException {

		smtp = new SMTP();
		return (EVAL_PAGE);
	}


    /**
     * Sets the "to" recipients to the specified value. The value 
	 * can contain multiple addresses separated by commas.
     *
     * @param to the to address(es)
     */
    public final void setTo (String to) {
		smtp.clearToRecipients();
		smtp.addTo(null, to);
    }


    /**
     * Sets the Reply-to address.
     *
     * @param replyTo the Reply-to address for the email
     *
     */
    public final void setReplyTo (String replyTo) {
		smtp.clearReplyTo();
		smtp.addReplyTo(null, replyTo);
    }


    /**
     * Sets the from address for the email.
     *
     * @param from the from address
     *
     */
    public final void setFrom (String from) {
		smtp.setFrom(null, from);
    }


    /**
     * Sets the CC address(es) for the email. The value 
	 * can contain multiple addresses separated by commas.
     *
     * @param cc the CC address(es)
     */
    public final void setCc (String cc) {
		smtp.clearCcRecipients();
		smtp.addCc(null, cc);
    }


    /**
     * Sets the BCC address(es) for the email. The value 
	 * can contain multiple addresses separated by commas.
     *
     * @param bcc the BCC address(es)
     */
    public final void setBcc (String bcc) {
		smtp.clearBccRecipients();
		smtp.addBcc(null, bcc);
    }


    /**
     * Sets the subject for the email.
     *
     * @param subject the subject for the email
     *
     */
    public final void setSubject (String subject) {
		smtp.setSubject(subject);
    }


    /**
     * Sets the body of the email message.
     *
     * @param body the body for the email message
     */
    public final void setBody (String body) {
		smtp.setBody(body);
    }


    /**
     * Sets the login user name for SMTP authentication
     *
     * @param user login name
     *
     */
    public final void setUser (String user) {
		smtp.setUserName(user);
    }


    /**
     * Sets the password for SMTP authentication
     *
     * @param password the plain text password for authentication
     *
     */
    public final void setPassword (String password) {
		smtp.setPassword(password.toCharArray());
    }


    /**
     * Sets the SMTP server to use to send the email.
     *
     * @param server the SMTP server to use
     *
     */
    public final void setServer (String server) {
		smtp.setHost(server);
    }


    /**
     * Sets authentication type.
     *
     */
    public final void setAuthType (String value) {
		authTypeString = value;
    }


    /**
     * Sets authentication type.
     *
     */
    public final void setAuthType (int authType) {

		this.authType = authType;
    }



    /**
     * set the mime type for this email text or html
     *
     * @param value string that is the mime type for this email
     */
    public final void setType (String value) {
    }

    /**
     * add a to address to the list of to addresses for this email
     *
     * @param value  string that is an address to whom this mail is to be sent
     *
     */
    final void addTo (String value) {
		smtp.addTo(null, value);
    }

    /**
     * set a cc address to the list of cc addresses for this email
     *
     * @param value  string that is a cc address to be added to the list of cc
     *               addresses for this email
     *
     */
    protected final void addCc(String value) {
		smtp.addCc(null, value);
    }


    /**
     * Add a BCC address to the list of BCC addresses for the email message.
     *
     * @param bcc the address string containing one or more comma separated
	 * BCC addresses
     */
    protected final void addBcc (String value) {
		smtp.addBcc(null, value);
    }


	/**
	 * Adds a name/value pair to the mail header.
	 *
	 * @param name
	 * @param value 
	 */
	final void addHeader (String name, String value) {
		smtp.addHeaderField(name, value);
	}


    /**
     * Sets the error flag for this email.
     *
     * @param flag true indicates an error has occured
     */
    public final void setError (boolean flag) {
		isError = flag;
    }


	public void send (boolean inBackground) throws IOException, SMTPException {

		if (!inBackground) {
			smtp.send();
		} else {
			smtp.backgroundSend();
		}
	}
}

