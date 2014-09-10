/*
 * COPYRIGHT_BOILERPLATE
 * Copyright (c) 2014 Carnegie Mellon University
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of SWITCH nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SWITCH BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package edu.cmu.ece.PrivacyLens;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * This class should test the functions in the Util class
 */
public class UtilTest {
    /** Class logger. */
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Test whether the hash function works */
    @Test
    public void testHash() {
        // the hash function does sha256, but I guess I don't care about implementation
        // test if two things hash to same value
        logger.trace("Equal strings hash to same value");
        String hash1 = Util.hash("foobar");
        String hash2 = Util.hash("foobar");
        Assert.assertEquals(hash1, hash2);

        // test if different things hash to different value
        logger.trace("Unequal strings hash to different value");
        hash1 = Util.hash("foobar");
        hash2 = Util.hash("barfoo");
        Assert.assertNotEquals(hash1, hash2);

        // test if hash length > 5 (arbitrary)
        logger.trace("Hash is of sufficient length to avoid collisions");
        hash1 = Util.hash("baz");
        final int length = hash1.length();
        Assert.assertTrue(length > 5);
    }

    /** Test whether stringToList function works */
    @Test
    public void testStringToList() {
        // test whether empty string converts to empty list
        logger.trace("Empty string handled?");
        String string = "";
        List<String> list = Util.stringToList(string);
        Assert.assertEquals(list.size(), 0);

        // test whether single space converts to empty list
        logger.trace("Single space handled?");
        string = " ";
        list = Util.stringToList(string);
        Assert.assertEquals(list.size(), 0);

        // test whether single item surrounded by spaces converts to list of size 1
        logger.trace("Item surrounded by spaces handled?");
        string = " foo ";
        list = Util.stringToList(string);
        Assert.assertEquals(list.size(), 1);

        // test whether two item string converts to list of size 2
        logger.trace("Two item string handled?");
        string = " foo bar ";
        list = Util.stringToList(string);
        Assert.assertEquals(list.size(), 2);
    }

    /** Test whether getRandomRange works */
    @Test
    public void testGetRandomRange() {
        // XXX what should be tested?
        // bad values to the range?
        // min == max?
        Assert.assertTrue(true);
    }

    /** Test whether listToString works */
    @Test
    public void testListToString() {
        // test whether empty list converts to some string
        logger.trace("Empty list handled?");
        String string = "";
        List<String> list = Util.stringToList(string);
        String string2 = Util.listToString(list);
        Assert.assertTrue(string2.length() > 0);

        // test whether single element list converts to the first element
        logger.trace("Single element list handled?");
        string = "foo";
        list = Util.stringToList(string);
        string2 = Util.listToString(list);
        Assert.assertTrue(string2.equals(string));

        // test whether two element list converts to some string
        logger.trace("Two element list handled?");
        string = "foo bar";
        list = Util.stringToList(string);
        string2 = Util.listToString(list);
        final int twoElementLength = string2.length();
        Assert.assertTrue(twoElementLength > 0);

        // test whether three element list converts to a string longer than the two element list
        logger.trace("Three element list handled?");
        string = "foo bar baz";
        list = Util.stringToList(string);
        string2 = Util.listToString(list);
        final int threeElementLength = string2.length();
        Assert.assertTrue(threeElementLength > twoElementLength);
    }
}
