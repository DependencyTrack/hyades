/*
 * This file is part of Dependency-Track.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) Steve Springett. All Rights Reserved.
 */
package org.hyades.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

/**
 * This class parses CWEs and adds them to the database (if necessary).
 * cwec_v3.3.xml obtained from https://cwe.mitre.org/data/xml/cwec_v3.3.xml
 *
 * @author Steve Springett
 * @since 3.0.0
 */
public class CweImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CweImporter.class);
    private static final Map<Integer, String> CWE_MAPPINGS = new TreeMap<>();

    public static Map<Integer, String> processCweDefinitions() {
        final URL cweDictionaryUrl = CweImporter.class.getClassLoader().getResource("cwec_v4.6.xml");
        if (cweDictionaryUrl == null) {
            throw new NoSuchElementException("CWE dictionary was not found in classpath");
        }

        try (InputStream cweDictionaryInputStream = cweDictionaryUrl.openStream()) {
            LOGGER.info("Synchronizing CWEs with datastore");

            final var factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(cweDictionaryInputStream);
            final var xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();

            final XPathExpression expr1 = xpath.compile("/Weakness_Catalog/Categories/Category");
            XPathExpression expr2 = xpath.compile("/Weakness_Catalog/Weaknesses/Weakness");
            XPathExpression expr3 = xpath.compile("/Weakness_Catalog/Views/View");

            parseNodes((NodeList) expr1.evaluate(doc, XPathConstants.NODESET));
            parseNodes((NodeList) expr2.evaluate(doc, XPathConstants.NODESET));
            parseNodes((NodeList) expr3.evaluate(doc, XPathConstants.NODESET));
            LOGGER.info("CWE synchronization complete");
            return CWE_MAPPINGS;
        } catch (Exception ex) {
            LOGGER.error("An issue occured with reading xml file for cwe information: "+ex.getClass().getCanonicalName());
            LOGGER.error(ex.getMessage());
            LOGGER.error(Arrays.toString(ex.getStackTrace()));
            return null;
        }
    }


    private static void parseNodes(NodeList nodeList) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            NamedNodeMap attributes = node.getAttributes();
            Integer id = Integer.valueOf(attributes.getNamedItem("ID").getNodeValue());
            String desc = attributes.getNamedItem("Name").getNodeValue();
            CWE_MAPPINGS.put(id, desc);
        }
    }

}