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
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.EmbedMongoDB;

@RunWith(MockitoJUnitRunner.class)
public class InitDataMongoMojoTest {

    @Rule
    public TemporaryFolder createSchemaFolder = new TemporaryFolder();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final static int PORT = 27017;
    private File rootFolder;
    private File rootFolderWithError;

    @Test public void
    should_execute_instructions() throws MojoFailureException, MojoExecutionException, IOException {
        initFolder();
        try {
            new InitDataMongoMojoForTest(rootFolder, PORT, "myDB").execute();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Should not fail!");
        }
    }

    @Test public void
    should_fail_when_database_name_is_not_provided() throws MojoFailureException, MojoExecutionException, IOException {
        initFolder();

        thrown.expect(MojoExecutionException.class);
        thrown.expectMessage("Database name is missing");

        new InitDataMongoMojo(rootFolder, PORT, null).execute();
    }

    @Test public void
    should_fail_to_execute_instruction_with_error() throws IOException, MojoFailureException, MojoExecutionException {
        DB database = mock(DB.class);
        initFolderWithError();

        CommandResult result = new EmbedMongoDB("myDB").notOkErrorResult("Error while executing instructions from file '" + rootFolderWithError.listFiles()[0].getName());
        given(database.doEval(anyString(), Matchers.<Object>anyVararg())).willReturn(result);

        thrown.expect(MojoExecutionException.class);
        thrown.expectMessage("Error while executing instructions from file '" + rootFolderWithError.listFiles()[0].getName());

        new InitDataMongoMojoForTest(rootFolderWithError, PORT, "myDB", database).execute();
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
        BufferedWriter reader = null;
        try {
            reader = new BufferedWriter(new FileWriter(instructionsFile));
            reader.write("db.unknownInstruction();\n");
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        rootFolderWithError = instructionsFile.getParentFile();
        rootFolderWithError.mkdir();
    }

    static class InitDataMongoMojoForTest extends InitDataMongoMojo {

        private final DB database;

        public InitDataMongoMojoForTest(File dataFolder, int port, String databaseName) throws UnknownHostException {
            this(dataFolder, port, databaseName, new EmbedMongoDB("myDB"));
        }

        public InitDataMongoMojoForTest(File dataFolder, int port, String databaseName, DB database) {
            super(dataFolder, port, databaseName);
            this.database = database;
        }

        @Override
        DB connectToMongoAndGetDatabase() throws MojoExecutionException {
            return database;
        }
    }
}
