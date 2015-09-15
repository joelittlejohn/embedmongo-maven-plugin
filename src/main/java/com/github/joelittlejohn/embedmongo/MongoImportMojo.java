/**
 * Copyright © 2015 Pablo Diaz
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

import de.flapdoodle.embed.mongo.MongoImportExecutable;
import de.flapdoodle.embed.mongo.MongoImportProcess;
import de.flapdoodle.embed.mongo.MongoImportStarter;
import de.flapdoodle.embed.mongo.config.IMongoImportConfig;
import de.flapdoodle.embed.mongo.config.MongoImportConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.Timeout;
import de.flapdoodle.embed.process.runtime.Network;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Mojo(name="mongo-import", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class MongoImportMojo extends AbstractEmbeddedMongoMojo {
    @Parameter
    private ImportDataConfig[] imports;

    @Parameter(property = "embedmongo.defaultImportDatabase")
    private String defaultImportDatabase;

    @Parameter(property = "embedmongo.parallel", defaultValue = "false")
    private Boolean parallel;

    @Override
    public void executeStart() throws MojoExecutionException, MojoFailureException {
        try {
            sendImportScript();
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

    }

    private void sendImportScript() throws IOException, InterruptedException, MojoExecutionException {
        List<MongoImportProcess> pendingMongoProcess = new ArrayList<MongoImportProcess>();

        if(imports == null || imports.length == 0) {
            getLog().error("No imports found, check your configuration");

            return;
        }

        getLog().info("Default import database: " + defaultImportDatabase);

        for(ImportDataConfig importData: imports) {

            getLog().info("Import " + importData);

            verify(importData);
            String database = importData.getDatabase();

            if(StringUtils.isBlank(database)){
                database = defaultImportDatabase;
            }

            IMongoImportConfig mongoImportConfig = new MongoImportConfigBuilder()
                    .version(getVersion())
                    .net(new Net(getPort(), Network.localhostIsIPv6()))
                    .db(database)
                    .collection(importData.getCollection())
                    .upsert(importData.getUpsertOnImport())
                    .dropCollection(importData.getDropOnImport())
                    .importFile(importData.getFile())
                    .jsonArray(true)
                    .timeout(new Timeout(importData.getTimeout()))
                    .build();

            MongoImportExecutable mongoImport = MongoImportStarter.getDefaultInstance().prepare(mongoImportConfig);

            MongoImportProcess importProcess = mongoImport.start();

            if(parallel){
                pendingMongoProcess.add(importProcess);
            }else{
                waitFor(importProcess);
            }

        }

        for(MongoImportProcess importProcess: pendingMongoProcess){
            waitFor(importProcess);
        }

    }

    private void waitFor(MongoImportProcess importProcess) throws InterruptedException, MojoExecutionException {
        int code = importProcess.waitFor();

        if(code != 0){
            throw new MojoExecutionException("Cannot import '" + importProcess.getConfig().getImportFile() + "'");
        }

        getLog().info("Import return code: " + code);

    }

    private void verify(ImportDataConfig config) {
        Validate.notBlank(config.getFile(), "Import file is required\n\n" +
                "<imports>\n" +
                "\t<import>\n" +
                "\t\t<file>[my file]</file>\n" +
                "...");
        Validate.isTrue(StringUtils.isNotBlank(defaultImportDatabase) || StringUtils.isNotBlank(config.getDatabase()), "Database is required you can either define a defaultImportDatabase or a <database> on import tags");
        Validate.notBlank(config.getCollection(), "Collection is required\n\n" +
                "<imports>\n" +
                "\t<import>\n" +
                "\t\t<collection>[my file]</collection>\n" +
                "...");

    }

}
