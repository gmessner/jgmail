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
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 *
 * @author  Gregory M. Messner <gmessner@messners.com>
 * @version $Revision: 1.1 $
 */
public class AddRecipientTag extends BodyTagSupport {


	public static final String TO  = "to";
	public static final String CC  = "cc";
	public static final String BCC = "bcc";

		
	/**
	 * The recipient email address to add.
	 */
	private String address;

	
	/**
	 * The type of recipient to add, must be "to", "cc", or "bcc".
	 */
	private String type;
  
  
	/**
	 *  @throws JSPException if any error occurs during processing of this tag
	 *  @return SKIP_BODY tell JSP to not evaluate the body of this tag again
	 */
	public final int doStartTag () throws JspException {

		if (address != null && address.length() > 0 ) {
			  addRecipient(address);
			  return (SKIP_BODY);
		}

		return (EVAL_BODY_BUFFERED);
	}

	
	public final int doAfterBody () throws JspException {
			
		BodyContent body = getBodyContent();
		String address = body.getString();
		body.clearBody();
		
		if (address != null && address.length() > 0 ) {
			addRecipient(address);
			return (SKIP_BODY);
		} else {
			throw new JspException(
					"addRecpient must have an address specified");
		  }
	}


	/**
	 * Sets the type of recipient to add. Must be one of the following:
	 * <pre>
	 * 	to
	 * 	cc
	 * 	bcc
	 * </pre>
	 *
	 * @param type the recipient type to add
	 */
	public final void setType (String type) throws JspTagException {

		type = type.trim();
		if (TO.equalsIgnoreCase(type)) {
			this.type = TO;
		} else if (CC.equalsIgnoreCase(type)) {
			this.type = CC;
		} else if (BCC.equalsIgnoreCase(type)) {
			this.type = BCC;
		} else {
			throw new JspTagException("invalid recipient type specified");
		}
	}


	/**
	 * Sets email address of the recipient.
	 *
	 * @param address the recipient email address
	 */
	public final void setAddress (String address) {
		this.address = address.trim();
	}


	private void addRecipient (String address) throws JspTagException {

		/*
		 * Get the containing MailTag.
		 */
		MailTag mt = (MailTag)findAncestorWithClass(this, MailTag.class);
		if (mt == null) {
			throw new JspTagException(
				"addRecipient tag must be nested within a mail tag");
		}

		if (TO.equalsIgnoreCase(type)) {
			mt.addTo(address);
		} else if (CC.equalsIgnoreCase(type)) {
			mt.addCc(address);
		} else if (BCC.equalsIgnoreCase(type)) {
			mt.addBcc(address);
		} else {
			throw new JspTagException("invalid recipient type specified");
		}
	}
}
