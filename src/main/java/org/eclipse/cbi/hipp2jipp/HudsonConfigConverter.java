package org.eclipse.cbi.hipp2jipp;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Hudson to Jenkins config file XSL transformer
 * 
 * The HudsonConfigConverter can be run from the command line by specifying
 * either a single input file and an optional output file or by specifying a
 * directory (e.g. the jobs directory).
 * 
 * Here is the recommended way to do the migration:
 * 
 * 1. set up a new Jenkins instance 2. copy the jobs from your Hudson instance
 * to the jobs directory of your Jenkins instance 3. run the XslTransformer with
 * the Jenkins job directory as parameter 4. restart Jenkins instance
 * 
 * @author Frederic Gurr
 *
 */
public class HudsonConfigConverter {

    private static final String DEFAULT_BACKUP_FILE_EXTENSION = ".bak";
    public static final String DEFAULT_TRANSFORMED_FILE_EXTENSION = ".transformed.xml";

    private static XslTransformer xslTransformer;

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
                // TimestampConverter.convertBuildTimestamp(outputFile);
            }
            System.out.println("Done. File written: " + outputFile.getAbsolutePath());
        } else {
            // scan directory recursively for config.xmls or build.xmls
            search(inputFile);
        }
    }

    private static String getNameWithoutExtension(File file) {
        String filename = file.getName();
        int pos = filename.lastIndexOf('.');
        return (pos > 0) ? filename.substring(0, pos) : filename;
    }

    private static File getTransformedFileName(File inputFile) {
        return new File(inputFile.getAbsoluteFile().getParentFile().getAbsolutePath(), getNameWithoutExtension(inputFile) + DEFAULT_TRANSFORMED_FILE_EXTENSION);
    }

    private static void createBackupFile(File file) {
        // creating backup of config.xml
        File backupFile = new File(file.getAbsoluteFile().getParentFile().getAbsolutePath(), getNameWithoutExtension(file) + DEFAULT_BACKUP_FILE_EXTENSION);
        try {
            Files.copy(file.toPath(),backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // System.out.println("Created backup for " + file.getName() +".");
    }

        /**
     * Search recursively for job config and build XMLs
     * 
     * Most likely the given directory will be the jobs or the Jenkins root directory.
     * A few directories are excluded from the search like "workspace" or "archive". 
     * 
     * @param file directory
     */
    private static void search (File file) {
        if (file.canRead()) {
            if (file.isDirectory() && !Files.isSymbolicLink(file.toPath())) {
                // do not search workspace, users and config-history dir
                File[] list = file.listFiles(new FilenameFilter() {
                    
                    @Override
                    public boolean accept(File dir, String name) {
                        return  !name.startsWith("workspace") &&
                                !"users".equalsIgnoreCase(name) &&
                                !"archive".equalsIgnoreCase(name) &&
                                !"config-history".equalsIgnoreCase(name) &&
                                !"promotions".equalsIgnoreCase(name);
                    }
                });
                // System.out.println("Searching directory ... " + file.getAbsoluteFile());
                for (File f : list) {
                    search(f);
                }
            } else {
                convertFile(file);
            }
        } else {
            System.out.println(file.getAbsoluteFile() + " -> Permission Denied");
        }
    }

    /**
     * Convert file
     * 
     * @param file
     */
    private static void convertFile(File file) {
        // only convert build.xml and config.xml files
        if ("build.xml".equalsIgnoreCase(file.getName()) || "config.xml".equalsIgnoreCase(file.getName())) {
            createBackupFile(file);
            System.out.print("Converting " + file + "...");
            boolean successful = xslTransformer.transform(file);
            if (successful) {
                System.out.println(" Done.");
            } else {
                System.err.println(" Error.");
            }
        }
    }

    private static void showUsage() {
        System.out.println("Usage:");
        System.out.println("XslTransformer [single XML file/(hudson root) directory] [outputFile]");
        System.out.println();
    }
}
