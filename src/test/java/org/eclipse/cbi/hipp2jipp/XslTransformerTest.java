package org.eclipse.cbi.hipp2jipp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.transform.Source;

import org.junit.Assert;
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
        String xmlRootNodeNameBuild = XslTransformer.getXmlRootNodeName(new File("xml", "build.hudson.xml"));
        String xmlRootNodeNameConfigJob = XslTransformer.getXmlRootNodeName(new File("xml", "config.job.hudson.xml"));
        String xmlRootNodeNameConfigMain = XslTransformer.getXmlRootNodeName(new File("xml", "config.main.hudson.xml"));
        
        assertEquals("build", xmlRootNodeNameBuild);
        assertEquals("project", xmlRootNodeNameConfigJob);
        assertEquals("hudson", xmlRootNodeNameConfigMain);
    }

    @Test
    public void getXmlFileNameTest() {
        String xslFileNameBuild = XslTransformer.getXslFileName(new File("xml", "build.hudson.xml"));
        String xslFileNameConfigJob = XslTransformer.getXslFileName(new File("xml", "config.job.hudson.xml"));
        String xslFileNameConfigMain = XslTransformer.getXslFileName(new File("xml", "config.main.hudson.xml"));
//        String xslFileNameFoobar = XslTransformer.getXslFileName(new File("xml", "foobar.xml"));
        
        assertEquals("build.xsl", xslFileNameBuild);
        assertEquals("config.job.xsl", xslFileNameConfigJob);
        assertEquals("config.main.xsl", xslFileNameConfigMain);
//        assertNull(xslFileNameFoobar);
    }

//    @Test
//    public void xslTransformerMainTest_HudsonDir() {
//        System.out.println();
//        XslTransformer.main(new String[]{"test/hudson"});
//        
//        // TODO: assertions
//        // TODO: overwrite old .bak files
//    }
//
//    @Test
//    public void xslTransformerMainTest_JenkinsDir() {
//        System.out.println();
//        XslTransformer.main(new String[]{"test/jenkins"});
//        
//        // TODO: assertions
//        // TODO: overwrite old .bak files
//    }
    
    @Test
    public void buildTest_kapua() {
        transformAndCompare("kapua", "build.hudson", "build");
    }

    @Test
    public void buildTest_query() {
        transformAndCompare("query", "build.hudson", "build");
    }

    @Test
    public void buildTest_query1() {
        transformSingleFile("xml/build.hudson-query.xml", null, "build");
    }

    @Test
    public void buildTest_timestamp() {
        transformSingleFile("test/hudson/jobs/Develop/builds/2016-11-30_14-56-00/build.xml", null, "build");
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

//    @Test
//    public void configMainTest_kapua() {
//        transformAndCompare("kapua", "config.main.hudson", "hudson");
//    }

    /* Utilities */

    //TODO: simplify
    private void transformAndCompare(String name, String prefix, String rootNodeName) {
        String fileName = prefix + "-" + name;
        transformSingleFile(ORIGINAL_DIR + "/" + fileName + ".xml", TRANSFORM_OUTPUT_DIR, rootNodeName);
        compareWithReferenceFile(fileName);
    }

    private void transformSingleFile(String inputFileName, String outputDirName, String expectedXmlRootNodeName) {
        String fileName = new File (inputFileName).getName();
        String nameWithoutExtension = fileName.substring(0, fileName.lastIndexOf("."));
        File outputDir = null;
        if (outputDirName == null) {
            outputDir = new File (inputFileName).getAbsoluteFile().getParentFile();
        } else {
            outputDir = new File (outputDirName);
        }
        File transformedFile = new File(outputDir, nameWithoutExtension + XslTransformer.DEFAULT_TRANSFORMED_FILE_EXTENSION);
        if (transformedFile.exists()) {
            transformedFile.delete();
        }
        XslTransformer.main(new String[]{inputFileName, transformedFile.getAbsolutePath()});
        assertTrue(transformedFile.exists());
        
        // check root node in transformed file
        String xmlRootNodeName = XslTransformer.getXmlRootNodeName(transformedFile);
        assertEquals(expectedXmlRootNodeName, xmlRootNodeName);
    }

    private void compareWithReferenceFile(String fileName) {
        //TODO: ignore whitespace
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
