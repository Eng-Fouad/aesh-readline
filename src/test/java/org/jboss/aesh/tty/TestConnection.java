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

import org.jboss.aesh.parser.Parser;
import org.jboss.aesh.readline.Readline;
import org.jboss.aesh.readline.completion.Completion;
import org.jboss.aesh.readline.editing.EditMode;
import org.jboss.aesh.readline.editing.EditModeBuilder;
import org.jboss.aesh.terminal.Key;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class TestConnection implements Connection {

    private Consumer<Size> sizeHandler;
    private Consumer<Signal> signalHandler;
    private Consumer<int[]> stdinHandler;
    private Consumer<int[]> stdOutHandler;
    private Consumer<Void> closeHandler;

    private StringBuilder bufferBuilder;
    private String out;
    private TestReadline readline;

    public TestConnection() {
        //default emacs mode
        this(EditModeBuilder.builder().create(), null);
    }

    public TestConnection(EditMode editMode) {
        this(editMode, null);
    }

    public TestConnection(List<Completion> completions) {
        this(EditModeBuilder.builder().create(), completions);
    }

    public TestConnection(EditMode editMode, List<Completion> completions) {
        bufferBuilder = new StringBuilder();
        stdOutHandler = ints -> {
           bufferBuilder.append(Parser.stripAwayAnsiCodes(Parser.fromCodePoints(ints)));
        };

        readline = new TestReadline(editMode);
        if(completions != null)
            readline(completions);
        else
            readline();
    }

    public void readline() {
        readline.readline(this, ": ", out -> { this.out = out; } );
    }

    public void readline(List<Completion> completions) {
        readline.readline(this, ": ", out -> { this.out = out; }, completions );
    }

    public String getLine() {
        return out;
    }

    @Override
    public String terminalType() {
        return "fooTerm";
    }

    @Override
    public Size size() {
        return new Size(80,20);
    }

    @Override
    public Consumer<Size> getSizeHandler() {
        return sizeHandler;
    }

    @Override
    public void setSizeHandler(Consumer<Size> handler) {
        this.sizeHandler = handler;

    }

    @Override
    public Consumer<Signal> getSignalHandler() {
        return signalHandler;
    }

    @Override
    public void setSignalHandler(Consumer<Signal> handler) {
        signalHandler = handler;
    }

    @Override
    public Consumer<int[]> getStdinHandler() {
        return stdinHandler;
    }

    @Override
    public void setStdinHandler(Consumer<int[]> handler) {
        stdinHandler = handler;
    }

    @Override
    public Consumer<int[]> stdoutHandler() {
        return stdOutHandler;
    }

    @Override
    public void setCloseHandler(Consumer<Void> closeHandler) {
        this.closeHandler = closeHandler;
    }

    @Override
    public Consumer<Void> getCloseHandler() {
        return closeHandler;
    }

    @Override
    public void close() {
        closeHandler.accept(null);
    }

    public void assertBuffer(String expected) {
        assertEquals(expected, Parser.stripAwayAnsiCodes(readline.getBuffer()));
    }

    public void assertLine(String expected) {
        assertEquals(expected, out);
    }

    public void read(int... data) {
        stdinHandler.accept(data);
    }

    public void read(Key key) {
        stdinHandler.accept(key.getKeyValues());
    }

    public void read(String data) {
        stdinHandler.accept(Parser.toCodePoints(data));
    }

    class TestReadline extends Readline {

        TestReadline() {
            super();
        }

        TestReadline(EditMode editMode) {
            super(editMode);
        }

        public String getBuffer() {
            return getInputProcessor().getBuffer().getBuffer().getMultiLine();
        }

    }
}
