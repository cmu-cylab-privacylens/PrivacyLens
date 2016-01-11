/*
 * COPYRIGHT_BOILERPLATE
 * Copyright (c) 2013 Carnegie Mellon University
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

package edu.cmu.ece.privacylens;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import edu.cmu.ece.privacylens.Util;

/**
 * I'm using a comparison to the output hash, which has to be regenerated whenever the ToggleBean generates different
 * output. I would like to use an html snippet validator. Where is one?
 * 
 */
public class ToggleBeanTest {
    /** Class logger. */
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final boolean setupTest = false;

    private ToggleBean buildBase() {
        final ToggleBean bean = new ToggleBean();
        bean.setExplanation("<b>Explanation text</b>");
        bean.setExplanationIcon("explanation icon");
        bean.setTextDiv("attributeReleaseAttribute");
        bean.setImageDiv("attributeReleaseControl");
        bean.setParameter("parameter");
        bean.setText("<b>text</b>");
        return bean;
    }

    /** Test whether a toggle set to true works */
    @Test
    public void testTrue() {
        final ToggleBean bean = buildBase();
        bean.setImageFalse("false image");
        bean.setImageTrue("true image");
        bean.setValue(true);
        bean.setImmutable(false);
        Assert.assertTrue(bean.validate());

        if (setupTest) {
            logger.error(bean.getHtml());
        } else {
            Assert.assertEquals("b971f6ef00c7cbc9ebb1ef4c4926068bf3cad2e08b27898c3aad0a3d58d8ba84",
                    Util.hash(bean.getHtml()));
        }
    }

    /** Test whether a toggle set to false works */
    @Test
    public void testFalse() {
        final ToggleBean bean = buildBase();
        bean.setImageFalse("false image");
        bean.setImageTrue("true image");
        bean.setValue(false);
        bean.setImmutable(false);
        Assert.assertTrue(bean.validate());

        if (setupTest) {
            logger.error(Util.hash(bean.getHtml()));
        } else {
            Assert.assertEquals("098357846f458fd7cbb61a8bfd55373f95a5f068879dd7d4c0dc4a16b53c0d85",
                    Util.hash(bean.getHtml()));
        }
    }

    /** Test whether an immutable toggle set to true works. */
    @Test
    public void testImmuTrue() {
        final ToggleBean bean = buildBase();
        bean.setImageTrue("true image");
        bean.setValue(true);
        bean.setImmutable(true);
        Assert.assertTrue(bean.validate());

        if (setupTest) {
            logger.error(Util.hash(bean.getHtml()));
        } else {
            Assert.assertEquals("90f3f10f141debbcfdbd84dd5a2e9079be33d2ced7a0c4b8b0f8ae7a4dea1de7",
                    Util.hash(bean.getHtml()));
        }
    }

    /** Test whether an immutable toggle set to false works. */
    @Test
    public void testImmuFalse() {
        final ToggleBean bean = buildBase();
        bean.setImageFalse("false image");
        bean.setValue(false);
        bean.setImmutable(true);
        Assert.assertTrue(bean.validate());

        if (setupTest) {
            logger.error(Util.hash(bean.getHtml()));
        } else {

            Assert.assertEquals("b50dbcad64d10edc6b194193a7d85d7121215d2e34bf7b421b3d7909e62299d5",
                    Util.hash(bean.getHtml()));
        }
    }
}
