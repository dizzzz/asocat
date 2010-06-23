/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.ow.dilemma.ant.fetch;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Get;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.types.PatternSet;
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
    private Project localProject;
    private String className;

    public FetchTask() {
        getTask = new Get();
        expandTask = new Expand();
    }

    @Override
    public void setProject(Project project) {
        localProject = project;
    }

    @Override
    public void execute() throws BuildException {
//        super.execute();

        if (className != null) {
            try {
                Class.forName(className);
//                log("Class found, skipping");

                return;
            } catch (ClassNotFoundException exception) {
                // ignore
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
            getTask.setProject(localProject);
            getTask.setSrc(downloadUrl);
            getTask.setDest(tmp);
            getTask.setVerbose(true);
            getTask.setRetries(5);
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
            destination.mkdirs();
        }

        expandTask.setProject(localProject);
        expandTask.setSrc(tmp);
        expandTask.setDest(destination);

        Mapper m = expandTask.createMapper();
        m.add(new FlatFileNameMapper());

        expandTask.execute();
    }
}
