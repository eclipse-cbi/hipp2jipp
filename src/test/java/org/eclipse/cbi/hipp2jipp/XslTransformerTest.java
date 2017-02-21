package org.eclipse.cbi.hipp2jipp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

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
    
    @Test
    public void buildTest_kapua() {
        transformAndCompare("kapua", "build.hudson", "build");
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
    public void configMainTest_kapua() {
        transformAndCompare("kapua", "config.main.hudson", "hudson");
    }

    /* Utilities */

    //TODO: simplify
    private void transformAndCompare(String name, String prefix, String rootNodeName) {
        String fileName = prefix + "-" + name;
        transformSingleFile(ORIGINAL_DIR + "/" + fileName + ".xml", rootNodeName);
        compareWithReferenceFile(fileName);
    }

    private void transformSingleFile(String inputFileName, String expectedXmlRootNodeName) {
        String fileName = new File (inputFileName).getName();
        String nameWithoutExtension = fileName.substring(0, fileName.lastIndexOf("."));
        File outputDir = new File (TRANSFORM_OUTPUT_DIR);
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
