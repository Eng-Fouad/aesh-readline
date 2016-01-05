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
package org.jboss.aesh.parser;

import org.jboss.aesh.terminal.formatting.TerminalString;
import org.jboss.aesh.util.ANSI;
import org.jboss.aesh.util.Config;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
public class ParserTest {

    @Test
    public void testFindStartsWith() {
        List<String> completionList = new ArrayList<>(3);
        completionList.add("foobar");
        completionList.add("foobaz");
        completionList.add("foobor");
        completionList.add("foob");

        assertEquals("foob", Parser.findStartsWith(completionList));

        completionList.clear();
        completionList.add("foo");
        completionList.add("bar");
        assertEquals("", Parser.findStartsWith(completionList));
    }

    @Test
    public void testfindCurrentWordFromCursor() {
        assertEquals("", Parser.findCurrentWordFromCursor(" ", 1));
        assertEquals("foo", Parser.findCurrentWordFromCursor("foo bar", 3));
        assertEquals("bar", Parser.findCurrentWordFromCursor("foo bar", 6));
        assertEquals("foobar", Parser.findCurrentWordFromCursor("foobar", 6));
        assertEquals("foo", Parser.findCurrentWordFromCursor("foobar", 2));
        assertEquals("", Parser.findCurrentWordFromCursor("ls  ", 3));
        assertEquals("foo", Parser.findCurrentWordFromCursor("ls  foo", 6));
        assertEquals("foo", Parser.findCurrentWordFromCursor("ls  foo bar", 6));
        assertEquals("bar", Parser.findCurrentWordFromCursor("ls  foo bar", 10));
        assertEquals("ba", Parser.findCurrentWordFromCursor("ls  foo bar", 9));
        assertEquals("foo", Parser.findCurrentWordFromCursor("ls foo ", 6));
        assertEquals("o", Parser.findCurrentWordFromCursor("ls o org/jboss/aeshell/Shell.class", 4));
        assertEquals("", Parser.findCurrentWordFromCursor("ls  org/jboss/aeshell/Shell.class", 3));
    }

    @Test
    public void testFindCurrentWordWithEscapedSpaceToCursor() {
        assertEquals("foo bar", Parser.findCurrentWordFromCursor("foo\\ bar", 7));
        assertEquals("foo ba", Parser.findCurrentWordFromCursor("foo\\ bar", 6));
        assertEquals("foo bar", Parser.findCurrentWordFromCursor("ls  foo\\ bar", 11));
    }

    @Test
    public void testFindClosestWholeWordToCursor() {
        assertEquals("foo", Parser.findWordClosestToCursor("ls  foo bar", 6));

        assertEquals("", Parser.findWordClosestToCursor(" ", 1));
        assertEquals("foo", Parser.findWordClosestToCursor("foo bar", 1));
        assertEquals("foo", Parser.findWordClosestToCursor("foo bar", 3));
        assertEquals("foobar", Parser.findWordClosestToCursor("foobar", 6));
        assertEquals("foobar", Parser.findWordClosestToCursor("foobar", 2));
        assertEquals("", Parser.findWordClosestToCursor("ls  ", 3));

        assertEquals("o", Parser.findWordClosestToCursor("ls o org/jboss/aeshell/Shell.class", 4));
        assertEquals("", Parser.findWordClosestToCursor("ls  org/jboss/aeshell/Shell.class", 3));

        assertEquals("foo", Parser.findWordClosestToCursor("foo bar foo", 3));
    }

    @Test
    public void testFindClosestWholeWordToCursorEscapedSpace() {
        assertEquals("foo bar", Parser.findWordClosestToCursor("foo\\ bar", 7));
        assertEquals("foo bar", Parser.findWordClosestToCursor("ls  foo\\ bar", 11));
    }

    @Test
    public void testFindEscapedSpaceWordCloseToEnd() {
        assertEquals("ls\\ foo", Parser.findEscapedSpaceWordCloseToEnd(" ls\\ foo"));
        assertEquals("foo\\ bar", Parser.findEscapedSpaceWordCloseToEnd("ls foo\\ bar"));
        assertEquals("bar", Parser.findEscapedSpaceWordCloseToEnd("ls foo bar"));
        assertEquals("ls\\ foo\\ bar", Parser.findEscapedSpaceWordCloseToEnd("ls\\ foo\\ bar"));
        assertEquals("\\ ls\\ foo\\ bar", Parser.findEscapedSpaceWordCloseToEnd("\\ ls\\ foo\\ bar"));
        assertEquals("ls\\ foo\\ bar", Parser.findEscapedSpaceWordCloseToEnd(" ls\\ foo\\ bar"));
        assertEquals("ls\\ foo\\ bar\\ ", Parser.findEscapedSpaceWordCloseToEnd(" ls\\ foo\\ bar\\ "));
        assertEquals("", Parser.findEscapedSpaceWordCloseToEnd(" ls\\ foo\\ bar\\  "));
    }

