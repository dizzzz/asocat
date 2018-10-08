/*
 * SubversionInfoImpl.java
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

import java.io.File;

import org.tmatesoft.svn.core.SVNException;

import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;

import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

/**
 * Wrapper around javaSVN classes.
 *
 * @author Dannes Wessels
 */
public class SubversionInfoImpl {

    /*
     * Initialize library for https
     */
    private static void setupSVN() {
        DAVRepositoryFactory.setup();
        SVNRepositoryFactoryImpl.setup();
    }

    /**
     * Creates a new instance of SubversionInfoImpl
     */
    public SubversionInfoImpl() {
        setupSVN();
    }

    /**
     * Get Subversion Revision from SVN server.
     * @param username SVN username
     * @param password SVN password
     * @throws SVNException javaSVN exception
     * @return SVN revision id, -1 when an error occured.
     */
    public long getRevision(String username, String password) throws SVNException {

        DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);

        SVNClientManager ourClientManager = SVNClientManager.newInstance(options, username, password);

        SVNInfo info = ourClientManager.getWCClient().doInfo(new File("."), SVNRevision.COMMITTED);
        return info.getCommittedRevision().getNumber();
    }
}
