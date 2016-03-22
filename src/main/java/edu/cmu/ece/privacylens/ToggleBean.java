/*
 * COPYRIGHT_BOILERPLATE
 * Copyright (c) 2013 Carnegie Mellon University
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of SWITCH nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SWITCH BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package edu.cmu.ece.privacylens;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * This class aims to allow a toggle switch instance to be defined, with text, immutable flag, value, parameter,
 * explanation, switch images, table div elements and provides an html rendering.
 */
public class ToggleBean {
    // The identifier
    private String id;

    // The text the user sees, that he will base his decision on (HTML)
    private String text;

    // Can this setting be changed?
    private boolean immutable;

    // Is the (initial) setting true or false?
    private boolean value;

    // The underlying parameter
    private String parameter;

    // A longer explanation of the parameter (HTML)
    private String explanation;

    // List of aggregated attributes
    private List<ToggleBean> members;

    // Image url to show if the parameter is true
    private String imageTrue;

    // Image url to show if the parameter is false
    private String imageFalse;

    // Image url to show for explanation
    private String explanationIcon;

    // div for the text side
    private String textDiv;

    // div for the image side
    private String imageDiv;

    // Is this object ready for display
    private boolean validated = false;

    /**
     * @return Returns the text.
     */
    public String getText() {
        return text;
    }

    /**
     * @param text The text to set.
     */
    public void setText(final String text) {
        this.text = text;
    }

    /**
     * @return Returns the immutable.
     */
    public boolean isImmutable() {
        return immutable;
    }

    /**
     * @param immutable The immutable to set.
     */
    public void setImmutable(final boolean immutable) {
        this.immutable = immutable;
    }

    /**
     * @return Returns the value.
     */
    public boolean isValue() {
        return value;
    }

    /**
     * @param value The value to set.
     */
    public void setValue(final boolean value) {
        this.value = value;
    }

    /**
     * @return Returns the parameter.
     */
    public String getParameter() {
        return parameter;
    }

    /**
     * @param parameter The parameter to set.
     */
    public void setParameter(final String parameter) {
        this.parameter = parameter;
    }

    /**
     * @return Returns the explanation.
     */
    public String getExplanation() {
        return explanation;
    }

    /**
     * @param explanation The explanation to set.
     */
    public void setExplanation(final String explanation) {
        this.explanation = explanation;
    }

    /**
     * @return Returns the imageTrue.
     */
    public String getImageTrue() {
        return imageTrue;
    }

    /**
     * @param imageTrue The imageTrue to set.
     */
    public void setImageTrue(final String imageTrue) {
        this.imageTrue = imageTrue;
    }

    /**
     * @return Returns the imageFalse.
     */
    public String getImageFalse() {
        return imageFalse;
    }

    /**
     * @param imageFalse The imageFalse to set.
     */
    public void setImageFalse(final String imageFalse) {
        this.imageFalse = imageFalse;
    }

    /**
     * @return Returns the explanationIcon.
     */
    public String getExplanationIcon() {
        return explanationIcon;
    }

    /**
     * @param explanationIcon The explanationIcon to set.
     */
    public void setExplanationIcon(final String explanationIcon) {
        this.explanationIcon = explanationIcon;
    }

    /**
     * @return Returns the textDiv.
     */
    public String getTextDiv() {
        return textDiv;
    }

    /**
     * @param textDiv The textDiv to set.
     */
    public void setTextDiv(final String textDiv) {
        this.textDiv = textDiv;
    }

    /**
     * @return Returns the imageDiv.
     */
    public String getImageDiv() {
        return imageDiv;
    }

    /**
     * @param imageDiv The imageDiv to set.
     */
    public void setImageDiv(final String imageDiv) {
        this.imageDiv = imageDiv;
    }

    /**
     * @return Returns the validated.
     */
    public boolean isValidated() {
        return validated;
    }

    private boolean _validate() {
        if (immutable) {
            if (value && imageTrue == null) {
                return false;
            }
            if (!value && imageFalse == null) {
                return false;
            }
        } else {
            if (imageTrue == null || imageFalse == null) {
                return false;
            }
        }
        if (parameter == null) {
            return false;
        }
        if (explanation != null && explanationIcon == null) {
            return false;
        }
        return true;
    }

    /**
     * Validates the object
     * 
     * @return whether object is valid
     */
    public boolean validate() {
        validated = _validate();
        return validated;
    }

