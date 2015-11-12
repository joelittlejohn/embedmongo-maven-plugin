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

import de.flapdoodle.embed.process.runtime.Network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.UnknownHostException;

public final class NetworkUtils {

    private NetworkUtils() {
    }

    public static int allocateRandomPort() {
        try {
            ServerSocket server = new ServerSocket(0);
            int port = server.getLocalPort();
            server.close();
            return port;
        } catch (IOException e) {
            throw new RuntimeException("Failed to acquire a random free port", e);
        }
    }

    public static boolean localhostIsIPv6() {
        try {
            return Network.localhostIsIPv6();
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
