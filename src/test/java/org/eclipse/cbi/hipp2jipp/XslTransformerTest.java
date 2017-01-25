package org.eclipse.cbi.hipp2jipp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class XslTransformerTest {

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

    @Test
    public void XslTransformerMain_build_Test() {
        XslTransformerMainTest("xml/build.hudson.xml", "build");
    }

    @Test
    public void XslTransformerMain_configJob_Test() {
        XslTransformerMainTest("xml/config.job.hudson-kapua.xml", "project");
    }
    
    @Test
    public void XslTransformerMain_configMain_Test() {
        XslTransformerMainTest("xml/config.main.hudson.xml", "hudson");
    }

    private void XslTransformerMainTest(String inputFileName, String expectedXmlRootNodeName) {
        String nameWithoutExtension = inputFileName.substring(0, inputFileName.lastIndexOf("."));
        File transformedFile = new File(nameWithoutExtension + XslTransformer.DEFAULT_TRANSFORMED_FILE_EXTENSION);
        if (transformedFile.exists()) {
            transformedFile.delete();
        }
        XslTransformer.main(new String[]{inputFileName});
        assertTrue(transformedFile.exists());
        
        // check root node in transformed file
        String xmlRootNodeName = XslTransformer.getXmlRootNodeName(transformedFile);
        assertEquals(expectedXmlRootNodeName, xmlRootNodeName);
    }
    
}
