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

import static java.util.Collections.*;

import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.github.joelittlejohn.embedmongo.port.PortHelper;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.github.joelittlejohn.embedmongo.log.Loggers;
import com.github.joelittlejohn.embedmongo.log.Loggers.LoggingStyle;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.AbstractMongoConfig.Net;
import de.flapdoodle.embed.mongo.config.AbstractMongoConfig.Storage;
import de.flapdoodle.embed.mongo.config.AbstractMongoConfig.Timeout;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.distribution.GenericVersion;
import de.flapdoodle.embed.process.distribution.IVersion;
import de.flapdoodle.embed.process.exceptions.DistributionException;
import de.flapdoodle.embed.process.runtime.Network;
import org.apache.maven.project.MavenProject;

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
     * Random port should be used for MongoDB instead of the one specified by {@code port}.
     *
     * @parameter expression="${embedmongo.randomPort}" default-value="false"
     * @since 0.1.8
     */
    private boolean randomPort;

    /**
     * The version of MongoDB to run e.g. 2.1.1, 1.6 v1.8.2, V2_0_4,
     * 
     * @parameter expression="${embedmongo.version}" default-value="2.2.1"
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

    /**
     * An IP address for the MongoDB instance to be bound to during its
     * execution.
     * 
     * @parameter expression="${embedmongo.bindIp}"
     * @since 0.1.4
     */
    private String bindIp;

    /**
     * A proxy hostname to be used when downloading MongoDB distributions.
     * 
     * @parameter expression="${embedmongo.proxyHost}"
     * @since 0.1.1
     */
    private String proxyHost;

    /**
     * A proxy port to be used when downloading MongoDB distributions.
     * 
     * @parameter expression="${embedmongo.proxyPort}" default-value="80"
     * @since 0.1.1
     */
    private int proxyPort;

    /**
     * Block immediately and wait until MongoDB is explicitly stopped (eg:
     * {@literal <ctrl-c>}). This option makes this goal similar in spirit to
     * something like jetty:run, useful for interactive debugging.
     * 
     * @parameter expression="${embedmongo.wait}" default-value="false"
     * @since 0.1.2
     */
    private boolean wait;

    /**
     * @parameter expression="${embedmongo.logging}" default-value="console"
     * @since 0.1.3
     */
    private String logging;

    /**
     * @parameter expression="${embedmongo.logFile}"
     * @since 0.1.7
     */
    private String logFile = Loggers.DEFAULT_LOG_FILE_NAME;

    /**
     * @parameter expression="${embedmongo.logFileEncoding}"
     * @since 0.1.7
     */
    private String logFileEncoding = Loggers.DEFAULT_LOG_FILE_ENCODING;

    /**
     * The proxy user to be used when downloading MongoDB
     * 
     * @parameter expression="${embedmongo.proxyUser}"
     * @since 0.1.6
     */
    private String proxyUser;

    /**
     * The proxy password to be used when downloading MondoDB
     * 
     * @parameter expression="${embedmongo.proxyPassword}"
     * @since 0.1.6
     */
    private String proxyPassword;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @readonly
     */
    private MavenProject project;

    /**
     * The Maven Session Object for setting allocated port to session's user properties.
     *
     * @parameter expression="${session}"
     * @readonly
     */
    private MavenSession session;


    @Override
    @SuppressWarnings("unchecked")
    public void execute() throws MojoExecutionException, MojoFailureException {

        if (this.proxyHost != null && this.proxyHost.length() > 0) {
            this.addProxySelector();
        }

        MongodExecutable executable;
        try {

            IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
                    .defaults(Command.MongoD)
                    .processOutput(getOutputConfig())
                    .build();
            if (randomPort) {
                port = new PortHelper().allocateRandomPort();
            }
            savePortToSessionUserProperties();
            MongodConfig mongoConfig = new MongodConfig(getVersion(),
                    new Net(bindIp, port, Network.localhostIsIPv6()),
                    new Storage(getDataDirectory(), null, 0),
                    new Timeout());

            executable = MongodStarter.getInstance(runtimeConfig).prepare(mongoConfig);
        } catch (UnknownHostException e) {
            throw new MojoExecutionException("Unable to determine if localhost is ipv6", e);
        } catch (DistributionException e) {
            throw new MojoExecutionException("Failed to download MongoDB distribution: " + e.withDistribution(), e);
        }

        try {
            MongodProcess mongod = executable.start();

            if (wait) {
                while (true) {
                    try {
                        TimeUnit.MINUTES.sleep(5);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }

            getPluginContext().put(MONGOD_CONTEXT_PROPERTY_NAME, mongod);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to start the mongod", e);
        }
    }

    /**
     * Saves port to the {@link MavenSession#userProperties} to provide client with ability to retrieve port
     * later in integration-test-phase via {@link PortHelper#getMongoPort(String)} method.
     * Port is saved as a property {@code MONGO_PORT_PROPERTY + "." + artifactId} where {@code artifactId}
     * is id of project artifact where the integration tests are run.
     * The {@code artifactId} suffix is necessary because concurrent executions of {@code embedmongo-maven-plugin}
     * cannot share the same property.
     * <p>
     * {@code userProperties} seems to be the only way how to propagate property to the forked JVM run
     * started by failsafe plugin.
     * </p>
     */
    private void savePortToSessionUserProperties() {
        final String portKey = PortHelper.MONGO_PORT_PROPERTY + "." + project.getArtifactId();
        final String portValue = Integer.toString(port);
        final Properties userProperties = session.getUserProperties();
        userProperties.setProperty(portKey, portValue);
    }

    private ProcessOutput getOutputConfig() throws MojoFailureException {

        LoggingStyle loggingStyle = LoggingStyle.valueOf(logging.toUpperCase());

        switch (loggingStyle) {
            case CONSOLE:
                return Loggers.console();
            case FILE:
                return Loggers.file(logFile, logFileEncoding);
            case NONE:
                return Loggers.none();
            default:
                throw new MojoFailureException("Unexpected logging style encountered: \"" + logging + "\" -> " + loggingStyle);
        }

    }

    private void addProxySelector() {

        // Add authenticator with proxyUser and proxyPassword
        if (proxyUser != null && proxyPassword != null) {
            Authenticator.setDefault(new Authenticator() {
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(proxyUser, proxyPassword.toCharArray());
                }
            });
        }

        final ProxySelector defaultProxySelector = ProxySelector.getDefault();
        ProxySelector.setDefault(new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                if (uri.getHost().equals("fastdl.mongodb.org")) {
                    return singletonList(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
                } else {
                    return defaultProxySelector.select(uri);
                }
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
            }
        });
    }

    private IVersion getVersion() {
        String versionEnumName = this.version.toUpperCase().replaceAll("\\.", "_");

        if (versionEnumName.charAt(0) != 'V') {
            versionEnumName = "V" + versionEnumName;
        }

        try {
            return Version.valueOf(versionEnumName);
        } catch (IllegalArgumentException e) {
            getLog().warn("Unrecognised MongoDB version '" + this.version + "', this might be a new version that we don't yet know about. Attemping download anyway...");
            return new GenericVersion(this.version);
        }

    }

    private String getDataDirectory() {
        if (databaseDirectory != null) {
            return databaseDirectory.getAbsolutePath();
        } else {
            return null;
        }
    }

}
