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
package com.github.joelittlejohn.embedmongo.port;

import org.codehaus.plexus.util.StringUtils;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Port helper for common operations with port.
 * <p>
 *     It can :
 *     <ul>
 *         <li>allocate random free port - Inspired by
 * <a href="https://github.com/sonatype/port-allocator-maven-plugin/blob/master/src/main/java/org/sonatype/plugins/portallocator/PortAllocatorMojo.java">
 *     PortAllocatorMojo</a>.</li>
 *         <li>Return port already allocated by invoking start goal of {@code embedmongo-maven-plugin}</li>
 *     </ul>

*  </p>
 */
public class PortHelper {

    public static final String MONGO_PORT_PROPERTY = "embedmongo.port";


    /**
     * Finds port where the mongodb started in previous phase by {@code embedmongo-maven-plugin} should be running.
     * This assumes that embedmongo-maven-plugin' goal start has already been invoked for {@code artifactId}
     * and allocates port properly.
     *
     * @param artifactId maven artifact id of module for which the port set by {@code embedmongo-maven-plugin} should be found.
     * @return port of mongodb for running integration tests of given {@code artifactId}
     */
    public static int getMongoPort(String artifactId) {
        String portProperty;
        if (artifactId == null || artifactId.length() == 0) {
            throw new IllegalArgumentException("maven artifactId has to be specified to find port for proper maven module.");
        } else {
            portProperty  = MONGO_PORT_PROPERTY + "." + artifactId;
        }
        final String port = System.getProperty(portProperty);
        if (StringUtils.isEmpty(port)) {
            throw new IllegalStateException("No mongo port has been set by embedmongo maven plugin via system property '"
                    + portProperty + "'!\n Check plugin configuration and make sure that mongoDb is started before"
                    + " you are trying to access its port.");
        }
        return Integer.valueOf(port);
    }


    /**
     * Allocates new free random port.
     *
     * <p>
     * This implementation allocate random free port and closes it immediately.
     * In some (hopefully) rare situations the port may be occupied in the meantime between calling this method
     * and using returned port on client side.
     * </p>
     * @return random free port
     */
    public int allocateRandomPort() {
        return allocate(0);
    }


    //--------------------------------------------------- HELPER METHODS -----------------------------------------------

    private static int allocate(int portNumber) {
        ServerSocket server;
        try {
            server = new ServerSocket(portNumber);
        } catch (IOException e) {
            throw new PortUnavailableException(portNumber, e);
        }

        portNumber = server.getLocalPort();
        try {
            server.close();
        } catch (IOException e) {
            throw new RuntimeException("Unable to release port " + portNumber, e);
        }
        return portNumber;
    }
}
