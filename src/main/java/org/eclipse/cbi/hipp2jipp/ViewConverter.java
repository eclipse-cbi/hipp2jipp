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
import java.util.Properties;

/**
 * Converts views from Hudson to Jenkins
 *  * 
 * @author Frederic Gurr
 *
 */
public class ViewConverter {

    private final static String DEFAULT_HUDSON_MAIN_CONFIG = ".hudson/config.xml";
    private final static String DEFAULT_JENKINS_MAIN_CONFIG = ".jenkins/config.xml";

    private static XslTransformer xslTransformer;

    public ViewConverter() {
    }

    public static void convert(File hudsonConfig, File jenkinsConfig, File outputFile) {
        Properties xslParameters = new Properties();
        xslParameters.put("sourceFile", hudsonConfig.getAbsolutePath());
        HudsonConfigConverter.createBackupFile(jenkinsConfig, ".bak3");
        if (outputFile == null) {
            outputFile = jenkinsConfig;
        }
        xslTransformer.transform(jenkinsConfig, outputFile, XslTransformer.XSL_DIR + "/copyViews.xsl", xslParameters);
    }

    private static File getConfigFile(String[] args, int argPosition, String defaultConfigFile, String name) {
        File configFile = null;
        if (args == null || args.length < argPosition+1 || "".equals(args[argPosition])) {
            configFile = new File(defaultConfigFile).getAbsoluteFile();
            System.out.println("No " + name + " config file given, default is " + configFile + " ...");
        } else {
            configFile = new File(args[argPosition]);
            System.out.println("Setting " + name + " config file to " + configFile);
        }

        // if a dir is given, assume it's the parent dir
        if (configFile.isDirectory()) {
            System.out.println("Directory given, looking for " + configFile + "/config.xml ..." );
            configFile = new File(configFile, "config.xml");
        }

        // check files
        if (!configFile.exists()) {
            System.err.println("File/directory " + configFile +  " does not exist.");
            System.err.println("Please specify either the path to the main config.xml or the path to the parent dir (e.g. Hudson home dir).");
            System.exit(1);
        } else {
            System.out.println("Found " + configFile);
        }

        String rootNodeName = XslTransformer.getXmlRootNodeName(configFile);
        if (!"hudson".equalsIgnoreCase(rootNodeName)) {
            System.err.println("File " + configFile +  " does not have the correct XmlRootNodeName ('hudson')!");
            System.exit(1);
        }
        
        return configFile;
    }

    private static File getOutputFile(String[] args) {
        // output file (optionally)
        File outputFile = null;
        if (args == null || args.length < 3 || "".equals(args[2])) {
            System.out.println("No output file given. Replacing Jenkins config file.");
        } else {
            outputFile = new File(args[2]);
            System.out.println("Setting output file to " + outputFile);
        }

        if (outputFile != null && outputFile.isDirectory()) {
            System.err.println("Please specify output file, not a directory.");
            System.exit(1);
        }
        return outputFile;
    }

    public static void main(String[] args) {
        // TODO: usage

        // Hudson config file (optionally)
        File hudsonConfigFile = getConfigFile(args, 0, DEFAULT_HUDSON_MAIN_CONFIG, "Hudson");

        // Jenkins config file (optionally)
        File jenkinsConfigFile = getConfigFile(args, 1, DEFAULT_JENKINS_MAIN_CONFIG, "Jenkins");

        // Output file (optionally)
        File outputFile = getOutputFile(args);

        xslTransformer = new XslTransformer();
        convert(hudsonConfigFile, jenkinsConfigFile, outputFile);
    }

}
