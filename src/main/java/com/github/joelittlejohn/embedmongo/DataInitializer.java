/**
 * Copyright © 2012 Joe Littlejohn
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
import org.apache.maven.plugin.logging.Log;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

/**
 * When invoked, this goal connects to an instance of mongo and execute some instructions
 * to add data.
 *
 * You should use the same javascript syntax that you would use in the mongo client.
 */
public class DataInitializer {

    private File initDirectory;
    private int port;
    private String databaseName;
    private Log logger;

    public DataInitializer(File initDirectory, int port, String databaseName, Log logger) {
        this.initDirectory = initDirectory;
        this.port = port;
        this.databaseName = databaseName;
        this.logger = logger;
    }

    public void insertData() throws MojoExecutionException, MojoFailureException {
        if (initDirectory.isDirectory()) {
            Scanner scanner = null;
            StringBuilder instructions = new StringBuilder();
            File[] files = initDirectory.listFiles();
            if (files != null && files.length > 0) {
                logger.info("Folder " + initDirectory.getAbsolutePath() + " contains " + files.length + " file(s):");
                DB db = getConnectToMongoAndGetDatabase();
                for (File file : files) {
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
                        logger.error("- file " + file.getName() + " parsed with error: " + result.getErrorMessage());
                        throw new MojoExecutionException("Error while executing instructions from file '"+file.getName()+"': " + result.getErrorMessage(), result.getException());
                    }
                    logger.info("- file " + file.getName() + " parsed successfully");
                }
                logger.info("Data initialized with success");
            } else {
                logger.info("No data to initialize");
            }
        }
    }

    DB getConnectToMongoAndGetDatabase() throws MojoExecutionException {
        if (databaseName == null || databaseName.trim().length() == 0) {
            throw new MojoExecutionException("Database name is missing");
        }

        MongoClient mongoClient;
        try {
            mongoClient = new MongoClient("localhost", port);
        } catch (UnknownHostException e) {
            throw new MojoExecutionException("Unable to connect to mongo instance", e);
        }
        logger.info("Connected to MongoDB");
        return mongoClient.getDB(databaseName);
    }
}
