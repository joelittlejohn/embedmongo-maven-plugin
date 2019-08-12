/**
 * Copyright © 2012 Joe Littlejohn
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.joelittlejohn.embedmongo;

import de.flapdoodle.embed.mongo.distribution.Feature;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class FeaturesTest {

    @Rule
    public MojoRule rule = new MojoRule();

    private List<String> GOAL_LIST = Arrays.asList("start", "mongo-scripts", "mongo-import", "stop");

    @Test
    public void testFeaturesMissing() throws Exception {
        File pom = new File("src/test/resources/features/featuresMissing.xml");

        for (String goal : GOAL_LIST) {
            AbstractEmbeddedMongoMojo mojo = (AbstractEmbeddedMongoMojo) rule.lookupMojo(goal, pom);
            assertEquals("2.2.0", mojo.getVersion().asInDownloadPath());
        }
    }

    @Test
    public void testFeaturesList() throws Exception {
        File pom = new File("src/test/resources/features/featuresEnabled.xml");

        for (String goal : GOAL_LIST) {
            AbstractEmbeddedMongoMojo mojo = (AbstractEmbeddedMongoMojo) rule.lookupMojo(goal, pom);
            assertFalse(mojo.isSkip());
            assertEquals(EnumSet.of(Feature.ONLY_WITH_SSL, Feature.ONLY_WINDOWS_2008_SERVER, Feature.NO_HTTP_INTERFACE_ARG), mojo.getVersion().getFeatures());
        }
    }
}