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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Transforms the given XML files according to the specified XSL file
 * 
 * @author Frederic Gurr
 *
 */
public class XslTransformer {

    private static final String GENERAL_CONFIG_XSL = "config.main.xsl";
    private static final String JOB_CONFIG_XSL = "config.job.xsl";
    private static final String BUILD_XSL = "build.xsl";

    public static final String XSL_DIR = "xsl";

    private TransformerFactory transformerFactory = TransformerFactory.newInstance();

    public boolean transform(File inputFile, File outputFile, String xslFileName, Properties xslParameters) {
        InputStream is = null;
        OutputStream os = null;
        try {
            outputFile.createNewFile();
            is = new FileInputStream(inputFile);
            os = new FileOutputStream(outputFile);

            // InputStream xsl = new FileInputStream(new File(XSL_DIR, getXslFileName(inputFile)));
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            InputStream xsl = classloader.getResourceAsStream(xslFileName);

            Transformer transformer = transformerFactory.newTransformer(new StreamSource(xsl));
            //TODO: Fix indentation
            // when indent is off during the first transformation and on during the second transformation, it works
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty("indent", "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            // set optional XSL parameters
            if (xslParameters != null && !xslParameters.isEmpty()) {
                for (String key : xslParameters.stringPropertyNames()) {
                    transformer.setParameter(key, xslParameters.getProperty(key));
                }
            }
            transformer.transform(new StreamSource(is), new StreamResult(os));
        } catch (IOException | TransformerException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public boolean transform(File inputFile, File outputFile) {
        String xslFileName = getXslFileName(inputFile);
        if (xslFileName == null) {
            System.err.println(inputFile + " cannot be converted.");
            return false;
            // Skip general config files
        } else if (GENERAL_CONFIG_XSL.equalsIgnoreCase(xslFileName)) {
            System.out.println("Skipping main config file!");
            return false;
        }
        return transform(inputFile, outputFile, XSL_DIR + "/" + xslFileName, null);
    }

    public boolean transform(File inputFile) {
        try {
            File tempFile = File.createTempFile(inputFile.getName(), "tmp", inputFile.getAbsoluteFile().getParentFile());
            boolean successful = transform(inputFile, tempFile);
            if (!successful) {
                return false;
            } else if (!tempFile.exists() || tempFile.length() == 0) {
                System.err.println("Error during creation of temp file.");
                return false;
            }
            inputFile.delete();
            Files.copy(tempFile.toPath(), inputFile.toPath());
            tempFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static String getXslFileName(File inputFile) {
        String rootNodeName = getXmlRootNodeName(inputFile);
        if (rootNodeName == null) {
            System.err.println(inputFile + " has no rootNodeName.");
            return null;
        }
        String xslFileName = "";
        if ("build".equalsIgnoreCase(rootNodeName)) {
            xslFileName = BUILD_XSL;
        } else if ("project".equalsIgnoreCase(rootNodeName)) {
            xslFileName = JOB_CONFIG_XSL;
        } else if ("hudson".equalsIgnoreCase(rootNodeName)) {
            xslFileName = GENERAL_CONFIG_XSL;
        } else {
            System.err.println("Unknown rootNodeName: " + rootNodeName);
            return null;
        }
        return xslFileName;
    }

    public static String getXmlRootNodeName(File file) {
        try {
            InputSource is = new InputSource(); 
            is.setCharacterStream(new FileReader(file));
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);
            Element root = doc.getDocumentElement();
            return root.getNodeName();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
