/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dubbo.common.utils;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class ConcurrentHashMapUtilsTest {

    @Test
    public void testComputeIfAbsent() {
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
        String ifAbsent = ConcurrentHashMapUtils.computeIfAbsent(map, "mxsm", k -> "mxsm");
        assertEquals("mxsm", ifAbsent);
        ifAbsent = ConcurrentHashMapUtils.computeIfAbsent(map, "mxsm", k -> "mxsm1");
        assertEquals("mxsm", ifAbsent);
        map.remove("mxsm");
        ifAbsent = ConcurrentHashMapUtils.computeIfAbsent(map, "mxsm", k -> "mxsm1");
        assertEquals("mxsm1", ifAbsent);
    }

    @Test
    public void issue11986ForJava8Test(){
        // https://github.com/apache/dubbo/issues/11986
        final ConcurrentHashMap<String,Integer> map=new ConcurrentHashMap<>();
        // // map.computeIfAbsent("AaAa", key->map.computeIfAbsent("BBBB",key2->42));
        if (JRE.JAVA_8.isCurrentVersion()) {
            // JDK8下，由于循环调用bug，会造成执行computeIfAbsent死循环问题
            // ConcurrentHashMapUtils.computeIfAbsent用于解决此问题，保证正常执行
            ConcurrentHashMapUtils.computeIfAbsent(map, "AaAa", key->map.computeIfAbsent("BBBB",key2->42));
            assertEquals(2, map.size());
            assertEquals(Integer.valueOf(42), map.get("AaAa"));
            assertEquals(Integer.valueOf(42), map.get("BBBB"));
        }
    }

    @Test
    public void issue11986ForJava17Test(){
        // https://github.com/apache/dubbo/issues/11986
        final ConcurrentHashMap<String,Integer> map=new ConcurrentHashMap<>();
        if (JRE.JAVA_11.isCurrentVersion() || JRE.JAVA_17.isCurrentVersion()) {
            // JDK9+解决了JDK-8161372这个，不过，遇到循环调用直接抛出IllegalStateException异常
            assertThrows(IllegalStateException.class, ()->{
                ConcurrentHashMapUtils.computeIfAbsent(map, "AaAa", key->map.computeIfAbsent("BBBB",key2->42));
            });
        }
    }
}
