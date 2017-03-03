package org.eclipse.cbi.hipp2jipp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;

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

    public boolean transform(File inputFile, File outputFile) {
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
                return false;
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

    public boolean transform(File inputFile) {
        try {
            File tempFile = File.createTempFile(inputFile.getName(), "tmp", inputFile.getAbsoluteFile().getParentFile());
            boolean successful = transform(inputFile, tempFile);
            if (!successful || !tempFile.exists() || tempFile.length() == 0) {
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
                outputFile = getTransformedFileName(inputFile);
            }
            
            xslTransformer.transform(inputFile, outputFile);
            if ("build.transformed.xml".equalsIgnoreCase(outputFile.getName())) {
                TimestampConverter.convertBuildTimestamp(outputFile);
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
//            File generalConfigFile = new File(hudsonRootDir, GENERAL_CONFIG_XML);
//            if (!generalConfigFile.exists()) {
//                System.err.println("Main config file " + generalConfigFile.getAbsolutePath() + " does not exist.");
//                return;
//            }

            // converting general config.xml
            // disabled, because it's easier to keep the new config.xml and adjust when necessary
//            System.out.print("Converting config.xml in Hudson root dir " + hudsonRootDir.getAbsolutePath() + "...");
//            createBackupFile(generalConfigFile);
//            xslTransformer.transform(generalConfigFile);
//            System.out.println(" Done.");

            File[] jobDirList = jobsDir.listFiles();
            Arrays.sort(jobDirList);
            for (File jobDir : jobDirList) {
                if (jobDir.isDirectory()) {
                    File jobConfigFile = new File(jobDir, JOB_CONFIG_XML);
                    if (!jobConfigFile.exists()) {
                        System.err.println("Job dir " + jobDir.getName() + " does not contain a config.xml file. Skipping directory...");
                        continue;
                    }
                    
                    // converting job config.xml
                    System.out.print("Converting config.xml in " + jobDir.getName() + "...");
                    createBackupFile(jobConfigFile);
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

    private static File getTransformedFileName(File inputFile) {
        String nameWithoutExtension = inputFile.getName().substring(0, inputFile.getName().lastIndexOf("."));
        return new File(inputFile.getAbsoluteFile().getParentFile().getAbsolutePath(), nameWithoutExtension + DEFAULT_TRANSFORMED_FILE_EXTENSION);
    }

    private static boolean isJenkinsHomeDir(File inputDir) {
        return ".hudson".equalsIgnoreCase(inputDir.getName()) ||
               "hudson".equalsIgnoreCase(inputDir.getName()) ||
               ".jenkins".equalsIgnoreCase(inputDir.getName()) ||
               "jenkins".equalsIgnoreCase(inputDir.getName());
    }

    private static void search (File file) {
        if (file.canRead()) {
            // do not search workspace dir
            if (file.isDirectory() && !Files.isSymbolicLink(file.toPath()) && !"workspace".equalsIgnoreCase(file.getName()) && !"users".equalsIgnoreCase(file.getName())) {
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
            boolean successful = xslTransformer.transform(file);
            if (successful) {
                System.out.println(" Done.");
                if ("build.xml".equalsIgnoreCase(file.getName())) {
                    TimestampConverter.convertBuildTimestamp(file);
                }
            }
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
                // converting job config.xml
                System.out.print("Converting " + buildFile.getName() + " in " + buildDir.getName() + "...");
                createBackupFile(buildFile);
                xslTransformer.transform(buildFile);
                System.out.println(" Done.");
                TimestampConverter.convertBuildTimestamp(buildFile);
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
        System.out.println("XslTransformer [single XML file/(hudson root) directory] [outputFile]");
        System.out.println();
    }
}
