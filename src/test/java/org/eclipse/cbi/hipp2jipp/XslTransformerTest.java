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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.xml.transform.Source;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonListener;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.DOMDifferenceEngine;
import org.xmlunit.diff.DifferenceEngine;

public class XslTransformerTest {

    private final static String ORIGINAL_DIR = "xml/original";
    private final static String REFERENCE_DIR = "xml/reference";
    private final static String TRANSFORM_OUTPUT_DIR = "xml/transformed";

    @BeforeClass
    public static void setUpBeforeClass() {
        new File("xml/transformed/").mkdirs();
    }

    @Test
    public void convertTimestampTest() {
        String timestamp = "2017-02-16_12-35-13";
        long epoch = TimestampConverter.convertTimestampToEpoch(timestamp);
        Date date = new Date(epoch);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        assertEquals(timestamp, df.format(date));
    }

    @Test
    public void getXmlRootNodeTest() {
        String xmlRootNodeNameBuild = XslTransformer.getXmlRootNodeName(new File("xml/original", "build.hudson.xml"));
        String xmlRootNodeNameConfigJob = XslTransformer.getXmlRootNodeName(new File("xml/original", "config.job.hudson.xml"));
        String xmlRootNodeNameConfigMain = XslTransformer.getXmlRootNodeName(new File("xml/original", "config.main.hudson.xml"));
        
        assertEquals("build", xmlRootNodeNameBuild);
        assertEquals("project", xmlRootNodeNameConfigJob);
        assertEquals("hudson", xmlRootNodeNameConfigMain);
    }

    @Test
    public void getXmlFileNameTest() {
        String xslFileNameBuild = XslTransformer.getXslFileName(new File("xml/original", "build.hudson.xml"));
        String xslFileNameConfigJob = XslTransformer.getXslFileName(new File("xml/original", "config.job.hudson.xml"));
        String xslFileNameConfigMain = XslTransformer.getXslFileName(new File("xml/original", "config.main.hudson.xml"));
//        String xslFileNameFoobar = XslTransformer.getXslFileName(new File("xml", "foobar.xml"));
        
        assertEquals("build.xsl", xslFileNameBuild);
        assertEquals("config.job.xsl", xslFileNameConfigJob);
        assertEquals("config.main.xsl", xslFileNameConfigMain);
//        assertNull(xslFileNameFoobar);
    }

    @Test
    public void buildTest_kapua() {
        transformAndCompare("kapua", "build.hudson", "build");
    }

    @Test
    public void buildTest_query() {
        transformAndCompare("query", "build.hudson", "build");
    }

    @Test
    public void buildTest_timestamp() {
        transformSingleFile("xml/original/timestamp/2016-11-30_14-56-00/build.xml", null, "build");
    }

    @Test
    public void configJobTest_kapua() {
        transformAndCompare("kapua", "config.job.hudson", "project");
    }

    @Test
    public void configJobTest_query() {
        transformAndCompare("query", "config.job.hudson", "project");
    }

    @Test
    public void configJobTest_transaction() {
        transformAndCompare("transaction", "config.job.hudson", "project");
    }

    @Test
    public void configJobTest_packagedrone() {
        transformAndCompare("package-drone", "config.job.hudson", "project");
    }

    @Test
    public void configJobTest_jgit1() {
        transformAndCompare("jgit1", "config.job.hudson", "project");
    }

    @Test
    public void configJobTest_jgit2() {
        transformAndCompare("jgit2", "config.job.hudson", "project");
    }

    @Test
    public void configJobTest_jgit3() {
        transformAndCompare("jgit3", "config.job.hudson", "project");
    }

    @Test
    public void configJobTest_app4mc() {
        transformAndCompare("app4mc", "config.job.hudson", "project");
    }

    @Test
    public void configJobTest_cbi1() {
        transformAndCompare("cbi1", "config.job.hudson", "project");
    }

    @Test
    public void configJobTest_cbi2() {
        transformAndCompare("cbi2", "config.job.hudson", "project");
    }

    @Test
    public void configJobTest_cbi3() {
        transformAndCompare("cbi3", "config.job.hudson", "project");
    }

    @Test
    public void configJobTest_cbi4() {
        transformAndCompare("cbi4", "config.job.hudson", "project");
    }

    /**
     * Tests that the <authToken> field is transformed correctly
     */
    @Test
    public void configJobTest_omr() {
        transformAndCompare("omr", "config.job.hudson", "project");
    }

