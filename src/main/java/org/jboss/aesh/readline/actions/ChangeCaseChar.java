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

package org.jboss.aesh.readline.actions;

import org.jboss.aesh.readline.InputProcessor;
import org.jboss.aesh.readline.Action;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class ChangeCaseChar implements Action {
    @Override
    public String name() {
        return "change-case-char";
    }

    @Override
    public void apply(InputProcessor inputProcessor) {
        if(inputProcessor.getBuffer().getBuffer().getLine().length() >=
                inputProcessor.getBuffer().getBuffer().getMultiCursor()) {
            inputProcessor.getBuffer().addActionToUndoStack();
            char c = inputProcessor.getBuffer().getBuffer().getLine().charAt(inputProcessor.getBuffer().getBuffer().getMultiCursor());
            if(Character.isUpperCase(c))
                inputProcessor.getBuffer().getBuffer().replaceChar(Character.toLowerCase(c),
                        inputProcessor.getBuffer().getBuffer().getMultiCursor());
            else
                inputProcessor.getBuffer().getBuffer().replaceChar(Character.toUpperCase(c),
                        inputProcessor.getBuffer().getBuffer().getMultiCursor());
            inputProcessor.getBuffer().drawLine();
            inputProcessor.getBuffer().moveCursor(1);
        }

    }
}
