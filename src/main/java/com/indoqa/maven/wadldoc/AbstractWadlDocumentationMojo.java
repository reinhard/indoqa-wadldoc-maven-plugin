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
package com.indoqa.maven.wadldoc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.antlr.stringtemplate.StringTemplate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.reporting.MavenReportException;
import org.xml.sax.InputSource;

import com.indoqa.maven.wadldoc.transformation.Wadl2HtmlPipeline;

/**
 * @requiresDependencyResolution runtime
 * @since 1.0.0
 */
public abstract class AbstractWadlDocumentationMojo extends AbstractMojo {

    /**
     * Specifies whether HTML representations should be escaped.
     * 
     * @parameter expression="${maven.restapidocs.escape-html-representations}" default-value="true"
     */
    protected boolean escapeHtmlRepresentations;

    /**
     * Specifies the input directory where the WADL files are located.
     * 
     * @parameter expression="${maven.restapidocs.wadlDir}"
     *            default-value="${project.basedir}/src/main/wadl"
     * @required
     */
    protected File inputDirectory;

    /**
     * Specifies the destination directory where to save the generated HTML files.
     * 
     * @parameter expression="${destDir}" alias="destDir"
     *            default-value="${project.build.directory}/restapidocs"
     * @required
     */
    protected File outputDirectory;

    /**
     * Specifies whether the REST API docs generation should be skipped.
     * 
     * @parameter expression="${maven.restapidocs.skip}" default-value="false"
     */
    protected boolean skip;

    /**
     * Specifies the CSS file.
     * 
     * @parameter expression="${maven.restapidocs.stylesheet}"
     */
    protected File stylesheet;

    /**
     * Specifies the title.
     * 
     * @parameter expression="${maven.restapidocs.title}" default-value="REST API documentation"
     */
    private String title;

    protected void executeReport(Locale unusedLocale) throws MavenReportException {
        if (this.skip) {
            this.getLog().info("Skipping javadoc generation");
            return;
        }

        // make sure that the output directory exists
        this.outputDirectory.mkdirs();

        // fix for some Maven classloading problems in conjunction with JAXP that
        // relies on a set thread context classloader ...
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

        this.getLog().debug("Reading WADL files from: " + this.inputDirectory);
        @SuppressWarnings("unchecked")
        Collection<File> wadlFiles = FileUtils.listFiles(this.inputDirectory, new String[] {"xml", "wadl"}, false);

        if (wadlFiles.size() <= 0) {
            this.getLog().info("No WADL files. Nothing to do.");
        }

        // WADL to HTML transformation
        this.transformWadlDocuments2HTMLDocuments(wadlFiles);

        // copy stylesheet
        this.copyStylesheet();

        // index page
        HtmlDocument startPage = this.writeIndexPage(wadlFiles);

        // frameset
        this.writeFrameset(wadlFiles, startPage);
    }

    protected void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    private void copyStylesheet() throws MavenReportException {
        if (this.stylesheet != null) {
            if (!this.stylesheet.exists()) {
                throw new MavenReportException("The referred stylesheet " + this.stylesheet + " doesn't exist.");
            }

            try {
                FileUtils.copyFile(this.stylesheet, new File(this.outputDirectory, this.stylesheet.getName()));
            } catch (IOException e) {
                throw new MavenReportException("Can't copy stylesheet " + this.stylesheet, e);
            }
        }
    }

    private String createIndexFileContent(List<HtmlDocument> htmlDocuments) throws MavenReportException {
        try {
            StringTemplate stringTemplate = new StringTemplate(IOUtils.toString(this.getClass().getResourceAsStream(
                    "resources.html")));
            if (this.stylesheet != null) {
                stringTemplate.setAttribute("stylesheet", this.stylesheet.getName());
            }
            stringTemplate.setAttribute("title", this.title);
            stringTemplate.setAttribute("resources", htmlDocuments);
            return stringTemplate.toString();
        } catch (IOException e) {
            throw new MavenReportException("Can't create file resources.html", e);
        }
    }

    private File createOutFile(File wadlFile) {
        return new File(this.outputDirectory, createOutFileName(wadlFile));
    }

