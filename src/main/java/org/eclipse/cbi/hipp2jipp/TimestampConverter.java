package org.eclipse.cbi.hipp2jipp;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class TimestampConverter {

    public static void convertBuildTimestamp(File file) {
        System.out.println("Trying to fix missing build time stamp...");
        // System.out.println("File: " + file.getPath());
        // TODO: check if time stamp is already there
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

}
