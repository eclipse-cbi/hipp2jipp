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

    private static void checkFile(File file, String input) {
        // if a dir is given, assume it's the parent dir
        if (file.isDirectory()) {
            System.out.println("Directory given, looking for " + file + "/config.xml ..." );
            file = new File(file, "config.xml");
        }

        if (!file.exists()) {
            System.err.println("File/directory " + input +  " does not exist.");
            System.err.println("Please specify either the path to the main config.xml or the path to the parent dir (e.g. Hudson home dir).");
            System.exit(1);
        } else {
            System.out.println("Found " + file);
        }

        String rootNodeName = XslTransformer.getXmlRootNodeName(file);
        if (!"hudson".equalsIgnoreCase(rootNodeName)) {
            System.err.println("File " + file +  " does not have the correct XmlRootNodeName ('hudson')!");
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        // TODO: usage
        
        // Hudson config file (optionally)
        File hudsonConfigFile = null;
        if (args == null || args.length < 1 || "".equals(args[0])) {
            hudsonConfigFile = new File(DEFAULT_HUDSON_MAIN_CONFIG);
            System.out.println("No Hudson config file given, default is " + hudsonConfigFile + " ...");
        } else {
            hudsonConfigFile = new File(args[0]);
            System.out.println("Setting Hudson config file to " + hudsonConfigFile);
        }

        checkFile(hudsonConfigFile, args[0]);

        // Jenkins config file (optionally)
        File jenkinsConfigFile = null;
        if (args == null || args.length < 2 || "".equals(args[1])) {
            jenkinsConfigFile = new File (DEFAULT_JENKINS_MAIN_CONFIG);
            System.out.println("No Jenkins config file given, default file is " + jenkinsConfigFile);
        } else {
            jenkinsConfigFile = new File(args[1]);
            System.out.println("Setting Jenkins config file to " + jenkinsConfigFile);
        }

        checkFile(jenkinsConfigFile, args[1]);

        // output file (optionally)
        File outputFile = null;
        if (args == null || args.length < 3 || "".equals(args[2])) {
            System.out.println("No output file given. Replacing Jenkins config file.");
        } else {
            outputFile = new File(args[2]);
            System.out.println("Setting output file to " + outputFile);
        }

        if (outputFile.isDirectory()) {
            System.err.println("Please specify output file, not a directory.");
            System.exit(1);
        }


        xslTransformer = new XslTransformer();
        convert(hudsonConfigFile, jenkinsConfigFile, outputFile);
    }
}
