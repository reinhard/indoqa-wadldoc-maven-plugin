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

import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.cocoon.pipeline.NonCachingPipeline;
import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.pipeline.component.sax.FileGenerator;
import org.apache.cocoon.pipeline.component.sax.XMLSerializer;
import org.apache.cocoon.pipeline.component.sax.XSLTTransformer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

public class Wadl2HtmlPipeline {

    private Pipeline pipeline;
    private final URL wadl;
    private final boolean escapeHtmlRepresentations;
    private String stylesheet;

    public Wadl2HtmlPipeline(URL wadlUrl, String stylesheet, boolean escapeHtmlRepresentations) {
        Validate.notNull(wadlUrl, "A WADL URL object has to be passed.");
        this.wadl = wadlUrl;
        this.stylesheet = stylesheet;
        this.escapeHtmlRepresentations = escapeHtmlRepresentations;
    }

    public Wadl2HtmlPipeline(URL wadlUrl, String stylesheet) {
        this(wadlUrl, stylesheet, true);
    }

    private void setup() throws PipelineException {
        try {
            this.pipeline = new NonCachingPipeline();

            // start with the WADL file
            this.pipeline.addComponent(new FileGenerator(this.wadl));

            // escape HTML representations
            if (this.escapeHtmlRepresentations) {
                this.pipeline.addComponent(new EscapingTransformer());
                URL resource = this.getClass().getClassLoader().getResource(
                        "com/indoqa/maven/wadldoc/stylesheet/wadl_documentation_html-reps.xsl");
                this.pipeline.addComponent(new XSLTTransformer(resource, null));
            } else {
                URL resource = this.getClass().getClassLoader().getResource(
                        "com/indoqa/maven/wadldoc/stylesheet/wadl_documentation.xsl");
                this.pipeline.addComponent(new XSLTTransformer(resource, null));
            }

            // CSS
            if (StringUtils.isNotBlank(this.stylesheet)) {
                this.pipeline.addComponent(new StylesheetTransformer(this.stylesheet));
            }

            // serialization
            Properties properties = new Properties();
            properties.put("method", "html");
            this.pipeline.addComponent(new XMLSerializer(properties));
        } catch (Exception e) {
            throw new PipelineException(e);
        }
    }

    public void execute(OutputStream os) throws PipelineException {
        try {
            this.setup();
            this.pipeline.setup(os);
            this.pipeline.execute();
        } catch (Exception e) {
            throw new PipelineException(e);
        }
    }

    public static class PipelineException extends RuntimeException {

        public PipelineException(String message, Throwable cause) {
            super(message, cause);
        }

        public PipelineException(Throwable cause) {
            super(cause);
        }
    }
}
