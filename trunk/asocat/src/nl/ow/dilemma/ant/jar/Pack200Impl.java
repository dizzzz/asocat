/*
 * Pack200Impl.java
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.jar.Pack200.Packer;
import java.util.jar.Pack200.Unpacker;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *   Implementation class for (un)packing jar files with pack200.
 *
 * @author Dannes Wessels
 */
public class Pack200Impl {
    
    public static String PACK_CLASS="java.util.jar.Pack200";
    
    /**
     * Checks wether Pack200 is available. 
     *
     * @return TRUE is Pack200 libraries are available.
     */
    public static boolean hasPack200(){
        try {
            Class.forName(PACK_CLASS);
        } catch (ClassNotFoundException ex) {
            return false;
        }
        return true;
    }
    
    /**
     * Pack and unpack jar file.
     *
     * @param file Jarfile that must be repacked.
     * @throws java.io.IOException When something wrong happens.
     */
    public void repack(File file) throws IOException{
        
        File tmpfile = File.createTempFile("repack2","tmp");

        pack(file, tmpfile, false);
        unpack(tmpfile, file, false);
        
        tmpfile.delete();
    }
    
    /**
     * Pack jar file.
     *
     * @param jarFile Jarfile that must be Pack200ed.
     * @throws java.io.IOException When something wrong happens.
     */
    public void pack(File jarFile) throws IOException {
        File packedFile = new File(jarFile.getAbsolutePath() + ".pack.gz");
        pack(jarFile, packedFile, true);
    }
    
    /**
     * Unpack jar file.
     *
     * @param packedJarFile Packed jarfile that must be Unpack200ed.
     * @throws java.io.IOException When something wrong happens.
     */
    public void unpack(File packedJarFile) throws IOException {
        
        String packedName=packedJarFile.getAbsolutePath();
        
        String jarFilename = (packedName.endsWith(".pack.gz"))
            ? packedName.substring(0,packedName.length()-8)
            : "unpacked";
        
        File jarFile = new File(jarFilename);
        unpack(packedJarFile, jarFile, true);
    }
    
    /**
     * Unpack jar file.
     *
     * @param jarFile Packed jar file that must be Unpack200ed.
     * @param packedJarFile Unpacked jar file.
     * @param zip TRUE is GZIP compression must be applied.
     * @throws java.io.IOException When something wrong happens.
     */
    public void pack(File jarFile, File packedJarFile, boolean zip) throws IOException {
        
        // Pack200 classes (Java5+) check
        if(!hasPack200()){
            System.err.println(Pack200Impl.PACK_CLASS + " not found, Pack200 features not available.");
            return; 
        }
        
        Packer packer = Pack200.newPacker();
        
        JarFile inputFile = new JarFile(jarFile);
        FileOutputStream fos = new FileOutputStream(packedJarFile);
        
        
        OutputStream os = (zip)
                ? new BufferedOutputStream(new GZIPOutputStream(fos))
                : new BufferedOutputStream(fos);
        
        packer.pack(inputFile, os );
        os.flush(); 
        os.close();
    }
    
    /**
     * Unpack pack200ed jar file.
     *
     * @param packedJarFile  Packed jarfile that must be Unpack200ed.
     * @param jarFile Unpacked jar file.
     * @param zip TRUE is GZIP compression must be applied.
     * @throws java.io.IOException When something wrong happens.
     */
    public void unpack(File packedJarFile, File jarFile, boolean zip) throws IOException {
        
        // Pack200 classes (Java5+) check
        if(!hasPack200()){
            System.err.println(Pack200Impl.PACK_CLASS + " not found, Pack200 features not available.");
            return; 
        }
        
        Unpacker unpacker = Pack200.newUnpacker();
        
        FileInputStream fis = new FileInputStream(packedJarFile);
        JarOutputStream jos = new JarOutputStream( new FileOutputStream(jarFile) );
        
        InputStream is = (zip)
                ? new BufferedInputStream(new GZIPInputStream(fis))
                : new BufferedInputStream(fis);
        
        unpacker.unpack(is, jos);
        
        fis.close();
        jos.flush();
        jos.close();
    }

    
}