    /** {@inheritDoc} */
    public String toString() {
        return "ToggleBean [text=" + text + ", immutable=" + immutable + ", value=" + value + ", parameter="
                + parameter + ", explanation=" + explanation + ", imageTrue=" + imageTrue + ", imageFalse="
                + imageFalse + ", textDiv=" + textDiv + ", imageDiv=" + imageDiv + ", validated=" + validated + "]";
    }

    private String _toHtml() {
        String html;
        final String escapedParam = StringEscapeUtils.escapeHtml(parameter);
        final StringBuilder sb = new StringBuilder();

        // text div
        sb.append("<div ");
        if (textDiv != null) {
            sb.append("id='");
            sb.append(StringEscapeUtils.escapeHtml(textDiv));
            sb.append("'");
        }
        sb.append(">");

        final String spanId = "ToggleBean-span-" + escapedParam;
        sb.append("<span id='"); // for crossing out
        sb.append(spanId);
        sb.append("'");
        if (!value) {
            sb.append(" style=\"text-decoration: line-through\"");
        }
        sb.append(">");
        sb.append(text);
        sb.append("</span>"); // useful?
        sb.append("\n");

        // tool tip
        if (explanation != null) {
            final String infoId = "ToggleBean-info-" + escapedParam;
            sb.append(String.format("<script>" + "$(function() {" + "  $(\"#%s\").dialog({" + "    autoOpen: false,"
                    + "    modal: true," + "    buttons: {" + "      \"Close\": function() {"
                    + "        $(this).dialog(\"close\");" + "      }," + "    }," + "  });" + "});" + "</script>",
                    infoId));
            sb.append("<div id=\"");
            sb.append(infoId);
            sb.append("\" title=\"Information\">");
            sb.append(explanation);
            sb.append("</div>");
            sb.append("<img src=\"");
            sb.append(explanationIcon);
            sb.append("\" onClick=\"$('#ToggleBean-info-");
            sb.append(escapedParam);
            sb.append("').dialog('open');\" style=\"vertical-align:middle\" />");
        }

        // input
        final String inputId = "ToggleBean-input-" + escapedParam;
        sb.append("<input type=\"hidden\" id=\"");
        sb.append(inputId);
        sb.append("\" name=\"input-");
        sb.append(escapedParam);
        sb.append("\" value=\"");
        sb.append(value ? '1' : '0');
        sb.append("\" />");

        sb.append("\n");

        sb.append("</div>\n");

        // image div
        sb.append("<div ");
        if (imageDiv != null) {
            sb.append("id='");
            sb.append(StringEscapeUtils.escapeHtml(imageDiv));
            sb.append("'");
        }
        sb.append(">");

        // slider
        sb.append("<img src=\"");
        if (value) {
            sb.append(imageTrue);
        } else {
            sb.append(imageFalse);
        }
        final String sliderId = "ToggleBean-control-" + escapedParam;
        sb.append("\" id=\"");
        sb.append(sliderId);
        sb.append("\" style=\"vertical-align:middle\" />");
        sb.append("</div>\n");

        // spacing div
        sb.append("<div style=\"clear:both\"></div>");
        sb.append("\n");

        // click handler
        if (!immutable) {
            sb.append(String.format("<script>" + "$('#%s').click(function() {" + "var slider=$('#%s');"
                    + "var input=$('#%s');" + "var span=$('#%s');" + "var newState=!(input.attr(\"value\") == 1);"
                    + "span.css(\"text-decoration\", newState ? \"none\" : \"line-through\");"
                    + "input.attr(\"value\", newState ? \"1\":\"0\");" + "var newImg = newState ? \"%s\" : \"%s\";"
                    + "slider.attr(\"src\", newImg);});" + "</script>", sliderId, sliderId, inputId, spanId, imageTrue,
                    imageFalse));
            sb.append("\n");
        }

        html = sb.toString();
        return html;
    }

    /**
     * Render as html
     * 
     * @return html code
     */
    public String getHtml() {
        if (!validated) {
            return "/////";

        }

        final String html = _toHtml();
        return html;
    }

    public void addMember(final ToggleBean member) {
        if (members == null) {
            members = new ArrayList<ToggleBean>();
        }
        members.add(member);
    }

    public boolean hasMembers() {
        if (members == null || members.isEmpty()) {
            return false;
        }
        return true;
    }

}