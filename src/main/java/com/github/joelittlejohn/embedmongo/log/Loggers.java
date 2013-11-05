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

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.config.MongodProcessOutputConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.io.NamedOutputStreamProcessor;

public class Loggers {

    public enum LoggingStyle {
        FILE, CONSOLE, NONE
    }

    public static ProcessOutput file(String logFile, String encoding) {
        FileOutputStreamProcessor file = new FileOutputStreamProcessor(logFile, encoding);

        return new ProcessOutput(
                new NamedOutputStreamProcessor("[mongod output]", file),
                new NamedOutputStreamProcessor("[mongod error]", file),
                new NamedOutputStreamProcessor("[mongod commands]", file));
    }

    public static ProcessOutput console() {
        return MongodProcessOutputConfig.getDefaultInstance(Command.MongoD);
    }

    public static ProcessOutput none() {
        NoopStreamProcessor noop = new NoopStreamProcessor();
        return new ProcessOutput(noop, noop, noop);
    }
}