    @Test
    public void testFindEscapedSpaceWord() {
        assertTrue(Parser.doWordContainOnlyEscapedSpace("foo\\ bar"));
        assertTrue(Parser.doWordContainOnlyEscapedSpace("foo\\ bar\\ "));
        assertTrue(Parser.doWordContainOnlyEscapedSpace("\\ foo\\ bar\\ "));
        assertFalse(Parser.doWordContainOnlyEscapedSpace(" foo\\ bar\\ "));
        assertFalse(Parser.doWordContainOnlyEscapedSpace("foo bar\\ "));
        assertFalse(Parser.doWordContainOnlyEscapedSpace("foo bar"));
    }

    @Test
    public void testChangeWordWithSpaces() {
        assertEquals("foo bar", Parser.switchEscapedSpacesToSpacesInWord("foo\\ bar"));
        assertEquals(" foo bar", Parser.switchEscapedSpacesToSpacesInWord("\\ foo\\ bar"));
        assertEquals(" foo bar ", Parser.switchEscapedSpacesToSpacesInWord("\\ foo\\ bar\\ "));
        assertEquals(" foo bar", Parser.switchEscapedSpacesToSpacesInWord("\\ foo bar"));

        assertEquals("foo\\ bar", Parser.switchSpacesToEscapedSpacesInWord("foo bar"));
        assertEquals("\\ foo\\ bar", Parser.switchSpacesToEscapedSpacesInWord(" foo bar"));
        assertEquals("\\ foo\\ bar\\ ", Parser.switchSpacesToEscapedSpacesInWord(" foo bar "));
    }

    @Test
    public void testSplitBySizeKeepWords() {
        String words = "foo to bar is how it is i guess";
        List<String> out = Parser.splitBySizeKeepWords(words, 10);
        assertEquals("foo to bar", out.get(0));
        assertEquals("is how it", out.get(1));
        assertEquals("is i guess", out.get(2));

        words = "It is an error to use a backslash prior to any alphabetic";
        out = Parser.splitBySizeKeepWords(words, 20);
        assertEquals("It is an error to", out.get(0));
        assertEquals("use a backslash", out.get(1));
        assertEquals("prior to any", out.get(2));
        assertEquals("alphabetic", out.get(3));
    }

    @Test
    public void testTrim() {
        assertEquals("foo", Parser.trim("  foo "));
        assertEquals("bar foo", Parser.trim("bar foo "));
        assertEquals("bar foo", Parser.trim(" bar foo"));
        assertEquals("\\ foo\\ ", Parser.trim("\\ foo\\  "));
    }

    @Test
    public void testFindFirstWord() {
        assertEquals("foo", Parser.findFirstWord(" foo \\ bar"));
        assertEquals("foo", Parser.findFirstWord(" foo bar baz"));
        assertEquals("foo", Parser.findFirstWord("foo bar baz"));
        assertEquals("foobar", Parser.findFirstWord("foobar baz"));
        assertEquals("foobarbaz", Parser.findFirstWord("foobarbaz"));
    }

    @Test
    public void testTrimInFront() {
        assertEquals("foo ", Parser.trimInFront("  foo "));
        assertEquals("foo", Parser.trimInFront("  foo"));
        assertEquals("foo", Parser.trimInFront("foo"));
    }

    // verify divide by zero fix
    @Test
    public void testFormatDisplayList() {
        List<String> list = new ArrayList<>();
        String s1 = "this is a loooooong string thats longer than the terminal width";
        list.add(s1);

        assertEquals(s1 + "  " + Config.getLineSeparator(), Parser.formatDisplayList(list, 20, 20));
    }

    @Test
    public void testPadLeft() {
        assertEquals(" foo", Parser.padLeft(4, "foo"));
        assertEquals("   foo", Parser.padLeft(6, "foo"));
    }

    @Test
    public void testFindNumberOfSpaces() {
        assertEquals(4, Parser.findNumberOfSpacesInWord("this is a word "));
        assertEquals(4, Parser.findNumberOfSpacesInWord("this is a word !"));
        assertEquals(5, Parser.findNumberOfSpacesInWord(" this is a word !"));
        assertEquals(4, Parser.findNumberOfSpacesInWord(" this is a\\ word !"));
    }

