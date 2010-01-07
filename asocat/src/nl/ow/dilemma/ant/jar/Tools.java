/*
 * Tools.java
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

import nl.ow.dilemma.external.ByteArrayOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *  A handfull set of helper methods. 
 *
 * @author Dannes Wessels
 */
public class Tools {
    
    public static void writeArrayToFile(File resultFile, byte[] fullFile) throws IOException, FileNotFoundException{
        FileOutputStream  file = new FileOutputStream(resultFile);
        file.write(fullFile);
        file.close();
    }
    
    public static byte[] readArrayFromFile(File srcFile) throws FileNotFoundException, IOException{
        FileInputStream fis = new FileInputStream(srcFile);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        byte[] buf = new byte[4096];
        int len;
        while ((len = fis.read(buf)) > 0) {
            baos.write(buf, 0, len);
        }
        fis.close();
        baos.close();
        
        return baos.toByteArray();
    }
    
    public static void gzip(InputStream is, OutputStream os) throws IOException {

        GZIPOutputStream gos = new GZIPOutputStream(os);
        
        byte[] buf = new byte[4096];
        int len;
        while ((len = is.read(buf)) > 0) {
            gos.write(buf, 0, len);
        }
        is.close();
        gos.close();
        
    }
    
    public static void gzip(File jarfile){
        if(!jarfile.canRead()){
            System.err.println("Cannot read file "+jarfile.getAbsolutePath());
            return;
        }
        
        File dest = new File(jarfile.getAbsolutePath() + ".gz");
        try {
            
            FileInputStream fis = new FileInputStream(jarfile);
            FileOutputStream fos = new FileOutputStream(dest);
            
            gzip(fis, fos);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public static void gunzip(InputStream is, OutputStream os) throws IOException {

        GZIPInputStream gis = new GZIPInputStream(is);
        
        byte[] buf = new byte[4096];
        int len;
        while ((len = gis.read(buf)) > 0) {
            os.write(buf, 0, len);
        }
        gis.close();
        os.close();  
    }
    
    public static void gunzip(File jarfile){
        if(!jarfile.canRead()){
            System.err.println("Cannot read file "+jarfile.getAbsolutePath());
            return;
        }
        
        String shortName=jarfile.getAbsolutePath();
        
        if(shortName.endsWith(".gz")){
            shortName=shortName.substring(0,(shortName.length()-3) );
        } else{
            System.err.println("Filename does not end with '.gz'");
            return;
        }
            
        
        File dest = new File(shortName);
        try {
            
            FileInputStream fis = new FileInputStream(jarfile);
            FileOutputStream fos = new FileOutputStream(dest);
            
            gunzip(fis, fos);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    
}
