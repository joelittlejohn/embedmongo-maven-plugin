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

import static org.junit.Assert.fail;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class InitDataMongoMojoTest {

    @Rule
    public TemporaryFolder createSchemaFolder = new TemporaryFolder();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private File rootFolder;
    private File rootFolderWithError;

    @Ignore("Need an instance of MongoDB to pass")
    @Test public void
    should_execute_instructions() throws MojoFailureException, MojoExecutionException, IOException {
        initFolder();
        try {
            new InitDataMongoMojo().setDatabaseName("myDB").setDataFolder(rootFolder).execute();
        } catch (Exception e) {
            fail("Should not fail!");
        }
    }

    @Test public void
    should_fail_when_database_name_is_not_provided() throws MojoFailureException, MojoExecutionException {
        thrown.expect(MojoExecutionException.class);
        thrown.expectMessage("Database name is missing");

        new InitDataMongoMojo().execute();
    }

    @Ignore("Need an instance of MongoDB to pass")
    @Test public void
    should_fail_to_execute_instruction_with_error() throws IOException, MojoFailureException, MojoExecutionException {
        initFolderWithError();

        thrown.expect(MojoExecutionException.class);
        thrown.expectMessage("Error while executing instructions from file '" + rootFolderWithError.listFiles()[0].getName());

        new InitDataMongoMojo().setDatabaseName("myDB").setDataFolder(rootFolderWithError).execute();
    }

    private void initFolder() throws IOException {
        File instructionsFile = createSchemaFolder.newFile();
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(instructionsFile));
            out.write("db.dropDatabase();\n");
            out.write("db.users.createIndex( { email: 1 }, { unique: true } );\n");
        } finally {
            if (out != null) {
                out.close();
            }
        }
        rootFolder = instructionsFile.getParentFile();
        rootFolder.mkdir();
    }

    private void initFolderWithError() throws IOException {
        File instructionsFile = createSchemaFolder.newFile();
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(instructionsFile));
            out.write("db.unknownInstruction();\n");
        } finally {
            if (out != null) {
                out.close();
            }
        }
        rootFolderWithError = instructionsFile.getParentFile();
        rootFolderWithError.mkdir();
    }
}
