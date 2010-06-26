/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.ow.dilemma.ant.fetch;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
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

    private Get getTask;
    private Expand expandTask;
    private URL downloadUrl;
    private File destination;
//    private Project localProject;
    private String className;
    private Path classpath;

    public FetchTask() {
        super();
        getTask = new Get();
        expandTask = new Expand();
    }

//    @Override
//    public void setProject(Project project) {
//        localProject = project;
//    }

    @Override
    public void execute() throws BuildException {

        if (className != null) {
//            try {
//                getProject().getCoreLoader().loadClass(className);
////                Class.forName(className);
//                log("Class found, skipping");
//
//                return;
//            } catch (ClassNotFoundException exception) {
//                log("Class not found found "+className);
//            }
            Available av = new Available();
            av.setClassname(className);
            av.setProject(getProject());
            av.setClasspath(classpath);
            if(av.eval()){
                log("Class "+className+" is already present, skipping download.");
                return;
            }

        }

        File tmpFile = null;
        try {
            tmpFile = File.createTempFile("FetchTask", "tmp");
            getFromUrl(tmpFile);

            unzipToDirectory(tmpFile);

        } catch (IOException ex) {
            throw new BuildException(ex);

        } finally {
            if (tmpFile != null) {
                tmpFile.delete();
            }
        }
    }

    private void getFromUrl(File tmp) throws BuildException {
        try {
            getTask.setProject(getProject());
            getTask.setSrc(downloadUrl);
            getTask.setDest(tmp);
            getTask.setVerbose(true);
            getTask.setRetries(5);
            getTask.setTaskName(getTaskName());
            getTask.execute();

        } catch (Exception ex) {
            throw new BuildException(ex);
        }
    }

    public void setUrl(URL u) {
        downloadUrl = u;
    }

    public void setDest(String d) {
        destination = new File(d);
    }

    public void addPatternset(PatternSet set) {
        expandTask.addPatternset(set);
    }

    public void setClassname(String n) {
        this.className = n;
    }

    private void unzipToDirectory(File tmp) {

        if (!destination.exists()) {
            log("Creating "+getCanonicalPath(destination));
            destination.mkdirs();
        }

        expandTask.setProject(getProject());
        expandTask.setSrc(tmp);
        expandTask.setDest(getCanonicalFile(destination));
        expandTask.setTaskName(getTaskName());

        Mapper m = expandTask.createMapper();
        m.add(new FlatFileNameMapper());

        expandTask.execute();
    }

    private String getCanonicalPath(File in){
        try {
            return in.getCanonicalPath();
        } catch (IOException ex) {
            return in.getAbsolutePath();
        }
    }

    private File getCanonicalFile(File in){
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
     * @param r a Reference to a Path instance to be used as the classpath
     *          value.
     */
    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }
}
