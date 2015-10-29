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
import static org.apache.commons.lang3.StringUtils.*;

import java.io.File;

public class ImportDataConfig {
    private String database;
    private String collection;
    private String file;
    private Boolean dropOnImport = true;
    private Boolean upsertOnImport = true;
    private long timeout = 200000;

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
        if (isBlank(collection)) {
            return substringBeforeLast(substringAfterLast(this.file, File.separator), ".");
        } else {        
            return collection;
        }
    }

    public String getFile() {
        return file;
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
}
