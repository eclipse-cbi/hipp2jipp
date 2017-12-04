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
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * Hudson to Jenkins config file XSL transformer<br/>
 * <br/>
 * The HudsonConfigConverter can be run from the command line by specifying<br/>
 * either a single input file and an optional output file or by specifying a<br/>
 * directory (e.g. the jobs directory).<br/>
 * <br/>
 * Here is the recommended way to do the migration:<br/>
 * <br/>
 * 1. set up a new Jenkins instance<br/>
 * 2. copy the jobs from your Hudson instance to the jobs directory of your Jenkins instance<br/>
 * 3. run the XslTransformer with the Jenkins job directory as parameter<br/>
 * 4. restart Jenkins instance<br/>
 * 
 * @author Frederic Gurr
 *
 */
public class HudsonConfigConverter {

    private static final String DEFAULT_BACKUP_FILE_EXTENSION = ".bak";
    public static final String DEFAULT_TRANSFORMED_FILE_EXTENSION = ".transformed.xml";

    private static XslTransformer xslTransformer;

    public static FilenameFilter excludedDirFilter = new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
            return !name.startsWith("workspace") &&
                    !"users".equalsIgnoreCase(name) &&
                    !"archive".equalsIgnoreCase(name) &&
                    !"config-history".equalsIgnoreCase(name) &&
                    !"promotions".equalsIgnoreCase(name);
        }
    };

    @Option(name="-o",usage="output to this file",metaVar="OUTPUT")
    private File outputFile = null;

    @Argument
    private List<String> arguments = new ArrayList<String>();

    public static void main(String[] args) {
        new HudsonConfigConverter().doMain(args);
    }

    public void doMain(String[] args) {
        xslTransformer = new XslTransformer();

        CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);

            if (arguments.isEmpty()) {
                throw new CmdLineException(parser, "No argument is given");
            }

        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java HudsonConfigConverter [options...] arguments...");
            // print the list of available options
            parser.printUsage(System.err);
            System.err.println();
        }

        // input file
        String inputFilePath = arguments.get(0);

        File inputFile = new File(inputFilePath);
        if (!inputFile.exists()) {
            System.err.println("Input file/directory " + inputFile.getAbsolutePath() + " does not exist.");
            return;
        }

        if (inputFile.isFile()) {
            // single XML file
            if (outputFile == null) {
                outputFile = getTransformedFileName(inputFile);
            }

            xslTransformer.transform(inputFile, outputFile);

            String rootNodeName = XslTransformer.getXmlRootNodeName(inputFile);
            if ("build".equalsIgnoreCase(rootNodeName)) {
                TimestampConverter.convertBuildTimestamp(outputFile);
            }
            System.out.println("Done. File written: " + outputFile.getAbsolutePath());
        } else {
            // scan directory recursively for config.xmls or build.xmls
            search(inputFile);
        }
    }

    public static String getNameWithoutExtension(File file) {
        String filename = file.getName();
        int pos = filename.lastIndexOf('.');
        return (pos > 0) ? filename.substring(0, pos) : filename;
    }

    public static File getTransformedFileName(File inputFile) {
        return new File(inputFile.getAbsoluteFile().getParentFile().getAbsolutePath(), getNameWithoutExtension(inputFile) + DEFAULT_TRANSFORMED_FILE_EXTENSION);
    }

    public static void createBackupFile(File file, String extension) {
        File backupFile = new File(file.getAbsoluteFile().getParentFile().getAbsolutePath(), getNameWithoutExtension(file) + extension);
        try {
            Files.copy(file.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // System.out.println("Created backup for " + file.getName() +".");
    }

    /**
     * Search recursively for job config and build XMLs
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
                File[] list = file.listFiles(excludedDirFilter);
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
            createBackupFile(file, DEFAULT_BACKUP_FILE_EXTENSION);
            System.out.print("Converting " + file + "...");
            boolean successful = xslTransformer.transform(file);
            if (successful) {
                if ("build.xml".equalsIgnoreCase(file.getName())) {
                    TimestampConverter.convertBuildTimestamp(file);
                }
                System.out.println(" Done.");
            } else {
                System.err.println(" Error.");
            }
        }
    }

}
