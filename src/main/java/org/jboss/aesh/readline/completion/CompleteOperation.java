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
package org.jboss.aesh.readline.completion;

import org.jboss.aesh.parser.Parser;
import org.jboss.aesh.terminal.formatting.TerminalString;

import java.util.ArrayList;
import java.util.List;

/**
 * A payload object to store completion data
 *
 * @author Ståle W. Pedersen <stale.pedersen@jboss.org>
 */
public class CompleteOperation {
    private String buffer;
    private int cursor;
    private int offset;
    private List<TerminalString> completionCandidates;
    private boolean trimmed = false;
    private boolean ignoreStartsWith = false;
    private String nonTrimmedBuffer;
    private boolean ignoreNonEscapedSpace = false;

    private char separator = ' ';
    private boolean appendSeparator = true;
    private boolean ignoreOffset = false;

    public CompleteOperation(String buffer, int cursor) {
        setCursor(cursor);
        setSeparator(' ');
        doAppendSeparator(true);
        completionCandidates = new ArrayList<>();
        setBuffer(buffer);
    }

    public String getBuffer() {
        return buffer;
    }

    private void setBuffer(String buffer) {
        if(buffer != null && buffer.startsWith(" ")) {
            trimmed = true;
            this.buffer = Parser.trimInFront(buffer);
            nonTrimmedBuffer = buffer;
            setCursor(cursor - getTrimmedSize());
        }
        else
            this.buffer = buffer;
    }

    public boolean isTrimmed() {
        return trimmed;
    }

    public int getTrimmedSize() {
        return nonTrimmedBuffer.length() - buffer.length();
    }

    public String getNonTrimmedBuffer() {
        return nonTrimmedBuffer;
    }

    public int getCursor() {
        return cursor;
    }

    private void setCursor(int cursor) {
        if(cursor < 0)
            this.cursor = 0;
        else
            this.cursor = cursor;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setIgnoreOffset(boolean ignoreOffset) {
        this.ignoreOffset = ignoreOffset;
    }

    public boolean doIgnoreOffset() {
        return ignoreOffset;
    }


    /**
     * Get the separator character, by default its space
     *
     * @return separator
     */
    public char getSeparator() {
        return separator;
    }

    /**
     * By default the separator is one space char, but
     * it can be overridden here.
     *
     * @param separator separator
     */
    public void setSeparator(char separator) {
        this.separator = separator;
    }

    /**
     * Do this completion allow for appending a separator
     * after completion? By default this is true.
     *
     * @return appendSeparator
     */
    public boolean hasAppendSeparator() {
        return appendSeparator;
    }

    /**
     * Set if this CompletionOperation would allow an separator to
     * be appended. By default this is true.
     *
     * @param appendSeparator appendSeparator
     */
    public void doAppendSeparator(boolean appendSeparator) {
        this.appendSeparator = appendSeparator;
    }

    public List<TerminalString> getCompletionCandidates() {
        return completionCandidates;
    }

    public void setCompletionCandidates(List<String> completionCandidates) {
        addCompletionCandidates(completionCandidates);
    }

    public void setCompletionCandidatesTerminalString(List<TerminalString> completionCandidates) {
        this.completionCandidates = completionCandidates;
    }

    public void addCompletionCandidate(TerminalString completionCandidate) {
        this.completionCandidates.add(completionCandidate);
    }

    public void addCompletionCandidate(String completionCandidate) {
        addStringCandidate(completionCandidate);
    }

    public void addCompletionCandidates(List<String> completionCandidates) {
        addStringCandidates(completionCandidates);
    }

    public void addCompletionCandidatesTerminalString(List<TerminalString> completionCandidates) {
        this.completionCandidates.addAll(completionCandidates);
    }

     public void removeEscapedSpacesFromCompletionCandidates() {
        Parser.switchEscapedSpacesToSpacesInTerminalStringList(getCompletionCandidates());
    }

    private void addStringCandidate(String completionCandidate) {
        this.completionCandidates.add(new TerminalString(completionCandidate, true));
    }

    private void addStringCandidates(List<String> completionCandidates) {
        for(String s : completionCandidates)
            addStringCandidate(s);
    }

    public List<String> getFormattedCompletionCandidates() {
        List<String> fixedCandidates = new ArrayList<String>(completionCandidates.size());
        for(TerminalString c : completionCandidates) {
            if(!ignoreOffset && offset < cursor) {
                int pos = cursor - offset;
                if(c.getCharacters().length() >= pos)
                    fixedCandidates.add(c.getCharacters().substring(pos));
                else
                    fixedCandidates.add("");
            }
            else {
                fixedCandidates.add(c.getCharacters());
            }
        }
        return fixedCandidates;
    }

    public List<TerminalString> getFormattedCompletionCandidatesTerminalString() {
        List<TerminalString> fixedCandidates = new ArrayList<>(completionCandidates.size());
        for(TerminalString c : completionCandidates) {
            if(!ignoreOffset && offset < cursor) {
                int pos = cursor - offset;
                if(c.getCharacters().length() >= pos) {
                    c.setCharacters(c.getCharacters().substring(pos));
                    fixedCandidates.add(c);
                }
                else
                    fixedCandidates.add(new TerminalString("", true));
            }
            else {
                fixedCandidates.add(c);
            }
        }
        return fixedCandidates;
    }

    public String getFormattedCompletion(String completion) {
        if(offset < cursor) {
            int pos = cursor - offset;
            if(completion.length() > pos)
                return completion.substring(pos);
            else
                return "";
        }
        else
            return completion;
    }

    public boolean isIgnoreStartsWith() {
        return ignoreStartsWith;
    }

    public void setIgnoreStartsWith(boolean ignoreStartsWith) {
        this.ignoreStartsWith = ignoreStartsWith;
    }

    public boolean doIgnoreNonEscapedSpace() {
        return ignoreNonEscapedSpace;
    }

    public void setIgnoreNonEscapedSpace(boolean ignoreNonEscapedSpace) {
        this.ignoreNonEscapedSpace = ignoreNonEscapedSpace;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Buffer: ").append(buffer)
                .append(", Cursor:").append(cursor)
                .append(", Offset:").append(offset)
                .append(", IgnoreOffset:").append(ignoreOffset)
                .append(", Append separator: ").append(appendSeparator)
                .append(", Candidates:").append(completionCandidates);

        return sb.toString();
    }

}
