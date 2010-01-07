/*
 * SubversionInfoTask.java
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

package nl.ow.dilemma.ant.svn;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.tmatesoft.svn.core.SVNException;

/**
 * Ant task for distilling SVN details like revision.
 *
 * @author Dannes Wessels
 */
public class SubversionInfoTask extends Task {
    
    private String username=null;
    private String password=null;
    private String url=null;
    
    /**
     * Set URL for SVN access.
     * @param url SVN url
     */
    public void setUrl(String url) {
        this.url = url;
    }
    
    /**
     * Set username for SVN access.
     * @param username SVN username
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * Set password for SVN access.
     * @param password SVN password
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    
    /**
     * Called by the project to let the task do its work.
     * @throws BuildException if something goes wrong with the build
     */
    @Override
    public void execute() throws BuildException {
        
        if(url==null){
            throw new BuildException("SVN: Please provide url");
        }
        
        if(username==null){
            throw new BuildException("SVN: Please provide username, e.g. anonymous");
        }
        
        if(password==null){
            throw new BuildException("SVN: Please provide password, e.g. anonymous");
        }
        
        SubversionInfoImpl jsh = new SubversionInfoImpl();
        long revision=-1;
        try {
            revision = jsh.getRevision(url, username, password);
        } catch (SVNException ex) {
            throw new BuildException(ex);
        }
        
        // Get details from parsed file
        getProject().setNewProperty("svn.revision", ""+revision);
    }
    
}
