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
import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import de.flapdoodle.embedmongo.MongoDBRuntime;
import de.flapdoodle.embedmongo.MongodExecutable;
import de.flapdoodle.embedmongo.MongodProcess;
import de.flapdoodle.embedmongo.config.MongodConfig;
import de.flapdoodle.embedmongo.distribution.Version;
import de.flapdoodle.embedmongo.runtime.Network;
import static java.util.Arrays.*;

/**
 * When invoked, this goal starts an instance of mongo. The required binaries
 * are downloaded if no mongo release is found in <code>~/.embedmongo</code>.
 * 
 * @goal start
 * @phase pre-integration-test
 * @see <a
 *      href="http://github.com/flapdoodle-oss/embedmongo.flapdoodle.de">http://github.com/flapdoodle-oss/embedmongo.flapdoodle.de</a>
 */
public class StartEmbeddedMongoMojo extends AbstractMojo {

    private static final String PACKAGE_NAME = StartEmbeddedMongoMojo.class.getPackage().getName();
    public static final String MONGOD_CONTEXT_PROPERTY_NAME = PACKAGE_NAME + ".mongod";

    /**
     * The port MongoDB should run on.
     * 
     * @parameter expression="${embedmongo.port}" default-value="27017"
     * @since 0.1.0
     */
    private int port;

    /**
     * The version of MongoDB to run e.g. 2.1.1, 1.6 v1.8.2, V2_0_4,
     * 
     * @parameter expression="${embedmongo.version}" default-value="V2_1_1"
     * @since 0.1.0
     */
    private String version;

    /**
     * The location of a directory that will hold the MongoDB data files.
     * 
     * @parameter expression="${embedmongo.databaseDirectory}"
     * @since 0.1.0
     */
    private File databaseDirectory;

    @Override
    @SuppressWarnings("unchecked")
    public void execute() throws MojoExecutionException, MojoFailureException {

        MongodExecutable executable;
        try {
            executable = MongoDBRuntime.getDefaultInstance().prepare(new MongodConfig(getVersion(), port, Network.localhostIsIPv6(), getDataDirectory()));
        } catch (UnknownHostException e) {
            throw new MojoExecutionException("Unable to determine if localhost is ipv6", e);
        }

        try {
            MongodProcess mongod = executable.start();
            addShutdownHook(mongod);

            getPluginContext().put(MONGOD_CONTEXT_PROPERTY_NAME, mongod);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to start the mongod", e);
        }
    }

    private Version getVersion() throws MojoExecutionException {
        String flapdoodleCompatibleVersionString = this.version.toUpperCase().replaceAll("\\.", "_");

        if (this.version.charAt(0) != 'V') {
            flapdoodleCompatibleVersionString = "V" + flapdoodleCompatibleVersionString;
        }

        try {
            return Version.valueOf(flapdoodleCompatibleVersionString);
        } catch (IllegalArgumentException e) {
            throw new MojoExecutionException("Unrecognised MongoDB version: '" + this.version + 
                    "', try one of the following: \n" + asList(Version.class.getEnumConstants()) + "\n", e);
        }

    }

    private String getDataDirectory() {
        if (databaseDirectory != null) {
            return databaseDirectory.getAbsolutePath();
        } else {
            return null;
        }
    }

    /**
     * A final failsafe to shutdown the mongod process at the end of the build,
     * even if the plugin stop goal is not invoked.
     * 
     * @param mongod
     *            the mongo process started by this plugin
     */
    private void addShutdownHook(final MongodProcess mongod) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                mongod.stop();
            }
        });
    }

}
