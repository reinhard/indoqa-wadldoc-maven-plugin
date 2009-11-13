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
import org.apache.cocoon.xml.sax.AttributesImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class StylesheetTransformer extends AbstractSAXTransformer {

    private static final String EL_STYLE = "style";
    private static final String EL_LINK = "link";
    private static final String EL_HEAD = "head";
    private static final String NS_HTML = "http://www.w3.org/1999/xhtml";

    private boolean inStyle;
    private final String stylesheet;

    public StylesheetTransformer(String stylesheet) {
        this.stylesheet = stylesheet;
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
        if (EL_STYLE.equalsIgnoreCase(localName) && atts.getIndex("type") >= 0) {
            this.inStyle = true;
            return;
        }

        super.startElement(uri, localName, name, atts);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (this.inStyle) {
            return;
        }
        super.characters(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        if (this.inStyle && EL_STYLE.equalsIgnoreCase(localName)) {
            this.inStyle = false;
            return;
        }

        if (EL_HEAD.equals(localName)) {
            AttributesImpl atts = new AttributesImpl();
            atts.addCDATAAttribute("rel", "stylesheet");
            atts.addCDATAAttribute("type", "text/css");
            atts.addCDATAAttribute("href", this.stylesheet);
            super.startElement(NS_HTML, EL_LINK, EL_LINK, atts);
            super.endElement(NS_HTML, EL_LINK, EL_LINK);
        }

        super.endElement(uri, localName, name);
    }
}
