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

import de.flapdoodle.embed.mongo.MongoImportExecutable;
import de.flapdoodle.embed.mongo.MongoImportProcess;
import de.flapdoodle.embed.mongo.MongoImportStarter;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.process.runtime.Network;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @goal import
 * @phase pre-integration-test
 */
public class ImportEmbeddedMongoMojo extends AbstractMojo {
    /**
     * @parameter
     */
    private ImportDataConfig[] imports;

    /**
     * @parameter expression="${embedmongo.defaultImportDatabase}"
     */
    private String defaultImportDatabase;

    /**
     * @parameter expression="${embedmongo.import.wait}" default-value="false"
     */
    private Boolean wait;

    /**
     * @parameter expression="${embedmongo.parallel}" default-value="false"
     */
    private Boolean parallel;
    /**
     * @parameter expression="${embedmongo.skip}" default-value="false"
     */
    private boolean skip;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            return;
        }

        MongodProcess mongod = (MongodProcess)getPluginContext().get(StartEmbeddedMongoMojo.MONGOD_CONTEXT_PROPERTY_NAME);

        if(mongod == null) {
            throw new MojoExecutionException("Can't import without an EmbeddedMongoDB running");
        }

        try {
            sendImportScript(mongod);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

    }


    private void sendImportScript(MongodProcess mongod) throws IOException, InterruptedException, MojoExecutionException {
        List<MongoImportProcess> pendingMongoProcess = new ArrayList<MongoImportProcess>();

        if(imports == null || imports.length == 0) {
            getLog().error("No imports found, check your configuration");

            return;
        }

        IMongodConfig config = mongod.getConfig();

        getLog().info("Default import database: " + defaultImportDatabase);

        for(ImportDataConfig importData: imports) {

            getLog().info("Import " + importData);

            verify(importData);
            String database = importData.getDatabase();

            if(StringUtils.isBlank(database)){
                database = defaultImportDatabase;
            }

            IMongoImportConfig mongoImportConfig = new MongoImportConfigBuilder()
                    .version(config.version())
                    .net(new Net(config.net().getPort(), Network.localhostIsIPv6()))
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

        if(wait) {
            getLog().info("STARTED - MongoDB up and all imports done.");
            mongod.waitFor();
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
