/*******************************************************************************
* Copyright (c) 2017 Eclipse Foundation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Frederic Gurr (Eclipse Foundation) - initial implementation
*******************************************************************************/
package org.eclipse.cbi.hipp2jipp;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Converts node/agent configs from Hudson to Jenkins<br/>
 * <br/>
 * In Hudson, all node/agent configs are stored in the main config.xml.<br/>
 * In Jenkins, there is a "nodes" directory that contains a sub folder for<br/>
 * each node/agent. Each sub folder contains a config.xml with the node/agent<br/>
 * configuration.<br/>
 * 
 * @author Frederic Gurr
 *
 */
public class NodeConverter {

    private static DocumentBuilderFactory dbf;
    private static DocumentBuilder db;

    public NodeConverter() {
    }

    public static void convert(File file) {
        convert(file, file.getParentFile());
    }

    public static void convert(File file, File outputDir) {
        // check that it's a main config file
        if (!isMainConfig(file)) {
            return;
        }
        // deal with empty outputDir
        if (outputDir == null) {
            outputDir = file.getParentFile();
        }
        // create node dir
        File nodesDir = new File(outputDir, "nodes");
        nodesDir.mkdirs();
        
        Element slaves = getSlaves(file);
        if (slaves != null) {
            NodeList childNodes = slaves.getElementsByTagName("slave");
            int nodeListLength = childNodes.getLength();
            if (nodeListLength == 0) {
                System.err.println("No slaves found.");
                return;
            }
            for (int i = 0; i < nodeListLength; i++) {
                Element slave = (Element) childNodes.item(i);
                String slaveName = slave.getElementsByTagName("name").item(0).getTextContent();
                System.out.println("Slave name: " + slaveName);
                if (slaveName != null && !slaveName.isEmpty()) {
                    File slaveDir = new File(nodesDir, slaveName);
                    slaveDir.mkdirs();
                    File slaveConfigFile = new File (slaveDir, "config.xml");
                    try {
                        slaveConfigFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    writeXML(slaveConfigFile, slave);
                } else {
                    System.err.println("Slave name is empty.");
                }
            }
        } else {
            System.err.println("No slaves found.");
        }
    }

    private static boolean isMainConfig(File file) {
        String rootNodeName = XslTransformer.getXmlRootNodeName(file);
        if (rootNodeName == null || !"hudson".equalsIgnoreCase(rootNodeName)) {
            System.err.println(file.getName() + " does not seem to be a Hudson main config.xml file.");
            return false;
        }
        return true;
    }

    private static Element getSlaves(File file) {
        try {
            InputSource is = new InputSource(); 
            is.setCharacterStream(new FileReader(file));
            dbf = DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);
            Element root = doc.getDocumentElement();
            NodeList slaves = root.getElementsByTagName("slaves");
            if (slaves != null) {
                return (Element) slaves.item(0);
            }
            return null;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void writeXML(File file, Element element) {
        System.out.print("Writing XML... ");
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db;
            db = dbf.newDocumentBuilder();
            // root element
            Document doc = db.newDocument();
            Element importedElement = (Element) doc.importNode(element, true);
            doc.appendChild(importedElement);

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            // TODO: fix indentation
            transformer.setOutputProperty("indent", "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file);
            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);
            transformer.transform(source, result);
        } catch (ParserConfigurationException e1) {
            e1.printStackTrace();
        } catch (TransformerConfigurationException e1) {
            e1.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        System.out.println("Done.");
    }

    private static void search(File file, File outputDir) {
        if (file.canRead()) {
            if (file.isDirectory() && !Files.isSymbolicLink(file.toPath())) {
                // do not search workspace, users and config-history dir
                File[] list = file.listFiles(HudsonConfigConverter.excludedDirFilter);
                // System.out.println("Searching directory ... " + file.getAbsoluteFile());
                for (File f : list) {
                    search(f, outputDir);
                }
            } else {
                if ("config.xml".equalsIgnoreCase(file.getName())) {
                    convert(file, outputDir);
                }
            }
        } else {
            System.out.println(file.getAbsoluteFile() + " -> Permission Denied");
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length < 1 || "".equals(args[0])) {
            System.err.println("Please specify a file for node conversion.");
            System.exit(1);
        }
        File file = new File(args[0]);
        File outputDir = null;
        if (args.length == 2) {
            outputDir = new File(args[1]);
        }
        if (!file.exists()) {
            System.err.println("File " + args[0] +  " does not exist.");
            System.exit(1);
        } else {
            if (file.isDirectory()) {
                search(file, outputDir);
            } else {
                convert(file, outputDir);
            }
        }
    }

}
