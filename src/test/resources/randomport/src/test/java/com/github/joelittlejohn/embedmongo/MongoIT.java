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

import com.github.joelittlejohn.embedmongo.port.PortHelper;
import org.junit.After;
import org.junit.Test;

import java.net.Socket;

public class MongoIT {

    private Socket mongoSocket;

    @Test
    public void testConnectMongo() throws Exception {
        mongoSocket = new Socket("127.0.0.1", PortHelper.getMongoPort("embedmongo-maven-plugin-random-port-test"));
    }

    @After
    public void tearDown() throws Exception {
        if (mongoSocket != null) {
            mongoSocket.close();
        }
    }
}
