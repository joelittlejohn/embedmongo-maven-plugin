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
package com.mongodb;

import org.bson.BsonDocument;

import java.net.UnknownHostException;
import java.util.Set;

public class EmbedMongoDB extends DB {

    public EmbedMongoDB(String name) throws UnknownHostException {
        super(new EmbedMongoClient(), name);
    }

    public CommandResult notOkErrorResult(String message) {
        CommandResult commandResult = new CommandResult(new BsonDocument(), new ServerAddress("localhost"));
        commandResult.put("errmsg", message);
        commandResult.put("ok", 0);
        return commandResult;
    }

    @Override
    public CommandResult doEval(String code, Object... args) {
        CommandResult commandResult = new CommandResult(new BsonDocument(), new ServerAddress("localhost"));
        commandResult.put("ok", 1.0);
        commandResult.put("retval", "null");
        return commandResult;
    }

    @Override
    protected DBCollection doGetCollection(String name) {
        return null;
    }

    @Override
    public Set<String> getCollectionNames() {
        return null;
    }
}
