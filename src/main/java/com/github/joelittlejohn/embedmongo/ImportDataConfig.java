/**
 * Copyright © 2012 Pablo Diaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.joelittlejohn.embedmongo;

import java.io.File;
import java.io.FileFilter;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

public class ImportDataConfig {
    private String database;
    private String collection;
    private String file;
    private Boolean dropOnImport = true;
    private Boolean upsertOnImport = true;
    private long timeout = 200000;
    
    private File[] collectionFiles;

    public ImportDataConfig() {
    }

    public ImportDataConfig(String database, String collection, String file, Boolean dropOnImport, Boolean upsertOnImport, long timeout) {
        this.database = database;
        this.collection = collection;
        this.file = file;
        this.dropOnImport = dropOnImport;
        this.upsertOnImport = upsertOnImport;
        this.timeout = timeout;
    }

    public String getDatabase() {
        return database;
    }
    
    public String getCollection() {
        if (isBlankCollectionName(collection)) {
            return getCollectionNameFromFilePath(this.file);
        } else {
            return collection;
        }
    }
    
    public String[] getCollections() {
        if (!isMultiCollectionEnabled()) return new String[]{getCollection()};
        else {
            String[] listCollectionFilesPath = listCollectionFilesPath();
            String[] fileNames = new String[listCollectionFilesPath.length];
            for (int i = 0; i < listCollectionFilesPath.length; i++) {
                fileNames[i] = getCollectionNameFromFilePath(listCollectionFilesPath[i]);
            }
            return fileNames;
        }
    }

    public String getFile() {
        return file;
    }
    
    public String[] getFiles() {
        if (!isMultiCollectionEnabled()) return new String[]{getFile()};
        else                             return listCollectionFilesPath();
    }

    public Boolean getDropOnImport() {
        return dropOnImport;
    }

    public Boolean getUpsertOnImport() {
        return upsertOnImport;
    }

    public long getTimeout() {
        return timeout;
    }

    @Override
    public String toString() {
        return "ImportDataConfig{" +
                "database='" + database + '\'' +
                ", collection='" + collection + '\'' +
                ", file='" + file + '\'' +
                ", dropOnImport=" + dropOnImport +
                ", upsertOnImport=" + upsertOnImport +
                ", timeout=" + timeout +
                '}';
    }
    
    //
    //
    //
    
    private boolean isMultiCollectionEnabled() {
        return isBlankCollectionName(this.collection) && isMultiCollectionFile(this.file);
    }
    
    private boolean isBlankCollectionName(String collectionName) {
        return isBlank(collectionName);
    }
    
    private boolean isMultiCollectionFile(String file) {
        return (new File(file).isDirectory());
    }
    
    private String getCollectionNameFromFilePath(String file) {
        return substringBeforeLast(substringAfterLast(file, File.separator), ".");
    }

    private File[] listCollectionFiles() {
        if (collectionFiles == null) {
            collectionFiles = new File(file).listFiles(new FileFilter() {
                                @Override
                                public boolean accept(File file) {
                                    // a directory ending with .json ? do not think so but ...
                                    return file.isFile() && file.getPath().endsWith(".json") || file.getPath().endsWith(".JSON");
                                }
                            });
        }
        return collectionFiles;
    }

    private String[] listCollectionFilesPath() {
        File[] files = listCollectionFiles();
        String[] filesPath = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            filesPath[i] = files[i].getAbsolutePath();
        }
        return filesPath;
    }
}