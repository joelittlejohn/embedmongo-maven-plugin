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

import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.distribution.Versions;
import de.flapdoodle.embed.process.distribution.IVersion;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Created by pablo on 28/03/15.
 */
public abstract class AbstractEmbeddedMongoMojo extends AbstractMojo {
    @Parameter(property = "embedmongo.skip", defaultValue = "false")
    private boolean skip;

    @Parameter(property = "embedmongo.port", defaultValue = "27017")
    private int port;

    @Parameter(property = "embedmongo.randomPort", defaultValue = "false")
    private boolean randomPort;

    @Parameter(property = "embedmongo.version", defaultValue = "2.2.1")
    private String version;

    @Component
    protected MavenProject project;

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        if(!skip){
            executeEmbeddedMongo();
        }
    }

    // FIXME: This is a copy/paste version of StartEmbeddedMongoMojo
    protected IFeatureAwareVersion getVersion() {
        String versionEnumName = this.version.toUpperCase().replaceAll("\\.", "_");

        if (versionEnumName.charAt(0) != 'V') {
            versionEnumName = "V" + versionEnumName;
        }

        try {
            return Version.valueOf(versionEnumName);
        } catch (IllegalArgumentException e) {
            getLog().warn("Unrecognised MongoDB version '" + this.version + "', this might be a new version that we don't yet know about. Attemping download anyway...");
            return Versions.withFeatures(new IVersion() {
                @Override
                public String asInDownloadPath() {
                    return version;
                }
            });
        }
    }

    protected Integer getPort() {
        String portStr = project.getProperties().getProperty("embedmongo.port");

        if(StringUtils.isNotBlank(portStr)){
            return Integer.valueOf(portStr);
        }else{
            return port;
        }
    }

    public abstract void executeEmbeddedMongo() throws MojoExecutionException, MojoFailureException;
}
