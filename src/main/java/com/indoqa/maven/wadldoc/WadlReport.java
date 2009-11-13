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
import java.util.Locale;

import org.apache.maven.doxia.sink.render.RenderingContext;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.sink.Sink;

/**
 * @goal restapi
 * @requiresDependencyResolution runtime
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public class WadlReport extends AbstractWadlDocumentationMojo implements MavenReport {

    /**
     * Specifies the destination directory where the REST javadoc saves the generated HTML files.
     * 
     * @parameter expression="${reportOutputDirectory}"
     *            default-value="${project.reporting.outputDirectory}/restapidocs"
     * @required
     */
    private File reportOutputDirectory;

    /**
     * The name of the destination directory. <br/>
     * 
     * @parameter expression="${destDir}" default-value="restapidocs"
     */
    private String destDir;

    /**
     * Specifies whether the build will continue even if there are errors.
     * 
     * @parameter expression="${maven.restapidocs.failOnError}" default-value="true"
     */
    protected boolean failOnError;

    public boolean canGenerateReport() {
        return true;
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            RenderingContext context = new RenderingContext(this.outputDirectory, this.getOutputName() + ".html");
            this.generate(new SiteRendererSink(context), Locale.ENGLISH);
        } catch (Exception e) {
            String message = "An error has occurred in " + this.getName(Locale.ENGLISH) + " report generation:"
                    + e.getMessage();
            if (this.failOnError) {
                throw new MojoFailureException(message);
            }
            this.getLog().error(message, e);
        }
    }

    public void generate(Sink sink, Locale locale) throws MavenReportException {
        this.outputDirectory = this.getReportOutputDirectory();
        this.executeReport(locale);
    }

    public String getCategoryName() {
        return CATEGORY_PROJECT_REPORTS;
    }

    public String getDescription(Locale locale) {
        return "The documentation of the REST API based on WADL.";
    }

    public String getName(Locale locale) {
        return "REST Api";
    }

    public String getOutputName() {
        return this.destDir + "/index";
    }

    public File getReportOutputDirectory() {
        if (this.reportOutputDirectory == null) {
            return this.outputDirectory;
        }

        return this.reportOutputDirectory;
    }

    public boolean isExternalReport() {
        return true;
    }

    public void setReportOutputDirectory(File reportOutputDirectory) {
        this.updateReportOutputDirectory(reportOutputDirectory, this.destDir);
    }

    public void setDestDir(String destDir) {
        this.destDir = destDir;
        this.updateReportOutputDirectory(this.reportOutputDirectory, destDir);
    }

    private void updateReportOutputDirectory(File reportOutputDirectory, String destDir) {
        if (reportOutputDirectory != null && destDir != null
                && !reportOutputDirectory.getAbsolutePath().endsWith(destDir)) {
            this.reportOutputDirectory = new File(reportOutputDirectory, destDir);
        } else {
            this.reportOutputDirectory = reportOutputDirectory;
        }
    }
}
