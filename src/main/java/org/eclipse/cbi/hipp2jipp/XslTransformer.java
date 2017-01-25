package org.eclipse.cbi.hipp2jipp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XslTransformer {

    private String XSL_DIR = "xsl";
    public static String DEFAULT_TRANSFORMED_FILE_EXTENSION = ".transformed.xml";

    private TransformerFactory transformerFactory = TransformerFactory.newInstance();

    public void transform(File inputFile, File outputFile) {
        InputStream is = null;
        OutputStream os = null;
        try {
            outputFile.createNewFile();
            is = new FileInputStream(inputFile);
            os = new FileOutputStream(outputFile);

            // InputStream xsl = new FileInputStream(new File(XSL_DIR, getXslFileName(inputFile)));
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            InputStream xsl = classloader.getResourceAsStream(XSL_DIR + "/" + getXslFileName(inputFile));

            Transformer transformer = transformerFactory.newTransformer(new StreamSource(xsl));
            transformer.setOutputProperty("indent", "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(new StreamSource(is), new StreamResult(os));
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
    }

    public static String getXslFileName(File inputFile) {
        String rootNodeName = getXmlRootNodeName(inputFile);
        if (rootNodeName == null) {
            return null;
        }
        String xslFileName = "";
        if ("build".equalsIgnoreCase(rootNodeName)) {
            xslFileName = "build.xsl";
        } else if ("project".equalsIgnoreCase(rootNodeName)) {
            xslFileName = "config.job.xsl";
        } else if ("hudson".equalsIgnoreCase(rootNodeName)) {
            xslFileName = "config.main.xsl";
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
    
//    public InputStream importConfigXml(InputStream is) {
//        InputStream returnStream = null;
//        OutputStream os = null;
//        transform(is, os, "config.xsl");

        // FIXME

        // // take the copy of the stream and re-write it to an InputStream
        // PipedInputStream in = new PipedInputStream();
        // final PipedOutputStream out = new PipedOutputStream(in);
        // new Thread(new Runnable() {
        // public void run () {
        // try {
        // // write the original OutputStream to the PipedOutputStream
        // os.writeTo(out);
        // } catch (IOException e) {
        // // logging and exception handling should go here
        // }
        // }
        // }).start();
        //
//        return returnStream;
//    }
    
    public static void main(String[] args) {
        XslTransformer xslTransformer = new XslTransformer();
        
        if (args.length < 1) {
            showUsage();
            return;
        }

        String inputFilePath = args[0];

        // check if file exists
        File inputFile = new File(inputFilePath);
        if (!inputFile.exists()) {
            System.err.println("Input file " + inputFilePath + " does not exist.");
            return;
        }
        if (!inputFile.isFile()) {
            System.err.println("Input file " + inputFilePath + " is not a file.");
            return;
        }

        // TODO: create backup of original file
        
        File outputFile;
        if (args.length == 2) {
            outputFile = new File(args[1]);
        } else {
            String nameWithoutExtension = inputFile.getName().substring(0, inputFile.getName().lastIndexOf("."));
            outputFile = new File(inputFile.getAbsoluteFile().getParentFile().getAbsolutePath(), nameWithoutExtension + DEFAULT_TRANSFORMED_FILE_EXTENSION);
        }
        
        xslTransformer.transform(inputFile, outputFile);
        System.out.println("Done. File written: " + outputFile.getName());
    }
    
    private static void showUsage() {
        System.out.println("Usage:");
        System.out.println("XslTransformer [inputFile] (outputFile)");
        System.out.println("outputFile is optional");
        System.out.println();
    }
}
