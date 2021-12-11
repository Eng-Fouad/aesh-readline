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
package org.aesh.readline.action.mappings;

import org.aesh.readline.ReadlineFlag;
import org.aesh.terminal.utils.Config;
import org.aesh.readline.util.Parser;
import org.aesh.readline.ConsoleBuffer;
import org.aesh.readline.InputProcessor;
import org.aesh.readline.action.Action;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class Enter implements Action {

    private static final String ENDS_WITH_BACKSLASH = "\\";
    private static final String HASHTAG = "#";

    @Override
    public String name() {
        return "accept-line";
    }

    @Override
    public void accept(InputProcessor inputProcessor) {
        ConsoleBuffer consoleBuffer = inputProcessor.buffer();
        consoleBuffer.undoManager().clear();
        boolean isCurrentLineEnding = true;
        // check flags to see if we should ignore one or both of the quotes
        // (0=ignore both, 1=ignore for double quotes, 2=ignore for single quotes)
        int multilineFlags = inputProcessor.flags().getOrDefault(ReadlineFlag.NO_MULTI_LINE_ON_QUOTE, -1);
        boolean ignoreQuotes = multilineFlags == 0;

        if(!consoleBuffer.buffer().isMasking()) { // don't push to history if masking
            // don't push lines that end with \ to history
            String buffer = consoleBuffer.buffer().asString().trim();
            // lines starting with a hashtag is treated as a comment
            if(buffer.startsWith(HASHTAG) ) {
                consoleBuffer.buffer().reset();
                inputProcessor.buffer().writeOut(Config.CR);
                isCurrentLineEnding = false;
            }
            else if (buffer.endsWith(ENDS_WITH_BACKSLASH) || (!ignoreQuotes && Parser.doesStringContainOpenQuote(buffer, multilineFlags))) {
                consoleBuffer.buffer().setMultiLine(true);
                consoleBuffer.buffer().updateMultiLineBuffer();
                inputProcessor.buffer().writeOut(Config.CR);
                isCurrentLineEnding = false;
            }
            else if( inputProcessor.buffer().history().isEnabled()) {
                inputProcessor.buffer().history().push(consoleBuffer.buffer().multiLine());
            }
        }

        if(isCurrentLineEnding)
            consoleBuffer.moveCursor(consoleBuffer.buffer().length());

        if(isCurrentLineEnding) {
            inputProcessor.setReturnValue(consoleBuffer.buffer().multiLine());
            consoleBuffer.buffer().reset();
        }
        else
            consoleBuffer.drawLine();
    }
}
