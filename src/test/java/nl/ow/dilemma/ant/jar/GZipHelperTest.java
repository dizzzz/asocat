/*
 * GZipHelperTest.java
 * JUnit based test
 *
 * Copyright (C) 2006  Dannes Wessels (dizzzz_at_gmail_com)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA
 */

package nl.ow.dilemma.ant.jar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import junit.framework.*;
import java.io.File;
import java.util.Arrays;

/**
 *
 * @author wessels
 */
public class GZipHelperTest extends TestCase {
    
    public GZipHelperTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(GZipHelperTest.class);
        
        return suite;
    }

    /**
     * Test round trip src-gzip-gunzip
     */
    public void testGzipRoundTrip() throws Exception {
        System.out.println("testGzipRoundTrip");
        
        File src = new File(getClass().getResource("/xmldb.jar").toURI());
        byte[] srcData=Tools.readArrayFromFile(src);
        
        // Gzip
        ByteArrayOutputStream zippedDataBAOS = new ByteArrayOutputStream();
        Tools.gzip( new ByteArrayInputStream(srcData),  zippedDataBAOS );
        byte[] zippedData = zippedDataBAOS.toByteArray();
        
        // Gunzip
        ByteArrayOutputStream unzippedDataBAOS = new ByteArrayOutputStream();
        Tools.gunzip( new ByteArrayInputStream(zippedDataBAOS.toByteArray()),  unzippedDataBAOS );
        byte[] resultData = unzippedDataBAOS.toByteArray();
        
        // Compare
        assertTrue("Compressed filesize must be smaller", (zippedData.length <= srcData.length));
        assertEquals("Data length check",srcData.length, resultData.length);
        assertTrue("Byte array content check", Arrays.equals(srcData, resultData));
        assertFalse("Content check src vs zipped", Arrays.equals(srcData, zippedData) );

    }
    
}
