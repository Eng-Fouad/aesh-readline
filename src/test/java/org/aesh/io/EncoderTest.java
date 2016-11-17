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
package org.aesh.io;

import org.junit.Ignore;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
@Ignore
public class EncoderTest {

    public void decodeEndcode(String incoming) {
        Charset charset = Charset.forName("UTF-8");
        //ArrayList<String> decodeResult = new ArrayList<>();
        final ArrayList<int[]> result = new ArrayList<>();
        Decoder decoder = new Decoder(charset, event -> {
            result.add(event);
        });

        final byte[] output = new byte[incoming.getBytes().length];
        Encoder encoder = new Encoder(charset, event -> {
            for(int i=0; i < event.length; i++)
                output[i] = event[i];
        });

        decoder.write(incoming.getBytes());
        encoder.accept(result.get(0));

        assertEquals(incoming, new String(output));
    }

    @Test
    public void testInputs() {
        decodeEndcode("foo");
        decodeEndcode("foo bar!!??");
        decodeEndcode("\r");
    }
}
