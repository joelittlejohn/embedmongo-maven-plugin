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

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Test;

public class NetworkUtilsTest {

    private final ScheduledExecutorService testPooledExecutor = Executors.newScheduledThreadPool(20);

    @After
    public void tearDown() throws Exception {
        testPooledExecutor.shutdown();
    }

    /**
     * This test executes method
     * {@link NetworkUtils#allocateRandomPort()}
     * many times concurrently to make sure that port allocation works correctly
     * under stress.
     */
    @Test
    public void testAllocateRandomPort() throws Exception {
        final int testAllocationCount = 10000;
        final CountDownLatch allocationsCounter = new CountDownLatch(testAllocationCount);

        final Runnable allocatePort = new Runnable() {
            @Override
            public void run() {
                int port = -1;
                try {
                    port = NetworkUtils.allocateRandomPort();
                    new ServerSocket(port);
                    // port has been bound successfully
                } catch (IOException e) {
                    throw new RuntimeException("Port " + port + " cannot be bind!");
                } finally {
                    allocationsCounter.countDown();
                }
            }
        };

        final Random randomGenerator = new Random();
        for (int i = 0; i < testAllocationCount; i++) {
            // schedule execution a little to in the future to simulate less predictable environment
            testPooledExecutor.schedule(allocatePort, randomGenerator.nextInt(10), TimeUnit.MILLISECONDS);
        }
        allocationsCounter.await(10, TimeUnit.SECONDS);
    }

}
