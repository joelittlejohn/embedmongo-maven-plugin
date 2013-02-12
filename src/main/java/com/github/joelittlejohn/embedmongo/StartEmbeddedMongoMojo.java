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
import java.util.concurrent.TimeUnit;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.github.joelittlejohn.embedmongo.log.Loggers;
import com.github.joelittlejohn.embedmongo.log.Loggers.LoggingStyle;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.RuntimeConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.distribution.GenericVersion;
import de.flapdoodle.embed.process.distribution.IVersion;
import de.flapdoodle.embed.process.exceptions.DistributionException;
import de.flapdoodle.embed.process.runtime.Network;

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
     * An IP address for the MongoDB instance to be bound to during its execution.
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

    @Override
    @SuppressWarnings("unchecked")
    public void execute() throws MojoExecutionException, MojoFailureException {
    	    	
        if (this.proxyHost != null && this.proxyHost.length() > 0) {
            this.addProxySelector();
        }

        MongodExecutable executable;
        try {
            RuntimeConfig config = new RuntimeConfig();
            config.setProcessOutput(getOutputConfig());

            executable = MongodStarter.getInstance(config).prepare(new MongodConfig(getVersion(), bindIp, port, Network.localhostIsIPv6(), getDataDirectory(), null, 0));
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

    private ProcessOutput getOutputConfig() throws MojoFailureException {

        LoggingStyle loggingStyle = LoggingStyle.valueOf(logging.toUpperCase());

        switch (loggingStyle) {
            case CONSOLE:
                return Loggers.console();
            case FILE:
                return Loggers.file();
            case NONE:
                return Loggers.none();
            default:
                throw new MojoFailureException("Unexpected logging style encountered: \"" + logging + "\" -> " + loggingStyle);
        }

    }

    private void addProxySelector() {
    	
    	// Add authenticator with proxyUser and proxyPassword
        if(proxyUser != null && proxyPassword != null) {
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

    private IVersion getVersion() throws MojoExecutionException {
        String versionEnumName = this.version.toUpperCase().replaceAll("\\.", "_");

        if (this.version.charAt(0) != 'V') {
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
