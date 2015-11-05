/**
 * Copyright © 2012 Pierre-Jean Vardanéga
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
import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.util.Scanner;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

/**
 * When invoked, this goal connects to an instance of mongo and execute some
 * instructions to add data.
 *
 * You should use the same javascript syntax that you would use in the mongo
 * client.
 *
 */
@Mojo(name = "mongo-scripts", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class MongoScriptsMojo extends AbstractEmbeddedMongoMojo {

    /**
     * Folder that contains all scripts to execute.
     */
    @Parameter(property = "scriptsDirectory", required = true)
    private File scriptsDirectory;

    /**
     * The name of the database where data will be stored.
     */
    @Parameter(property = "databaseName", required = true)
    private String databaseName;

    public MongoScriptsMojo() {
    }

    MongoScriptsMojo(File scriptsDirectory, int port, String databaseName) {
        super(port);
        this.scriptsDirectory = scriptsDirectory;
        this.databaseName = databaseName;
    }

    @Override
    public void executeStart() throws MojoExecutionException, MojoFailureException {
        DB db = connectToMongoAndGetDatabase();

        if (scriptsDirectory.isDirectory()) {
            Scanner scanner = null;
            StringBuilder instructions = new StringBuilder();
            File[] files = scriptsDirectory.listFiles();

            if (files == null) {
                getLog().info("Can't read scripts directory: " + scriptsDirectory.getAbsolutePath());

            } else {
                getLog().info("Folder " + scriptsDirectory.getAbsolutePath() + " contains " + files.length + " file(s):");

                for (File file : files) {
                    if (file.isFile()) {
                        try {
                            scanner = new Scanner(file);
                            while (scanner.hasNextLine()) {
                                instructions.append(scanner.nextLine()).append("\n");
                            }
                        } catch (FileNotFoundException e) {
                            throw new MojoExecutionException("Unable to find file with name '" + file.getName() + "'", e);
                        } finally {
                            if (scanner != null) {
                                scanner.close();
                            }
                        }
                        CommandResult result;
                        try {
                            result = db.doEval("(function() {" + instructions.toString() + "})();", new Object[0]);
                        } catch (MongoException e) {
                            throw new MojoExecutionException("Unable to execute file with name '" + file.getName() + "'", e);
                        }
                        if (!result.ok()) {
                            getLog().error("- file " + file.getName() + " parsed with error: " + result.getErrorMessage());
                            throw new MojoExecutionException("Error while executing instructions from file '" + file.getName() + "': " + result.getErrorMessage(), result.getException());
                        }
                        getLog().info("- file " + file.getName() + " parsed successfully");
                    }
                }
            }
            getLog().info("Data initialized with success");
        }
    }

    DB connectToMongoAndGetDatabase() throws MojoExecutionException {
        if (databaseName == null || databaseName.trim().length() == 0) {
            throw new MojoExecutionException("Database name is missing");
        }

        MongoClient mongoClient;
        try {
            mongoClient = new MongoClient("localhost", getPort());
        } catch (UnknownHostException e) {
            throw new MojoExecutionException("Unable to connect to mongo instance", e);
        }
        getLog().info("Connected to MongoDB");
        return mongoClient.getDB(databaseName);
    }
}
