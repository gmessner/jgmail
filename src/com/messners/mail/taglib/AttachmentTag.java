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
import javax.servlet.jsp.tagext.BodyTagSupport;


/**
 *
 * @author  Gregory M. Messner <gmessner@messners.com>
 * @version $Revision: 1.2 $
 */
public class AttachmentTag extends BodyTagSupport {

    /**
     * Mime type for the attachment.
     */
    private String type = null;


    /**
     * The URL of the attachment.
     */
    private String url = null;


    /**
     * The filename of the attachment.
     */
    private String filename = null;


    /**
     * The text of the attachment help in the body of the tag
     */
    private String body;


	/*
	 * The containg MailTag instance.
	 */
	private MailTag mailTag;


  	/**
     *  @throws JSPException if any error occurs during processing of this tag
     *  @return SKIP_BODY tell JSP to not evaluate the body of this tag again
     */
    public final int doStartTag () throws JspException {

        mailTag = (MailTag)findAncestorWithClass(this, MailTag.class);
        if (mailTag == null) {
            throw new JspException(
				"Attachment tag must be nested within a mail tag.");
        }

        body = null;
        if (type != null || (filename != null && filename.length() == 0) ||
                (url != null && url.length() == 0) ) {
            return (EVAL_BODY_BUFFERED);
        }

        return (SKIP_BODY);
    }


    /**
	 *  @throws JSPException if any error occurs during processing of this tag
     *  @return SKIP_BODY tell JSP to not evaluate the body of this tag again
     */
    public final int doAfterBody () throws JspException {

		/*
		 * Save the body content of the tag for later use
		 */
        body = bodyContent.getString();
        bodyContent.clearBody();
        if (body == null) {
            body = "";
        }

        return (SKIP_BODY);
    }


    /**
     */
    public final int doEndTag () throws JspException {

		/*
		 * If type is specified then the attachment is in the body of the tag,
		 * we'll convert the body into an InputStream to attach it
		 */
/*
        if (type != null) {

			

        } else if (filename != null) {

			try {
 				mailTag.addFileAttachment(filename);
			} catch (IOException ioe) {
			}

        } else if (url != null) {

			try {
	 			mailTag.addUrlAttachment(url);
			} catch (IOException ioe) {
			}
        }
*/
        return (EVAL_PAGE);
    }


    /**
     * Sets the MIME type for the attachment that is contained 
	 * in the body of this tag
     *
     * @param type the MIME type for the attachment
     *
     */
    public final void setType (String type) {
        this.type = type;
    }


    /**
     * Sets the filename for the file to be attached.
     *
     * @param filename the name of the file to be added as an attachment
     */
    public final void setFile (String filename) {
        this.filename = filename;
    }


    /**
     * Sets the URL for the attachment.
     *
     * @param url the fully qualified URL of the attachment
     */
    public final void setUrl (String url) {
        this.url = url;
    }
}