    /**
     * Tests that CloneWorkspaceSCM is transformed correctly
     */
    @Test
    public void configJobTest_kura1() {
        transformAndCompare("kura1", "config.job.hudson", "project");
    }

    /**
     * Tests that CloneWorkspacePublisher is transformed correctly
     */
    @Test
    public void configJobTest_kura2() {
        transformAndCompare("kura2", "config.job.hudson", "project");
    }

    /**
     * Tests that LockWrapper is transformed correctly
     */
    @Test
    public void configJobTest_simrel1() {
        transformAndCompare("simrel1", "config.job.hudson", "project");
    }

    /**
     * Tests that Groovy build step is transformed correctly
     */
    @Test
    public void configJobTest_simrel2() {
        transformAndCompare("simrel2", "config.job.hudson", "project");
    }

//    @Test
//    public void configMainTest_kapua() {
//        transformAndCompare("kapua", "config.main.hudson", "hudson");
//    }

    @Test
    public void copyViewsTest_positive() {
        testCopyViews("config.main.jenkins-cbi.xml", "config.main.hudson-cbi.xml");
    }

    @Test
    public void copyViewsTest_CLI() {
        String inputFileName = "xml/original/config.main.jenkins-cbi.xml";
        String outputFileName = "xml/transformed/config.main.jenkins-cbi.transformed.xml";
        String configFile = "xml/original/config.main.hudson-cbi.xml";
        HudsonConfigConverter.main(new String[]{inputFileName, "-o", outputFileName, "-cv", configFile});
        String nameWithoutExtension = HudsonConfigConverter.getNameWithoutExtension(new File(inputFileName));
        compareWithReferenceFile(nameWithoutExtension);
    }

    /* Utilities */

    private void testCopyViews(String in, String configFile) {
        File inputFile = new File(ORIGINAL_DIR, in);
        String nameWithoutExtension = HudsonConfigConverter.getNameWithoutExtension(inputFile);
        File outputFile = new File(TRANSFORM_OUTPUT_DIR, nameWithoutExtension + HudsonConfigConverter.DEFAULT_TRANSFORMED_FILE_EXTENSION);
        String hudsonConfigFile = ORIGINAL_DIR + "/" + configFile;
        Properties xslParameters = new Properties();
        xslParameters.put("sourceFile", hudsonConfigFile);
        new XslTransformer().transform(inputFile, outputFile, XslTransformer.XSL_DIR + "/copyViews.xsl", xslParameters);
        compareWithReferenceFile(nameWithoutExtension);
    }

    //TODO: simplify
    private void transformAndCompare(String name, String prefix, String rootNodeName) {
        String fileName = prefix + "-" + name;
        transformSingleFile(ORIGINAL_DIR + "/" + fileName + ".xml", TRANSFORM_OUTPUT_DIR, rootNodeName);
        compareWithReferenceFile(fileName);
    }

    private void transformSingleFile(String inputFileName, String outputDirName, String expectedXmlRootNodeName) {
        String nameWithoutExtension = HudsonConfigConverter.getNameWithoutExtension(new File(inputFileName));
        // delete old file before transforming
        File outputDir = null;
        if (outputDirName == null) {
            outputDir = new File (inputFileName).getAbsoluteFile().getParentFile();
        } else {
            outputDir = new File (outputDirName);
        }
        File transformedFile = new File(outputDir, nameWithoutExtension + HudsonConfigConverter.DEFAULT_TRANSFORMED_FILE_EXTENSION);
        if (transformedFile.exists()) {
            transformedFile.delete();
        }
        HudsonConfigConverter.main(new String[]{inputFileName, "-o", transformedFile.getAbsolutePath()});
        assertTrue(transformedFile.exists());
        
        // check root node in transformed file
        String xmlRootNodeName = XslTransformer.getXmlRootNodeName(transformedFile);
        assertEquals(expectedXmlRootNodeName, xmlRootNodeName);
    }

    private void compareWithReferenceFile(String fileName) {
        // TODO: ignore whitespace
        Source control = Input.fromFile(REFERENCE_DIR + "/" + fileName + ".transformed_reference.xml").build();
        Source test = Input.fromFile(TRANSFORM_OUTPUT_DIR + "/" + fileName + ".transformed.xml").build();
        DifferenceEngine diff = new DOMDifferenceEngine();
        diff.addDifferenceListener(new ComparisonListener() {
            public void comparisonPerformed(Comparison comparison, ComparisonResult outcome) {
                System.out.println(comparison);
                Assert.fail("found a difference: " + comparison);
            }
        });
        diff.compare(control, test);
    }

}
