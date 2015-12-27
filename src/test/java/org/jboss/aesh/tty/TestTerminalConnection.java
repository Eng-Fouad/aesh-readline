/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aesh.tty;

import org.jboss.aesh.readline.TestTerminal;
import org.jboss.aesh.tty.terminal.TerminalConnection;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class TestTerminalConnection {

    @Test
    public void testRead() throws IOException {
        PipedOutputStream outputStream = new PipedOutputStream();
        PipedInputStream pipedInputStream = new PipedInputStream(outputStream);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        TerminalConnection connection = new TerminalConnection(pipedInputStream, byteArrayOutputStream);

        final ArrayList<int[]> result = new ArrayList<>();
        connection.setStdinHandler(result::add);

        outputStream.write(("FOO").getBytes());
        outputStream.flush();
        outputStream.close();
        connection.startBlockingReader();

        assertEquals(Arrays.toString(result.get(0)), Arrays.toString( new int[] {70,79,79}));
    }

    @Test
    public void testTestConnection() {
        TestTerminal testConnection = new TestTerminal();
        testConnection.read( read -> {
            assertEquals("foo", read);
        });

        testConnection.write("foo\n");
        testConnection.close();
        testConnection.start();
    }

    @Test
    public void testConnection() {
        TestConnection test = new TestConnection();
        test.read("foo");
        test.assertBuffer("foo");
        test.assertLine(null);
        test.read("\n");
        test.assertLine("foo");
    }
}