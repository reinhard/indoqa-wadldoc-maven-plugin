<?xml version="1.0" encoding="UTF-8"?>
<!--
   Licensed to Indoqa Software Design und Beratung GmbH (Indoqa) 
   under one or more contributor license agreements. See the NOTICE 
   file distributed with this work for additional information
   regarding copyright ownership. Indoqa licenses this file
   to you under the Apache License, Version 2.0 (the "License"); 
   you may not use this file except in compliance
   with the License. You may obtain a copy of the License at
  
    http://www.apache.org/licenses/LICENSE-2.0
  
   Unless required by applicable law or agreed to in writing,
   software distributed under the License is distributed on an
   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
   KIND, either express or implied.  See the License for the
   specific language governing permissions and limitations
   under the License.
 -->
<xsl:stylesheet 
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
 xmlns:wadl="http://research.sun.com/wadl/2006/10"
 xmlns:xs="http://www.w3.org/2001/XMLSchema"
 xmlns:html="http://www.w3.org/1999/xhtml"
 xmlns:exsl="http://exslt.org/common"
 xmlns:ns="urn:namespace"
 extension-element-prefixes="exsl"
 xmlns="http://www.w3.org/1999/xhtml"
 exclude-result-prefixes="xsl wadl xs html ns"
>
  <xsl:import href="./wadl_documentation.xsl" />

  <xsl:template match="wadl:representation[@mediaType='text/html']/wadl:doc|wadl:fault[@mediaType='text/html']/wadl:doc">
    <pre>
      <xsl:apply-templates select="node()"/>
    </pre>
  </xsl:template>
</xsl:stylesheet>
