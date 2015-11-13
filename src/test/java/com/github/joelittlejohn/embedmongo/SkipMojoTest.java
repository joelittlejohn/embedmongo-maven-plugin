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

import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SkipMojoTest {

    @Rule
    public MojoRule rule = new MojoRule();

    private List<String> GOAL_LIST = Arrays.asList("start", "mongo-scripts", "mongo-import", "stop");

    @Test
    public void testSkipEnabled() throws Exception {
        File pom = new File("src/test/resources/skipmojo/skipEnabled.xml");

        for (String goal : GOAL_LIST) {
            AbstractEmbeddedMongoMojo mojo = (AbstractEmbeddedMongoMojo) rule.lookupMojo(goal, pom);
            assertTrue(mojo.isSkip());
        }
    }

    @Test
    public void testSkipDisabled() throws Exception {
        File pom = new File("src/test/resources/skipmojo/skipDisabled.xml");

        for (String goal : GOAL_LIST) {
            AbstractEmbeddedMongoMojo mojo = (AbstractEmbeddedMongoMojo) rule.lookupMojo(goal, pom);
            assertFalse(mojo.isSkip());
        }
    }
}