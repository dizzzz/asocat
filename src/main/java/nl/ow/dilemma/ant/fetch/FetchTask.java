/*
 * FetchTask.java
 *
 * Copyright (C) 2010  Dannes Wessels (dizzzz_at_gmail_com)
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
package nl.ow.dilemma.ant.fetch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Available;
import org.apache.tools.ant.taskdefs.Get;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.FlatFileNameMapper;

/**
 * Ant task for download and unzip files from internet. Temporary files are automatically cleaned
 * up. This task is a wrapper around existing tasks.
 *
 * @see org.apache.tools.ant.taskdefs.Available
 * @see org.apache.tools.ant.taskdefs.Get
 * @see org.apache.tools.ant.taskdefs.Expand
 *
 * @author Dannes Wessels
 */
public class FetchTask extends Task {

    private URL downloadUrl;
    private File destination;
    private String className;
    private Path classpath;
    private List<PatternSet> patternSets = new ArrayList<PatternSet>();
    private boolean useCache = false;
    
    private int maxtime=0;
    private boolean failonerror=true;

    /**
     *  Execute task, called by Ant after setting all attributes. If no patterns are set
     * the file is not extracted nor deleted
     * 
     * @throws BuildException When something bad happens,
     */
    @Override
    public void execute() throws BuildException {


        // If class name is set, check availability class.
        if (className != null) {

            // Setup taskdef Available
            Available av = new Available();
            av.setClassname(className);
            av.setProject(getProject());
            av.setClasspath(classpath);

            // Perform actual check on availability class.
            if (av.eval()) {
                log("Class " + className + " is already present, skipping download.");
                return;
            }
        }


        // Execute get and unzip
        File tmpFile = null;
        String filename = getFile(downloadUrl);

        try {
            // Try to get localFile
            File localFile = getCachedFile(filename);

            if (localFile != null && useCache) {
                // File is in archive, so copy from local disk
                if (patternSets.size() > 0) {
                    // Patterns supplied, extract
                    unzipToDirectory(localFile, destination, patternSets);
                } else {
                    log("Copy file from cache: " + localFile.getCanonicalPath());
                    copyFileToDirectory(localFile, null, destination);
                }

            } else {
                // Cached file does not exist
                tmpFile = File.createTempFile("FetchTask", "tmp");
                getFromUrl(downloadUrl, tmpFile);

                // Copy file to cache
                if (useCache) {
                    File fetchDir = getFetchDir();
                    log("Write file " + filename + " to cache " + fetchDir);
                    copyFileToDirectory(tmpFile, filename, fetchDir);
                }

                if (patternSets.size() > 0) {
                    // Patterns supplied, extract
                    unzipToDirectory(tmpFile, destination, patternSets);

                } else {
                    // Move file
                    File destFile = new File(destination, filename);
                    log("Moving to " + destFile.getAbsolutePath());
                    boolean success = tmpFile.renameTo(destFile);
                    if(!success){
                        // Fallback
                        copyFileToDirectory(tmpFile, filename, destFile.getParentFile());
                        success=tmpFile.delete();
                        if(!success){
                            log("Could not remove " + tmpFile.getCanonicalPath());
                            tmpFile.deleteOnExit();
                        }
                    }
                }
            }


        } catch (Exception ex) {
            if(failonerror){
                throw new BuildException(ex);
            } else {
                log(ex.getMessage());
            }

        } finally {
            if (tmpFile != null && patternSets.size() > 0) {
                tmpFile.delete();
            }
        }

    }

    /**
     *  Download file from URL using GET task ant
     *
     * @param url   URL of to-be-downloaded file
     * @param tmp   Store Location for downloaded file
     * 
     * @throws BuildException Thrown when something wrong happens.
     */
    private void getFromUrl(URL url, File tmp) throws BuildException {
        try {
            Get getTask = new Get();
            getTask.setProject(getProject());
            getTask.setSrc(url);
            getTask.setDest(tmp);
            getTask.setVerbose(true);
            getTask.setRetries(5);
            getTask.setMaxTime(maxtime);
            getTask.setTaskName(getTaskName());
            getTask.execute();

        } catch (Throwable ex) {
            throw new BuildException(ex);
        }
    }

    /**
     *  Unzip file to specified directory.
     *
     * @param zipFile         Zip file
     * @param unzipDirectory  Location to unzip files in.
     * @param allPatternSets  Collecttion of all pattern sets.
     *
     * @throws BuildException Thrown when something unexpected happens.
     */
    private void unzipToDirectory(File zipFile, File unzipDirectory, List<PatternSet> allPatternSets) throws BuildException {

        // Check if unzip location exists and isn't a regular file
        if (unzipDirectory.exists() && !unzipDirectory.isDirectory()) {
            throw new BuildException(unzipDirectory.getAbsolutePath() + "already exists and is not an directory.");
        }

        // If unzip directory does not exist, create it.
        if (!unzipDirectory.exists()) {
            log("Creating " + getCanonicalPath(unzipDirectory));
            unzipDirectory.mkdirs();
        }

        // Setup expand task
        Expand expandTask = new Expand();
        expandTask.setProject(getProject());
        expandTask.setSrc(zipFile);
        expandTask.setDest(getCanonicalFile(unzipDirectory));
        expandTask.setTaskName(getTaskName());

        // Add all patterns to expand task
        for (PatternSet patternSet : allPatternSets) {
            expandTask.addPatternset(patternSet);
        }

        // The files should be extracted without hierarchy
        Mapper mapper = expandTask.createMapper();
        mapper.add(new FlatFileNameMapper());

        // Perform actual unzip
        expandTask.execute();
    }

