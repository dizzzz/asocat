/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.ow.dilemma.ant.fetch;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;
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
 *
 * @author wessels
 */
public class FetchTask extends Task {

//    private Expand expandTask;
    private URL downloadUrl;
    private File destination;
    private String className;
    private Path classpath;
    private PatternSet ps = new PatternSet();

    @Override
    public void execute() throws BuildException {


        // If class name is set, check availability class
        if (className != null) {

            // Setup taskdef Available
            Available av = new Available();
            av.setClassname(className);
            av.setProject(getProject());
            av.setClasspath(classpath);

            // Perform actual check
            if (av.eval()) {
                log("Class " + className + " is already present, skipping download.");
                return;
            }
        }

        // Execute get and unzip
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile("FetchTask", "tmp");
            getFromUrl(downloadUrl, tmpFile);
            unzipToDirectory(tmpFile, destination, ps);

        } catch (IOException ex) {
            throw new BuildException(ex);

        } finally {
            if (tmpFile != null) {
                tmpFile.delete();
            }
        }
    }

    /**
     *  Download file from URL using GET task ant
     *
     * @param url   URL of to-be-downloaded file
     * @param tmp   Store Location for downloaded file
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
            getTask.setTaskName(getTaskName());
            getTask.execute();

        } catch (Throwable ex) {
            throw new BuildException(ex);
        }
    }

    /**
     *  Unzip file to specified directory
     *
     * @param zipFile   Zip file
     * @param unzipDirectory  Location to unzip files in.
     */
    private void unzipToDirectory(File zipFile, File unzipDirectory, PatternSet set) throws BuildException {

        if (unzipDirectory.exists() && !unzipDirectory.isDirectory()) {
            throw new BuildException(unzipDirectory.getAbsolutePath() + "already exists and is not an directory.");
        }

        if (!unzipDirectory.exists()) {
            log("Creating " + getCanonicalPath(unzipDirectory));
            unzipDirectory.mkdirs();
        }

        Expand expandTask = new Expand();
        expandTask.setProject(getProject());
        expandTask.setSrc(zipFile);
        expandTask.setDest(getCanonicalFile(unzipDirectory));
        expandTask.setTaskName(getTaskName());
        expandTask.addPatternset(set);

        //Note that the file patterns are already registered to the expand task.
        Mapper m = expandTask.createMapper();
        m.add(new FlatFileNameMapper());

        // perform execution
        expandTask.execute();
    }

    public void setUrl(URL u) {
        downloadUrl = u;
    }

    public void setDest(File d) {
        destination = d;
    }

    public void addPatternset(PatternSet set) {
        ps.append(set, getProject());
    }

    public void setClassname(String n) {
        this.className = n;
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
}
