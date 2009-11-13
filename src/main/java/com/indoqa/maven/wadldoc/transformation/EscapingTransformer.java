/*
 * Licensed to Indoqa Software Design und Beratung GmbH (Indoqa) 
 * under one or more contributor license agreements. See the NOTICE 
 * file distributed with this work for additional information
 * regarding copyright ownership. Indoqa licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.indoqa.maven.wadldoc.transformation;

import org.apache.cocoon.sax.AbstractSAXTransformer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This transformer escapes HTML representations.
 */
public class EscapingTransformer extends AbstractSAXTransformer {

    private static final String ATT_MEDIA_TYPE = "mediaType";
    private static final String ATT_VALUE_MEDIA_TYPE_HTML = "text/html";
    private static final String EL_DOC = "doc";
    private static final String EL_REPRESENTATION = "representation";
    private static final String NS_WADL = "http://research.sun.com/wadl/2006/10";

    private boolean inDocElement;
    private boolean inRepMediaTypeHtml;

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        if (EL_DOC.equals(localName) && NS_WADL.equals(uri)) {
            this.inDocElement = false;
        }

        if (this.inRepMediaTypeHtml && EL_REPRESENTATION.equals(localName) && NS_WADL.equals(uri)) {
            this.inRepMediaTypeHtml = false;
        }

        if (this.inDocElement && this.inRepMediaTypeHtml) {
            this.escapeEndElement(name);
        } else {
            super.endElement(uri, localName, name);
        }
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
        if (this.inDocElement && this.inRepMediaTypeHtml) {
            this.escapeStartElement(name, atts);
        } else {
            super.startElement(uri, localName, name, atts);
        }

        if (EL_DOC.equals(localName) && NS_WADL.equals(uri)) {
            this.inDocElement = true;
        }
        if (EL_REPRESENTATION.equals(localName) && NS_WADL.equals(uri)) {
            String mediaType = atts.getValue(ATT_MEDIA_TYPE);
            if (mediaType != null && mediaType.startsWith(ATT_VALUE_MEDIA_TYPE_HTML)) {
                this.inRepMediaTypeHtml = true;
            }
        }

    }

    private void escapeEndElement(String name) throws SAXException {
        char[] chars = ("</" + name + ">").toCharArray();
        this.characters(chars, 0, chars.length);
    }

    private void escapeStartElement(String name, Attributes atts) throws SAXException {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(name);
        for (int i = 0; i < atts.getLength(); i++) {
            sb.append(" ");
            sb.append(atts.getQName(i));
            sb.append("=\"");
            sb.append(atts.getValue(i));
            sb.append("\"");
        }
        sb.append(">");

        char[] chars = sb.toString().toCharArray();
        this.characters(chars, 0, chars.length);
    }
}