    @Test
    public void testContainsDollar() {
        assertTrue(Parser.containsNonEscapedDollar("foo $bar"));
        assertFalse(Parser.containsNonEscapedDollar("foo bar"));
        assertFalse(Parser.containsNonEscapedDollar("foo \\$bar"));
        assertFalse(Parser.containsNonEscapedDollar("foo \\$bar\\$"));
        assertFalse(Parser.containsNonEscapedDollar("\\$foo \\$bar\\$"));
        assertTrue(Parser.containsNonEscapedDollar("$foo \\$bar\\$"));
        assertTrue(Parser.containsNonEscapedDollar("\\$foo \\$bar$"));
    }

    @Test
    public void testDoesStringContainQuote() {
        assertFalse(Parser.doesStringContainOpenQuote("foo bar is bar"));
        assertFalse(Parser.doesStringContainOpenQuote("\"foo bar is bar is foo is bar\""));
        assertFalse(Parser.doesStringContainOpenQuote("\"foo bar \"is bar is \"foo is bar\""));
        assertFalse(Parser.doesStringContainOpenQuote("\'foo bar \"is bar is \"foo is bar\'"));
        assertTrue(Parser.doesStringContainOpenQuote("\"foo bar is bar is \"foo is bar\""));
        assertFalse(Parser.doesStringContainOpenQuote("\"foo bar is bar is \\\"foo is bar\""));
        assertTrue(Parser.doesStringContainOpenQuote("\"foo bar is bar is \\\"foo is bar\'"));
    }

    @Test
    public void testFormatDisplayCompactListTerminalString() {
        TerminalString terminalLong = new TerminalString("This string is too long to terminal width");

        TerminalString terminalShort1 = new TerminalString("str1");
        TerminalString terminalShort2 = new TerminalString("str2");
        TerminalString terminalShort3 = new TerminalString("str3");
        TerminalString terminalShort4 = new TerminalString("str4");

        TerminalString terminalLonger1 = new TerminalString("longer1");
        TerminalString terminalLonger2 = new TerminalString("longer2");
        TerminalString terminalLonger3 = new TerminalString("longer3");

        assertEquals(
            terminalLong.toString() + Config.getLineSeparator(),
            Parser.formatDisplayCompactListTerminalString(Collections.singletonList(terminalLong), 10));

        assertEquals(
            terminalShort1.toString() + "  " + terminalShort2.toString() + Config.getLineSeparator(),
            Parser.formatDisplayCompactListTerminalString(Arrays.asList(terminalShort1, terminalShort2), 20));

        assertEquals(
            terminalShort1.toString() + Config.getLineSeparator() + terminalShort2.toString() + Config.getLineSeparator(),
            Parser.formatDisplayCompactListTerminalString(Arrays.asList(terminalShort1, terminalShort2), 10));

        assertEquals(
            terminalShort1.toString() + "  " + terminalShort3.toString() + Config.getLineSeparator() + terminalShort2.toString() + Config.getLineSeparator(),
            Parser.formatDisplayCompactListTerminalString(Arrays.asList(terminalShort1, terminalShort2, terminalShort3), 15));

        assertEquals(
            terminalShort1.toString() + "  " + terminalShort3.toString() + Config.getLineSeparator() +
                terminalShort2.toString() + "  " + terminalShort4.toString() + Config.getLineSeparator(),
            Parser.formatDisplayCompactListTerminalString(Arrays.asList(terminalShort1, terminalShort2, terminalShort3, terminalShort4), 15));

        assertEquals(
            terminalLonger1.toString() + "  " + terminalShort1.toString() + Config.getLineSeparator() +
                terminalLonger2.toString() + Config.getLineSeparator() + terminalLonger3.toString() + Config.getLineSeparator(),
            Parser.formatDisplayCompactListTerminalString(
                Arrays.asList(terminalLonger1, terminalLonger2, terminalLonger3, terminalShort1), 15));
    }

    @Test
    public void testStripAwayAnsiPattern() {
        assertEquals("foo", Parser.stripAwayAnsiCodes(ANSI.BLACK_TEXT + "foo"));
        assertEquals("foo", Parser.stripAwayAnsiCodes(ANSI.BOLD + ANSI.CYAN_BG + "foo"));
        assertEquals("foo", Parser.stripAwayAnsiCodes("foo" + ANSI.BOLD + ANSI.CYAN_BG));
        assertEquals("foo", Parser.stripAwayAnsiCodes(ANSI.ALTERNATE_BUFFER + "foo"));
        assertEquals("foo", Parser.stripAwayAnsiCodes(ANSI.CURSOR_ROW + "foo"));
        assertEquals("foo bar", Parser.stripAwayAnsiCodes("foo" + ANSI.RESET + " bar"));
    }

}
