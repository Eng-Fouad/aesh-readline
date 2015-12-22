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
package org.jboss.aesh.readline.history;

import org.jboss.aesh.readline.editing.EditMode;
import org.jboss.aesh.readline.editing.EditModeBuilder;
import org.jboss.aesh.terminal.Key;
import org.jboss.aesh.tty.TestConnection;
import org.jboss.aesh.util.Config;
import org.jboss.aesh.util.FileAccessPermission;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class HistoryTest {

    @Test
    public void testHistory() throws Exception {

        TestConnection term = new TestConnection(EditModeBuilder.builder(EditMode.Mode.EMACS).create());
        term.read("1234"+Config.getLineSeparator());
        term.readline();
        term.read("567"+Config.getLineSeparator());
        term.readline();
        term.read(Key.UP);
        term.read(Key.UP);
        term.read(Key.ENTER);
        term.assertLine("1234");
        term.readline();
        term.read(Key.UP);
        term.read(Key.UP);
        term.read(Key.ENTER);
        term.assertLine("567");
    }

    @Test
    public void testSearch() {
        History history = new InMemoryHistory(20);
        history.push("foo1");
        history.push("foo2");
        history.push("foo3");

        history.setSearchDirection(SearchDirection.REVERSE);
        assertEquals("foo3",history.search("foo") );
        assertEquals("foo2",history.search("foo") );
        assertEquals("foo1",history.search("foo") );

        history.setSearchDirection(SearchDirection.FORWARD);
        assertEquals("foo1",history.search("foo") );
        assertEquals("foo2",history.search("foo") );
        assertEquals("foo3",history.search("foo") );

        history.setSearchDirection(SearchDirection.REVERSE);
        assertEquals("foo3",history.search("foo") );
        assertEquals("foo2",history.search("foo") );
        assertEquals("foo1",history.search("foo") );

        history.setSearchDirection(SearchDirection.REVERSE);

        assertEquals("foo3",history.search("foo") );
        assertEquals("foo2",history.search("foo") );
        assertEquals("foo1",history.search("foo") );
        assertEquals("foo3",history.search("foo") );
    }

    @Test
    public void testSearchAndFetch() {
        History history = new InMemoryHistory(20);
        history.push("foo1");
        history.push("foo2");
        history.push("foo3");

        history.setSearchDirection(SearchDirection.REVERSE);
        assertEquals("foo3",history.search("foo") );
        assertEquals("foo2", history.getPreviousFetch());
    }

    @Test
    public void testHistorySize() {
        History history = new InMemoryHistory(20);

        for(int i=0; i < 25; i++)
            history.push(String.valueOf(i));


        assertEquals(20, history.size());
        assertEquals("24", history.getPreviousFetch());
    }

    @Test
    public void testClear() {
        History history = new InMemoryHistory(10);
        history.push("1");
        history.push("2");

        assertEquals("2", history.getPreviousFetch());
        history.clear();
        assertEquals(null, history.getPreviousFetch());
    }

    @Test
    public void testDupes() {
        History history = new InMemoryHistory(10);
        history.push("1");
        history.push("2");
        history.push("3");
        history.push("1");
        history.push("1");
        assertEquals("1", history.getPreviousFetch());
        assertEquals("3", history.getPreviousFetch());
        assertEquals("1", history.getNextFetch());
        assertEquals("3", history.getPreviousFetch());
        assertEquals("2", history.getPreviousFetch());
        assertEquals("1", history.getPreviousFetch());
        assertEquals(4, history.getAll().size());
    }

    @Test
    public void testFileHistoryPermission() throws IOException{
        File historyFile = new File(System.getProperty("java.io.tmpdir"), "aesh-history-file.test.1");
        historyFile.deleteOnExit();
        int maxSize = 10;
        FileAccessPermission perm = new FileAccessPermission();
        perm.setExecutable(false);
        perm.setExecutableOwnerOnly(false);
        perm.setReadable(true);
        perm.setReadableOwnerOnly(true);
        perm.setWritable(true);
        perm.setWritableOwnerOnly(true);
        History history = new FileHistory(historyFile, maxSize, perm, false);
        history.push("1");
        history.stop(); // it will write history to local file
        assertTrue(historyFile.canRead());
        assertFalse(historyFile.canExecute());
        assertTrue(historyFile.canWrite());

        historyFile = new File(System.getProperty("java.io.tmpdir"), "aesh-history-file.test.2");
        historyFile.deleteOnExit();
        perm = new FileAccessPermission();
        perm.setExecutable(true);
        perm.setExecutableOwnerOnly(true);
        perm.setReadable(false);
        perm.setReadableOwnerOnly(true);
        perm.setWritable(true);
        perm.setWritableOwnerOnly(true);
        history = new FileHistory(historyFile, maxSize, perm, false);
        history.push("1");
        history.stop(); // it will write history to local file
        assertFalse(historyFile.canRead());
        assertTrue(historyFile.canExecute());
        assertTrue(historyFile.canWrite());

        historyFile = new File(System.getProperty("java.io.tmpdir"), "aesh-history-file.test.3");
        historyFile.deleteOnExit();
        perm = new FileAccessPermission();
        perm.setExecutable(false);
        perm.setExecutableOwnerOnly(true);
        perm.setReadable(false);
        perm.setReadableOwnerOnly(true);
        perm.setWritable(false);
        perm.setWritableOwnerOnly(true);
        history = new FileHistory(historyFile, maxSize, perm, false);
        history.push("1");
        history.stop(); // it will write history to local file
        assertFalse(historyFile.canRead());
        assertFalse(historyFile.canExecute());
        assertFalse(historyFile.canWrite());
    }

    @Test
    public void testPrevHistory() {
        History history = new InMemoryHistory(20);
        history.push("foo1");
        history.push("foo2");
        history.push("foo3");

        assertEquals("foo3", history.getPreviousFetch());
        history.push("foo3");
        assertEquals("foo3", history.getPreviousFetch());

    }
}