    /**
     *  Set download URL, invoked by Ant.
     * @param url Location of file.
     */
    public void setUrl(URL url) {
        downloadUrl = url;
    }

    /**
     *  Set destination of unzipped files, invoked by Ant.
     * @param dest
     */
    public void setDest(File dest) {
        destination = dest;
    }

    /**
     * Set pettern of to be unzipped ziles, invoked by Ant.
     * @param set Patterns to match file sin ZIP file.
     */
    public void addPatternset(PatternSet set) {
        patternSets.add(set);
    }

    /**
     * Set classname that is checked to be present, invoked by Ant.
     * @param name Name of class.
     */
    public void setClassname(String name) {
        this.className = name;
    }

    /**
     *  Helper method to determine the File's canonicicak path. If conversion
     * fails the absolute path is returned.
     */
    private String getCanonicalPath(File in) {
        try {
            return in.getCanonicalPath();
        } catch (IOException ex) {
            return in.getAbsolutePath();
        }
    }

    /**
     *  Helper method to determine the File's canonicicak file. If conversion
     * fails the absolute file is returned.
     */
    private File getCanonicalFile(File in) {
        try {
            return in.getCanonicalFile();
        } catch (IOException ex) {
            return in.getAbsoluteFile();
        }
    }

    /**
     * Set the classpath to be used when searching for classes and resources.
     *
     * @param classpath an Ant Path object containing the search path.
     */
    public void setClasspath(Path classpath) {
        createClasspath().append(classpath);
    }

    /**
     * Classpath to be used when searching for classes and resources.
     *
     * @return an empty Path instance to be configured by Ant.
     */
    public Path createClasspath() {
        if (this.classpath == null) {
            this.classpath = new Path(getProject());
        }
        return this.classpath.createPath();
    }

    /**
     * Set the classpath by reference.
     *
     * @param reference a Reference to a Path instance to be used as the classpath
     *          value.
     */
    public void setClasspathRef(Reference reference) {
        createClasspath().setRefid(reference);
    }

    public boolean getUsecache() {
        return useCache;
    }

    public void setUsecache(boolean use) {
        useCache = use;
    }

    /**
     *  Set timeout, 0 is default
     */
    public void setMaxtime(int maxtime) {
        this.maxtime = maxtime;
    }
    
    public int getMaxtime() {
        return maxtime;
    }
    
    public boolean isFailonerror() {
        return failonerror;
    }

    public void setFailonerror(boolean failonerror) {
        this.failonerror = failonerror;
    }
    
    /**
     *  Extract filename from URL
     */
    private String getFile(URL url) {
        String path = url.getPath();
        if (path.contains("/")) {
            int position = path.lastIndexOf("/");
            return path.substring(position + 1);
        }
        return path;
    }

    /**
     * Get file from local fetch directory.
     *
     * @param fileName Name to be fetched locally
     * @return The file, else NULL
     */
    private File getCachedFile(String fileName) {

        File fetchDir = getFetchDir();

        File file = new File(fetchDir, fileName);
        if (!file.exists()) {
            return null;
        }

        if (!file.canRead()) {
            throw new BuildException(fetchDir.getAbsolutePath() + " is not readable");
        }

        return file;
    }

    /**
     *  Get reference to and create fetchdir
     */
    private File getFetchDir() {

        File homedir = new File(System.getProperty("user.home"));

        File fetchDir = new File(homedir, ".fetch");
        if (fetchDir.exists()) {
            if (!fetchDir.isDirectory()) {
                throw new BuildException(fetchDir.getAbsolutePath() + " is not a directory");
            }
            if (!fetchDir.canWrite()) {
                throw new BuildException(fetchDir.getAbsolutePath() + " is not writable");
            }
        } else {
            log("Creating fetch local cache directory " + fetchDir.getAbsolutePath());
            fetchDir.mkdirs();
        }
        return fetchDir;
    }

    /**
     *  Copy file to directory
     */
    private void copyFileToDirectory(File src, String name, File directory) {
        InputStream in = null;
        OutputStream out = null;
        try {
            if (name == null) {
                name = src.getName();
            }
            File toFile = new File(directory, name);

            in = new FileInputStream(src);
            out = new FileOutputStream(toFile);

            // Transfer bytes from in to out
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.flush();
            out.close();

        } catch (Throwable ex) {
            throw new BuildException(ex);

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    //
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    //
                }
            }
        }
    }
}
