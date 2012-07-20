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
package com.github.joelittlejohn.embedmongo.log;

import static de.flapdoodle.embedmongo.io.Processors.*;
import de.flapdoodle.embedmongo.config.MongodProcessOutputConfig;
import de.flapdoodle.embedmongo.io.NamedOutputStreamProcessor;
import de.flapdoodle.embedmongo.io.Processors;

public class Loggers {

    public enum LoggingStyle {
        FILE, CONSOLE, NONE
    }

    public static MongodProcessOutputConfig file() {
        FileOutputStreamProcessor file = new FileOutputStreamProcessor();

        return new MongodProcessOutputConfig(
                new NamedOutputStreamProcessor("[mongod output]", file),
                new NamedOutputStreamProcessor("[mongod error]", file), file);
    }

    public static MongodProcessOutputConfig console() {

        return new MongodProcessOutputConfig(
                namedConsole("[mongod output]"),
                namedConsole("[mongod error]"), Processors.console());
    }

    public static MongodProcessOutputConfig none() {

        NoopStreamProcessor noop = new NoopStreamProcessor();
        return new MongodProcessOutputConfig(noop, noop, noop);
    }
}
