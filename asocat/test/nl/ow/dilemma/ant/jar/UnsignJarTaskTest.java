/*
 * UnsignJarTaskTest.java
 * JUnit based test
 *
 * Created on December 7, 2006, 8:36 PM
 */

package nl.ow.dilemma.ant.jar;

import junit.framework.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/**
 *
 * @author wessels
 */
public class UnsignJarTaskTest extends TestCase {
    
    public UnsignJarTaskTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }


    public void testLargeFile() throws Exception {
        System.out.println("testLargeFile");

        UnsignJarImpl uj = new UnsignJarImpl();
        
        uj.unsign( new File("large.jar") );

    }
    
}
