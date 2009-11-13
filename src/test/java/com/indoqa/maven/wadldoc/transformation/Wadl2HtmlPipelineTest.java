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

import java.io.ByteArrayOutputStream;
import java.net.URL;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.junit.Test;

public class Wadl2HtmlPipelineTest {

    @Test(expected = IllegalArgumentException.class)
    public void constructPipelineWithURL() {
        new Wadl2HtmlPipeline((URL) null, null);
    }

    @Test
    public void simplePipeline() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new Wadl2HtmlPipeline(this.getClass().getResource("wadl.xml"), null, true).execute(baos);

        Assert.assertNotNull(baos);
        Diff diff = createDiff("test1-result.html", baos);
        Assert.assertTrue("Pieces of XML are not identical. " + diff, diff.identical());
    }

    @Test
    public void stylesheet() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new Wadl2HtmlPipeline(this.getClass().getResource("wadl.xml"), "stylesheet.css", true).execute(baos);

        Assert.assertNotNull(baos);
        Diff diff = createDiff("test2-result.html", baos);
        Assert.assertTrue("Pieces of XML are not identical. " + diff, diff.similar());
    }

    private static Diff createDiff(String fileName, ByteArrayOutputStream actual) throws Exception {
        return createDiff(Wadl2HtmlPipelineTest.class.getResource(fileName), actual);
    }

    private static Diff createDiff(URL expected, ByteArrayOutputStream actual) throws Exception {
        String string1 = IOUtils.toString(expected.openStream());
        String string2 = actual.toString();

        return new Diff(string1, string2);
    }
}
