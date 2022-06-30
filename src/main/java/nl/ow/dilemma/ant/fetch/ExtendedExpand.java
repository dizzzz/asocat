/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package nl.ow.dilemma.ant.fetch;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.types.selectors.SelectorUtils;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ExtendedExpand extends Expand {

    private static final int BUFFER_SIZE = 1024;

    private boolean overwrite = true;
    private List<PatternSet> patternsets = new Vector<>();
    private boolean stripAbsolutePathSpec = true;

    private final List<Path> extractedFiles = new ArrayList<>();

    @Override
    public void setOverwrite(boolean b) {
        this.overwrite = b;
        super.setOverwrite(b);
    }

    @Override
    public void addPatternset(PatternSet set) {
        this.patternsets.add(set);
        super.addPatternset(set);
    }

    @Override
    public void setStripAbsolutePathSpec(boolean b) {
        this.stripAbsolutePathSpec = b;
        super.setStripAbsolutePathSpec(b);
    }

    /**
     * extract a file to a directory
     * @param fileUtils             a fileUtils object
     * @param srcF                  the source file
     * @param dir                   the destination directory
     * @param compressedInputStream the input stream
     * @param entryName             the name of the entry
     * @param entryDate             the date of the entry
     * @param isDirectory           if this is true the entry is a directory
     * @param mapper                the filename mapper to use
     * @throws IOException on error
     */
    @Override
    protected void extractFile(FileUtils fileUtils, File srcF, File dir,
                               InputStream compressedInputStream,
                               String entryName, Date entryDate,
                               boolean isDirectory, FileNameMapper mapper)
            throws IOException {

        final boolean entryNameStartsWithPathSpec = !entryName.isEmpty()
                && (entryName.charAt(0) == File.separatorChar
                || entryName.charAt(0) == '/'
                || entryName.charAt(0) == '\\');
        if (stripAbsolutePathSpec && entryNameStartsWithPathSpec) {
            log("stripped absolute path spec from " + entryName,
                    Project.MSG_VERBOSE);
            entryName = entryName.substring(1);
        }
        boolean allowedOutsideOfDest = Boolean.TRUE == getAllowFilesToEscapeDest()
                || null == getAllowFilesToEscapeDest() && !stripAbsolutePathSpec && entryNameStartsWithPathSpec;

        if (patternsets != null && !patternsets.isEmpty()) {
            String name = entryName.replace('/', File.separatorChar)
                    .replace('\\', File.separatorChar);

            Set<String> includePatterns = new HashSet<>();
            Set<String> excludePatterns = new HashSet<>();
            for (PatternSet p : patternsets) {
                String[] incls = p.getIncludePatterns(getProject());
                if (incls == null || incls.length == 0) {
                    // no include pattern implicitly means includes="**"
                    incls = new String[]{"**"};
                }

                for (String incl : incls) {
                    String pattern = incl.replace('/', File.separatorChar)
                            .replace('\\', File.separatorChar);
                    if (pattern.endsWith(File.separator)) {
                        pattern += "**";
                    }
                    includePatterns.add(pattern);
                }

                String[] excls = p.getExcludePatterns(getProject());
                if (excls != null) {
                    for (String excl : excls) {
                        String pattern = excl.replace('/', File.separatorChar)
                                .replace('\\', File.separatorChar);
                        if (pattern.endsWith(File.separator)) {
                            pattern += "**";
                        }
                        excludePatterns.add(pattern);
                    }
                }
            }

            boolean included = false;
            for (String pattern : includePatterns) {
                if (SelectorUtils.matchPath(pattern, name)) {
                    included = true;
                    break;
                }
            }

            for (String pattern : excludePatterns) {
                if (SelectorUtils.matchPath(pattern, name)) {
                    included = false;
                    break;
                }
            }

            if (!included) {
                // Do not process this file
                log("skipping " + entryName
                                + " as it is excluded or not included.",
                        Project.MSG_VERBOSE);
                return;
            }
        }
        String[] mappedNames = mapper.mapFileName(entryName);
        if (mappedNames == null || mappedNames.length == 0) {
            mappedNames = new String[] {entryName};
        }
        File f = fileUtils.resolveFile(dir, mappedNames[0]);
        if (!allowedOutsideOfDest && !fileUtils.isLeadingPath(dir, f, true)) {
            log("skipping " + entryName + " as its target " + f.getCanonicalPath()
                    + " is outside of " + dir.getCanonicalPath() + ".", Project.MSG_VERBOSE);
            return;
        }

        try {
            if (!overwrite && f.exists()
                    && f.lastModified() >= entryDate.getTime()) {
                log("Skipping " + f + " as it is up-to-date",
                        Project.MSG_DEBUG);
                return;
            }

            log("expanding " + entryName + " to " + f,
                    Project.MSG_VERBOSE);
            // create intermediary directories - sometimes zip don't add them
            File dirF = f.getParentFile();
            if (dirF != null) {
                dirF.mkdirs();
            }

            if (isDirectory) {
                f.mkdirs();
            } else {
                byte[] buffer = new byte[BUFFER_SIZE];
                try (OutputStream fos = Files.newOutputStream(f.toPath())) {
                    int length;
                    while ((length = compressedInputStream.read(buffer)) >= 0) {
                        fos.write(buffer, 0, length);
                    }
                }
                extractedFiles.add(f.toPath());
            }

            fileUtils.setFileLastModified(f, entryDate.getTime());
        } catch (FileNotFoundException ex) {
            log("Unable to expand to file " + f.getPath(),
                    ex,
                    Project.MSG_WARN);
        }

    }

    public List<Path> getExtractedFiles() {
        return extractedFiles;
    }
}
