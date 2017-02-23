package org.eclipse.cbi.hipp2jipp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XslTransformer {

    private static final String GENERAL_CONFIG_XSL = "config.main.xsl";
    private static final String JOB_CONFIG_XSL = "config.job.xsl";
    private static final String BUILD_XSL = "build.xsl";

    private static final String GENERAL_CONFIG_XML = "config.xml";
    private static final String JOB_CONFIG_XML = "config.xml";
    private static final String BUILD_XML = "build.xml";
    private static final String DEFAULT_BACKUP_FILE_EXTENSION = ".bak";
    private static final String XSL_DIR = "xsl";
    public static final String DEFAULT_TRANSFORMED_FILE_EXTENSION = ".transformed.xml";

    private static XslTransformer xslTransformer = new XslTransformer();
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

            String xslFileName = getXslFileName(inputFile);
            if (xslFileName == null) {
                System.err.println(inputFile + " cannot be converted.");
                return;
            }

            InputStream xsl = classloader.getResourceAsStream(XSL_DIR + "/" + xslFileName);

            Transformer transformer = transformerFactory.newTransformer(new StreamSource(xsl));
            //TODO: Fix indentation
            // when indent is off during the first transformation and on during the second transformation, it works
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
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

    public void transform(File inputFile) {
        try {
            File tempFile = File.createTempFile(inputFile.getName(), "tmp", inputFile.getAbsoluteFile().getParentFile());
            transform(inputFile, tempFile);
            if (!tempFile.exists() || tempFile.length() == 0) {
                System.err.println("Error during creation of temp file.");
                return;
            }
            inputFile.delete();
            Files.copy(tempFile.toPath(), inputFile.toPath());
            tempFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public static void main(String[] args) {
        xslTransformer = new XslTransformer();

        if (args.length < 1) {
            showUsage();
            return;
        }

        String inputFilePath = args[0];

        File inputFile = new File(inputFilePath);
        if (!inputFile.exists()) {
            System.err.println("Input file/directory " + inputFile.getAbsolutePath() + " does not exist.");
            return;
        }

        if (inputFile.isFile()) {
            // single XML file
            File outputFile;
            if (args.length == 2) {
                outputFile = new File(args[1]);
            } else {
                String nameWithoutExtension = inputFile.getName().substring(0, inputFile.getName().lastIndexOf("."));
                outputFile = new File(inputFile.getAbsoluteFile().getParentFile().getAbsolutePath(), nameWithoutExtension + DEFAULT_TRANSFORMED_FILE_EXTENSION);
            }
            
            xslTransformer.transform(inputFile, outputFile);
            if ("build.transformed.xml".equalsIgnoreCase(outputFile.getName())) {
                convertBuildTimestamp(outputFile);
            }
            System.out.println("Done. File written: " + outputFile.getAbsolutePath());
        } else if (inputFile.isDirectory() && isJenkinsHomeDir(inputFile)) {
            // assume Hudson root dir
            File hudsonRootDir = inputFile;
            File jobsDir = new File(hudsonRootDir, "jobs");
            if (!jobsDir.exists()) {
                System.err.println("Jobs dir " + jobsDir.getAbsolutePath() + " does not exist.");
                return;
            }

            // check if config files exists
            File generalConfigFile = new File(hudsonRootDir, GENERAL_CONFIG_XML);
            if (!generalConfigFile.exists()) {
                System.err.println("Main config file " + generalConfigFile.getAbsolutePath() + " does not exist.");
                return;
            }

            // converting general config.xml
            createBackupFile(generalConfigFile);
            System.out.print("Converting config.xml in Hudson root dir " + hudsonRootDir.getAbsolutePath() + "...");
            xslTransformer.transform(generalConfigFile);
            System.out.println(" Done.");

            File[] jobDirList = jobsDir.listFiles();
            Arrays.sort(jobDirList);
            for (File jobDir : jobDirList) {
                if (jobDir.isDirectory()) {
                    File jobConfigFile = new File(jobDir, JOB_CONFIG_XML);
                    if (!jobConfigFile.exists()) {
                        System.err.println("Job dir " + jobDir.getName() + " does not contain a config.xml file. Skipping directory...");
                        continue;
                    }
                    // creating backup of config.xml
                    createBackupFile(jobConfigFile);

                    // converting job config.xml
                    System.out.print("Converting config.xml in " + jobDir.getName() + "...");
                    xslTransformer.transform(jobConfigFile);
                    System.out.println(" Done.");
                    
                    convertBuildXmls(jobDir);
                }
                System.out.println();
            }
        } else {
            // scan directory recursively for config.xmls or build.xmls
            search(inputFile);
        }
    }

    private static boolean isJenkinsHomeDir(File inputDir) {
        return ".hudson".equalsIgnoreCase(inputDir.getName()) || ".jenkins".equalsIgnoreCase(inputDir.getName());
    }

    private static void search (File file) {
        if (file.canRead()) {
            if (file.isDirectory() && !Files.isSymbolicLink(file.toPath())) {
                // System.out.println("Searching directory ... " + file.getAbsoluteFile());
                for (File f : file.listFiles()) {
                    if (f.isDirectory() && !Files.isSymbolicLink(file.toPath())) {
                        search(f);
                    } else {
                        checkFile(f);
                    }
                }
            } else {
                checkFile(file);
            }
        } else {
            System.out.println(file.getAbsoluteFile() + " -> Permission Denied");
        }
    }

    private static void checkFile(File file) {
        if ("build.xml".equalsIgnoreCase(file.getName()) || "config.xml".equalsIgnoreCase(file.getName())) {
            createBackupFile(file);
            System.out.print("Converting " + file + "...");
            xslTransformer.transform(file);
            if ("build.xml".equalsIgnoreCase(file.getName())) {
                convertBuildTimestamp(file);
            }
            System.out.println(" Done.");
        } 
    }

    private static void convertBuildTimestamp(File file) {
        System.out.println("Trying to fix missing build time stamp...");
        // System.out.println("File: " + file.getPath());
        // TODO: check if time stamp is already there
        // convert parent directory name to time stamp
        String parentDirName = file.getAbsoluteFile().getParentFile().getName();
        System.out.print("Parent dir name: " +  parentDirName);
        long epoch = convertTimestampToEpoch(parentDirName);
        if (epoch != -1) {
            writeBuildTimestampToXml(file, epoch);
        }
    }

    public static long convertTimestampToEpoch(String timestamp) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        long epoch = -1;
        try {
            Date date = df.parse(timestamp);
            epoch = date.getTime();
            System.out.print(" -> epoch date: " + epoch);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return epoch;
    }

    public static void writeBuildTimestampToXml(File file, long timestamp) {
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
                System.out.println(" -> fixed build time stamp.");
            } else {
                System.err.println("Wrong rootNodeName: " + root.getNodeName());
            }
        } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
            e.printStackTrace();
        }
    }

    private static void convertBuildXmls(File jobDir) {
        File buildsDir = new File(jobDir, "builds");
        if (!buildsDir.exists()) {
            System.err.println("Builds dir " + buildsDir.getName() + " does not exist in " + jobDir.getAbsolutePath() + ". Skipping directory...");
            return;
        }
        File[] buildDirList = buildsDir.listFiles();
        Arrays.sort(buildDirList);
        for (File buildDir : buildDirList) {
            if (buildDir.isDirectory() && !Files.isSymbolicLink(buildDir.toPath())) {
                File buildFile = new File(buildDir, BUILD_XML);
                if (!buildFile.exists()) {
                    System.err.println("Build dir " + jobDir.getAbsolutePath() + " does not contain a build.xml file. Skipping directory...");
                    continue;
                }
                // creating backup of config.xml
                createBackupFile(buildFile);

                // converting job config.xml
                System.out.print("Converting " + buildFile.getName() + " in " + buildDir.getName() + "...");
                xslTransformer.transform(buildFile);
                convertBuildTimestamp(buildFile);
                System.out.println(" Done.");
            }
        }
    }

    private static void createBackupFile(File file) {
        // creating backup of config.xml
        String nameWithoutExtension = file.getName().substring(0, file.getName().lastIndexOf("."));
        File backupFile = new File(file.getAbsoluteFile().getParentFile().getAbsolutePath(), nameWithoutExtension + DEFAULT_BACKUP_FILE_EXTENSION);
        try {
            Files.copy(file.toPath(),backupFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        // System.out.println("Created backup for " + file.getName() +".");
    }

    private static void showUsage() {
        System.out.println("Usage:");
        System.out.println("XslTransformer [hudson root directory/single XML file] [outputFile]");
        System.out.println();
    }
}
