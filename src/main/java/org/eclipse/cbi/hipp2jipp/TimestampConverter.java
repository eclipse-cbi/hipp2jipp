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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
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
 * Converts timestamps of builds<br/>
 * <br/>
 * Apparently, this is not needed anymore in newer Jenkins versions<br/>
 * 
 * @author Frederic Gurr
 *
 */
public class TimestampConverter {

    // TODO: check if timestamp tag already exists
    public static void convertBuildTimestamp(File file) {
        System.out.println("Trying to fix missing build time stamp...");
        if (containsTimestampTag(file)) {
            System.out.println("File already contains a timestamp tag, skipping...");
            return;
        }
        HudsonConfigConverter.createBackupFile(file, ".bak2");
        // System.out.println("File: " + file.getPath());
        // convert parent directory name to time stamp
        String parentDirName = file.getAbsoluteFile().getParentFile().getName();
        long epoch = convertTimestampToEpoch(parentDirName);
        if (epoch != -1) {
            writeBuildTimestampToXml(file, epoch);
        }
    }

    /* package private for tests */
    static long convertTimestampToEpoch(String timestamp) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        long epoch = -1;
        try {
            Date date = df.parse(timestamp);
            epoch = date.getTime();
            System.out.print(" " + timestamp + " -> epoch date: " + epoch);
        } catch (ParseException e) {
            System.out.println(" -> Parsing exception. Skipping conversion...");
        }
        return epoch;
    }

    private static void writeBuildTimestampToXml(File file, long timestamp) {
        try {
            InputSource is = new InputSource(); 
            is.setCharacterStream(new FileReader(file));
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);
            Element root = doc.getDocumentElement();
            if ("build".equalsIgnoreCase(root.getNodeName())){
                // add time stamp element
                Element timestampElement = doc.createElement("timestamp");
                timestampElement.appendChild(doc.createTextNode(String.valueOf(timestamp)));
                root.appendChild(timestampElement);
                // add start time element
                Element startTimeElement = doc.createElement("startTime");
                startTimeElement.appendChild(doc.createTextNode(String.valueOf(timestamp+5)));
                root.appendChild(startTimeElement);

                // Write XML file
                DOMSource source = new DOMSource(doc);
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                transformerFactory.setAttribute("indent-number", 2); // does not work
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                //transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                transformer.setOutputProperty("indent", "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                // String outputFileName = file.getAbsoluteFile().getParentFile().getAbsolutePath() + "/build.timestamp.xml";
                String outputFileName = file.getAbsolutePath();
                StreamResult result = new StreamResult(outputFileName);
                transformer.transform(source, result);
                System.out.println(" -> fixed.");
            } else {
                System.err.println("Wrong rootNodeName: " + root.getNodeName());
            }
        } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length < 1 || "".equals(args[0])) {
            System.err.println("Please specify a file for timestamp conversion.");
            System.exit(1);
        }
        File file = new File(args[0]);
        if (!file.exists()) {
            System.err.println("File " + args[0] +  " does not exist.");
            System.exit(1);
        } else {
            if (file.isDirectory()) {
                search(file);
            } else {
                convertBuildTimestamp(file);
            }
        }
    }

    /**
     * Search recursively for build XMLs
     * 
     * Most likely the given directory will be the jobs or the Jenkins root
     * directory. A few directories are excluded from the search like
     * "workspace" or "archive".
     * 
     * @param file
     *            directory
     */
    private static void search(File file) {
        if (file.canRead()) {
            if (file.isDirectory() && !Files.isSymbolicLink(file.toPath())) {
                // do not search workspace, users and config-history dir
                File[] list = file.listFiles(HudsonConfigConverter.excludedDirFilter);
                // System.out.println("Searching directory ... " + file.getAbsoluteFile());
                for (File f : list) {
                    search(f);
                }
            } else {
                if ("build.xml".equalsIgnoreCase(file.getName())) {
                    convertBuildTimestamp(file);
                }
            }
        } else {
            System.out.println(file.getAbsoluteFile() + " -> Permission Denied");
        }
    }

    public static boolean containsTimestampTag(File file) {
        try {
            InputSource is = new InputSource(); 
            is.setCharacterStream(new FileReader(file));
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);
            Element root = doc.getDocumentElement();
            NodeList timestamp = root.getElementsByTagName("timestamp");
            return timestamp.getLength() > 0;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
