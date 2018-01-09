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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.xml.transform.Source;

import org.junit.AfterClass;
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
    private final static String TIMESTAMP_DIR = ORIGINAL_DIR + "/timestamp/2016-11-30_14-56-00";
    private static PrintStream oldErrOut = null;

    @BeforeClass
    public static void setUpBeforeClass() {
        new File("xml/transformed/").mkdirs();
    }
    
    @AfterClass
    public static void after() {
        // Fix err out in case an exception happens
        if (oldErrOut != null) {
            System.setErr(oldErrOut);
        }
    }

    /**
     * Check if temp file works
     */
    @Test
    public void xslTransformerTest() {
        XslTransformer xslTransformer = new XslTransformer();
        File hudsonConfigFile = new File(ORIGINAL_DIR, "/config.job.hudson.xml");
        HudsonConfigConverter.createBackupFile(hudsonConfigFile, ".tst");
        xslTransformer.transform(hudsonConfigFile, hudsonConfigFile);
        //TODO: add assertion
        HudsonConfigConverter.restoreFromBackupFile(hudsonConfigFile, ".tst");
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
        String xmlRootNodeNameBuild = XslTransformer.getXmlRootNodeName(new File(ORIGINAL_DIR, "build.hudson.xml"));
        String xmlRootNodeNameConfigJob = XslTransformer.getXmlRootNodeName(new File(ORIGINAL_DIR, "config.job.hudson.xml"));
        String xmlRootNodeNameConfigMain = XslTransformer.getXmlRootNodeName(new File(ORIGINAL_DIR, "config.main.hudson.xml"));
        
        assertEquals("build", xmlRootNodeNameBuild);
        assertEquals("project", xmlRootNodeNameConfigJob);
        assertEquals("hudson", xmlRootNodeNameConfigMain);
    }

    @Test
    public void getXmlFileNameTest() {
        String xslFileNameBuild = XslTransformer.getXslFileName(new File(ORIGINAL_DIR, "build.hudson.xml"));
        String xslFileNameConfigJob = XslTransformer.getXslFileName(new File(ORIGINAL_DIR, "config.job.hudson.xml"));
        String xslFileNameConfigMain = XslTransformer.getXslFileName(new File(ORIGINAL_DIR, "config.main.hudson.xml"));
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
    public void buildTest_containsTimestampTag() {
        String buildFile = TIMESTAMP_DIR + "/../buildWithTimestamp.xml";
        assertTrue(TimestampConverter.containsTimestampTag(new File(buildFile)));
    }

    @Test
    public void buildTest_containsTimestampTag_negative() {
        String buildFile = TIMESTAMP_DIR + "/build.xml";
        assertFalse(TimestampConverter.containsTimestampTag(new File(buildFile)));
    }

    @Test
    public void buildTest_timestamp() {
        String buildFile = TIMESTAMP_DIR + "/build.xml";
        transformSingleFile(buildFile, null, "build");
        String nameWithoutExtension = HudsonConfigConverter.getNameWithoutExtension(new File(buildFile));
        compareWithReferenceFile(TIMESTAMP_DIR + "/" + nameWithoutExtension + ".transformed_reference.xml", TIMESTAMP_DIR + "/" + nameWithoutExtension + ".transformed.xml");
    }

    @Test
    public void buildTest_timestamp_cli() {
        String buildFileName = TIMESTAMP_DIR + "/build.xml";
        File buildFile = new File(buildFileName);
        TimestampConverter.main(new String[]{buildFileName});
        String nameWithoutExtension = HudsonConfigConverter.getNameWithoutExtension(buildFile);
        compareWithReferenceFile(TIMESTAMP_DIR + "/" + nameWithoutExtension + ".transformed_reference.xml", TIMESTAMP_DIR + "/" + nameWithoutExtension + ".xml");
        HudsonConfigConverter.restoreFromBackupFile(buildFile, ".bak2");
    }

    @Test
    public void buildTest_timestamp_cli_dir() {
        File buildFile = new File(TIMESTAMP_DIR, "build.xml");
        TimestampConverter.main(new String[]{TIMESTAMP_DIR});
        String nameWithoutExtension = HudsonConfigConverter.getNameWithoutExtension(buildFile);
        compareWithReferenceFile(TIMESTAMP_DIR + "/" + nameWithoutExtension + ".transformed_reference.xml", TIMESTAMP_DIR + "/" + nameWithoutExtension + ".xml");
        HudsonConfigConverter.restoreFromBackupFile(buildFile, ".bak2");
    }

    @Test
    public void configJobTest_kapua() {
        transformAndCompareFreestyleJob("kapua", "config.job.hudson");
    }

    @Test
    public void configJobTest_query() {
        transformAndCompareFreestyleJob("query", "config.job.hudson");
    }

    @Test
    public void configJobTest_transaction() {
        transformAndCompareFreestyleJob("transaction", "config.job.hudson");
    }

    @Test
    public void configJobTest_packagedrone() {
        transformAndCompareFreestyleJob("package-drone", "config.job.hudson");
    }

    @Test
    public void configJobTest_jgit1() {
        transformAndCompareFreestyleJob("jgit1", "config.job.hudson");
    }

    @Test
    public void configJobTest_jgit2() {
        transformAndCompareFreestyleJob("jgit2", "config.job.hudson");
    }

    @Test
    public void configJobTest_jgit3() {
        transformAndCompareFreestyleJob("jgit3", "config.job.hudson");
    }

    @Test
    public void configJobTest_app4mc() {
        transformAndCompareFreestyleJob("app4mc", "config.job.hudson");
    }

    @Test
    public void configJobTest_cbi1() {
        transformAndCompareFreestyleJob("cbi1", "config.job.hudson");
    }

    @Test
    public void configJobTest_cbi2() {
        transformAndCompareFreestyleJob("cbi2", "config.job.hudson");
    }

    @Test
    public void configJobTest_cbi3() {
        transformAndCompareFreestyleJob("cbi3", "config.job.hudson");
    }

    @Test
    public void configJobTest_cbi4() {
        transformAndCompareFreestyleJob("cbi4", "config.job.hudson");
    }

    /**
     * Tests that the <authToken> field is transformed correctly
     */
    @Test
    public void configJobTest_omr() {
        transformAndCompareFreestyleJob("omr", "config.job.hudson");
    }

    /**
     * Tests that CloneWorkspaceSCM is transformed correctly
     */
    @Test
    public void configJobTest_kura1() {
        transformAndCompareFreestyleJob("kura1", "config.job.hudson");
    }

    /**
     * Tests that CloneWorkspacePublisher is transformed correctly
     */
    @Test
    public void configJobTest_kura2() {
        transformAndCompareFreestyleJob("kura2", "config.job.hudson");
    }

    /**
     * Tests that LockWrapper is transformed correctly
     */
    @Test
    public void configJobTest_simrel1() {
        transformAndCompareFreestyleJob("simrel1", "config.job.hudson");
    }

    /**
     * Tests that Groovy build step is transformed correctly
     */
    @Test
    public void configJobTest_simrel2() {
        transformAndCompareFreestyleJob("simrel2", "config.job.hudson");
    }

    /**
     * Tests that Subversion SCM is transformed correctly
     */
    @Test
    public void configJobTest_jwt() {
        transformAndCompareFreestyleJob("jwt", "config.job.hudson");
    }

    /**
     * Tests that Buckminster build step is transformed correctly
     */
    @Test
    public void configJobTest_qvtd() {
        transformAndCompareFreestyleJob("qvtd", "config.job.hudson");
    }

    /**
     * Tests that parameterized trigger build step is transformed correctly
     */
    @Test
    public void configJobTest_lyo() {
        transformAndCompareFreestyleJob("lyo", "config.job.hudson");
    }

//    @Test
//    public void configMainTest_kapua() {
//        transformAndCompareFreestyleJob("kapua", "config.main.hudson", "hudson");
//    }

    /**
     * Tests that SCM URL with SSH is transformed correctly
     */
    @Test
    public void configJobTest_hono() {
        transformAndCompareFreestyleJob("hono", "config.job.hudson");
    }

    /**
     * Tests that Maven jobs are transformed correctly
     */
    @Test
    public void configJobTest_gendoc() {
        transformAndCompare("gendoc", "config.job.hudson", "maven2-moduleset");
    }

    /**
     * Tests that "Delete workspace when build is done" option is transformed correctly
     */
    @Test
    public void configJobTest_xtext1() {
        transformAndCompareFreestyleJob("xtext1", "config.job.hudson");
    }

    /**
     * Tests that "AggregatedTestResultPublisher" does not appear twice
     */
    @Test
    public void configJobTest_xtext2() {
        transformAndCompareFreestyleJob("xtext2", "config.job.hudson");
    }

    @Test
    public void copyViewsTest_positive() {
        testCopyViews("config.main.jenkins-cbi.xml", "config.main.hudson-cbi.xml");
    }

    @Test
    public void copyViewsTest_CLI() {
        String hudsonConfigFileName = ORIGINAL_DIR + "/config.main.hudson-cbi.xml";
        String jenkinsConfigFileName = ORIGINAL_DIR +"/config.main.jenkins-cbi.xml";
        String outputFileName = TRANSFORM_OUTPUT_DIR + "/config.main.jenkins-cbi.transformed.xml";
        File outputFile = new File(outputFileName);
        if (outputFile.exists()) {
            outputFile.delete();
        }
        ViewConverter.main(new String[]{hudsonConfigFileName, jenkinsConfigFileName, outputFileName});
        String nameWithoutExtension = HudsonConfigConverter.getNameWithoutExtension(new File(jenkinsConfigFileName));
        compareWithReferenceFile(nameWithoutExtension);
    }

    @Test
    public void copyViewsTest_CLI2() {
        String hudsonConfigPath = ORIGINAL_DIR + "/copyViews/.hudson";
        String jenkinsConfigPath = ORIGINAL_DIR + "/copyViews/.jenkins";
        String outputFileName = jenkinsConfigPath + "/config.transformed.xml";
        ViewConverter.main(new String[]{hudsonConfigPath, jenkinsConfigPath, outputFileName});
        compareWithReferenceFile(REFERENCE_DIR + "/config.main.jenkins-cbi.transformed_reference.xml", outputFileName);
        
        // clean up
        new File(jenkinsConfigPath, "config.bak3").delete();
        new File(outputFileName).delete();
    }

    @Test
    public void copyViewsTest_CLI3() {
        String jenkinsConfigPath = ORIGINAL_DIR + "/copyViews/.jenkins";
        File jenkinsConfigFile = new File (jenkinsConfigPath, "config.xml");
        HudsonConfigConverter.createBackupFile(jenkinsConfigFile, ".tst");
        // change user dir for main method
        String originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", originalUserDir + "/xml/original/copyViews");
        ViewConverter.main(new String[]{});
        System.setProperty("user.dir", originalUserDir);
        compareWithReferenceFile(REFERENCE_DIR + "/config.main.jenkins-cbi.transformed_reference.xml", jenkinsConfigPath + "/config.xml");
        HudsonConfigConverter.restoreFromBackupFile(jenkinsConfigFile, ".tst");
        new File (jenkinsConfigPath, "config.bak3").delete();
    }

    @Test
    public void copyNodesTest() {
        File inputFile = new File(ORIGINAL_DIR, "config.main.hudson-cbi.xml");
        NodeConverter.convert(inputFile, new File(TRANSFORM_OUTPUT_DIR));
        checkNodes();
    }

    @Test
    public void copyNodesTest_negative() {
        oldErrOut = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setErr(new PrintStream(baos));

        // actual tests
        File inputFile = new File(ORIGINAL_DIR, "config.main.hudson-kapua.xml");
        NodeConverter.convert(inputFile, new File(TRANSFORM_OUTPUT_DIR));

        System.setErr(oldErrOut);
        String output = new String(baos.toByteArray());
        System.out.println("Err Output: " + output);
        assertTrue(output.contains("No slaves found"));
    }

    @Test
    public void copyNodesTest_CLI() {
        File inputFile = new File(ORIGINAL_DIR, "config.main.hudson-cbi.xml");
        NodeConverter.main(new String[]{inputFile.getAbsolutePath(), TRANSFORM_OUTPUT_DIR});
        checkNodes();
    }


    /* Utilities */

    private void checkNodes() {
        String nodesRefDir = REFERENCE_DIR + "/nodes";
        String nodesOutDir = TRANSFORM_OUTPUT_DIR + "/nodes";
        compareWithReferenceFile(nodesRefDir + "/external-sample/config.xml", nodesOutDir + "/external-sample/config.xml");
        compareWithReferenceFile(nodesRefDir + "/hippcentos/config.xml", nodesOutDir + "/hippcentos/config.xml");
        compareWithReferenceFile(nodesRefDir + "/mac-tests2/config.xml", nodesOutDir + "/mac-tests2/config.xml");
    }

    private void testCopyViews(String in, String configFile) {
        File inputFile = new File(ORIGINAL_DIR, in);
        String nameWithoutExtension = HudsonConfigConverter.getNameWithoutExtension(inputFile);
        File outputFile = new File(TRANSFORM_OUTPUT_DIR, nameWithoutExtension + HudsonConfigConverter.DEFAULT_TRANSFORMED_FILE_EXTENSION);
        if (outputFile.exists()) {
            outputFile.delete();
        }
        String hudsonConfigFile = ORIGINAL_DIR + "/" + configFile;
        Properties xslParameters = new Properties();
        xslParameters.put("sourceFile", hudsonConfigFile);
        new XslTransformer().transform(inputFile, outputFile, XslTransformer.XSL_DIR + "/copyViews.xsl", xslParameters);
        compareWithReferenceFile(nameWithoutExtension);
    }

    private void transformAndCompareFreestyleJob(String name, String prefix) {
        transformAndCompare(name, prefix, "project");
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
        compareWithReferenceFile(REFERENCE_DIR + "/" + fileName + ".transformed_reference.xml", TRANSFORM_OUTPUT_DIR + "/" + fileName + ".transformed.xml");
    }

    private void compareWithReferenceFile(String reference, String transformed) {
        // TODO: ignore whitespace
        Source control = Input.fromFile(reference).build();
        Source test = Input.fromFile(transformed).build();
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