    private void transformWadl2Html(File wadlFile) throws MavenReportException {
        OutputStream outputStream;
        try {
            File outFile = this.createOutFile(wadlFile);
            outputStream = new FileOutputStream(outFile);

            this.getLog().debug("Transforming " + wadlFile + " to " + outFile);
        } catch (FileNotFoundException e) {
            throw new MavenReportException("Can't create file.", e);
        }

        Wadl2HtmlPipeline pipeline;
        try {
            String stylesheetName = null;
            if (this.stylesheet != null) {
                stylesheetName = this.stylesheet.getName();
            }
            pipeline = new Wadl2HtmlPipeline(wadlFile.toURI().toURL(), stylesheetName, this.escapeHtmlRepresentations);
        } catch (MalformedURLException e) {
            throw new MavenReportException("Can't create URL object from " + wadlFile + ".", e);
        }

        pipeline.execute(outputStream);
    }

    private void transformWadlDocuments2HTMLDocuments(Collection<File> wadlFiles) throws MavenReportException {
        for (File wadlFile : wadlFiles) {
            this.transformWadl2Html(wadlFile);
        }
    }

    private void writeFrameset(Collection<File> wadlFiles, HtmlDocument startPage) throws MavenReportException {
        FileWriter fw = null;
        try {
            StringTemplate stringTemplate = new StringTemplate(IOUtils.toString(this.getClass().getResourceAsStream(
                    "index.html")));
            stringTemplate.setAttribute("title", this.title);
            stringTemplate.setAttribute("startPagePath", startPage.getPath());
            fw = new FileWriter(new File(this.outputDirectory, "index.html"));
            fw.write(stringTemplate.toString());
        } catch (Exception e) {
            throw new MavenReportException("Can't create frameset.html", e);
        } finally {
            IOUtils.closeQuietly(fw);
        }
    }

    private HtmlDocument writeIndexPage(Collection<File> wadlFiles) throws MavenReportException {
        // XPath factory and namespace context
        XPathExpression expression;
        try {
            XPath xpath = XPathFactory.newInstance(XPathFactory.DEFAULT_OBJECT_MODEL_URI).newXPath();
            xpath.setNamespaceContext(new WadlNamespaceContext());
            expression = xpath.compile("/w:application/w:doc/@title");
        } catch (Exception e) {
            throw new MavenReportException("Can't create xpath factory.", e);
        }
        List<HtmlDocument> htmlDocuments = new ArrayList<HtmlDocument>();
        for (File wadlFile : wadlFiles) {
            htmlDocuments.add(new HtmlDocument(getWadlDocumentName(expression, wadlFile), createOutFileName(wadlFile)));
        }
        Collections.sort(htmlDocuments, new Comparator<HtmlDocument>() {

            public int compare(HtmlDocument d1, HtmlDocument d2) {
                return d1.getName().compareTo(d2.getName());
            }
        });
        File f = new File(this.outputDirectory, "resources.html");
        FileWriter fw = null;
        try {
            fw = new FileWriter(f);
            fw.write(this.createIndexFileContent(htmlDocuments));
        } catch (IOException e) {
            throw new MavenReportException("Can't create index.html", e);
        } finally {
            IOUtils.closeQuietly(fw);
        }

        return htmlDocuments.get(0);
    }

    private static String createOutFileName(File wadlFile) {
        return FilenameUtils.getBaseName(wadlFile.getName()) + ".html";
    }

    private static String getWadlDocumentName(XPathExpression expression, File wadlFile) throws MavenReportException {
        try {
            return expression.evaluate(new InputSource(new FileInputStream(wadlFile)));
        } catch (Exception e) {
            throw new MavenReportException("Can't evaluate XPath expression on " + wadlFile, e);
        }
    }

    private static class HtmlDocument {

        private String name;
        private String path;

        public HtmlDocument(String name, String path) {
            super();
            this.name = name;
            this.path = path;
        }

        public String getName() {
            return this.name;
        }

        public String getPath() {
            return this.path;
        }
    }

    private static class WadlNamespaceContext implements NamespaceContext {

        public String getNamespaceURI(String prefix) {
            if ("w".equals(prefix)) {
                return "http://research.sun.com/wadl/2006/10";
            }

            return XMLConstants.NULL_NS_URI;
        }

        public String getPrefix(String namespace) {
            if ("http://research.sun.com/wadl/2006/10".equals(namespace)) {
                return "w";
            }

            return null;
        }

        public Iterator<String> getPrefixes(String namespaceURI) {
            return null;
        }
    }
}
