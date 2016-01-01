/*
 * Copyright 2015 David Russell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.onetapbeyond.renjin.r.executor;

import org.junit.Test;
import org.junit.BeforeClass;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import java.io.*;
import java.util.*;
import javax.script.*;
import org.renjin.sexp.*;
import org.renjin.eval.Context;

/*
 * RenjinPooledEngineTests
 * 
 * Tests renjin-r-executor support for pooled ScriptEngines,
 * the default runtime behavior for the library.
 */
@RunWith(Parameterized.class)
public class RenjinPooledEngineTests {

    private boolean serializedTasks;
    private boolean serializedResults;

    public RenjinPooledEngineTests(Map<String,Boolean> params) {
        this.serializedTasks = params.get("serializedTasks");
        this.serializedResults = params.get("serializedResults");
    }

    /*
     * JUnit Parameterized Tests
     */
    @Parameters
    public static Collection<Object[]> getTestParameters() {

        /*
         * Single JVM Test, no serialization.
         */
        Map<String, Boolean> t1 = new HashMap<String, Boolean>();
        t1.put("serializedTasks", false);
        t1.put("serializedResults", false);

        /*
         * Distributed JVM Test with RenjinTask serialization only.
         */
        Map<String, Boolean> t2 = new HashMap<String, Boolean>();
        t2.put("serializedTasks", true);
        t2.put("serializedResults", false);

        /*
         * Distributed JVM Test with RenjinResult serialization only.
         */
        Map<String, Boolean> t3 = new HashMap<String, Boolean>();
        t3.put("serializedTasks", false);
        t3.put("serializedResults", true);

        /*
         * Distributed JVM Test with RenjinTask and RenjinResult serialization.
         */
        Map<String, Boolean> t4 = new HashMap<String, Boolean>();
        t4.put("serializedTasks", true);
        t4.put("serializedResults", true);
 
        return Arrays.asList(new Object[][] {
                { t1 }, { t2 }, { t3 }, { t4 }
        });
    }

	@BeforeClass
	public static void setUpClass() {
	}

    @Test public void testBuilder() {
        Renjin builder = Renjin.R();
        assertNotNull(builder);
    }

    @Test(expected=RenjinException.class)
    public void testBuilderWithoutCode() throws RenjinException {
        Renjin builder = Renjin.R();
        // Missing required code(), expect RenjinException.
        RenjinTask rTask = builder.build();
        assertNotNull(builder);
        assertNull(rTask);
    }


    @Test
    public void testBuilderWithCode() throws RenjinException {
        Renjin builder = Renjin.R(serializedTasks, serializedResults);
        RenjinTask rTask = builder.code("x<-1").build();
        RenjinResult rResult = rTask.execute();
        SEXP output = rResult.output();
        assertNotNull(builder);
        assertNotNull(rTask);
        assertTrue(rResult.success());
        assertNotNull(output);
        assertTrue(output.isNumeric());
        assertNull(rResult.error());
        assertNull(rResult.cause());
    }

    @Test
    public void testBuilderWithSEXPInput() throws RenjinException {
        SEXP data = new DoubleArrayVector(1,2,3,4,5);
        Renjin builder =Renjin.R(serializedTasks, serializedResults);
        RenjinTask rTask = builder.code("x<-z").input("z", data).build();
        RenjinResult rResult = rTask.execute();
        Map<String,Object> inputMap = rResult.input();
        SEXP z = (SEXP) inputMap.get("z");
        SEXP output = rResult.output();
        assertNotNull(builder);
        assertNotNull(rTask);
        assertTrue(rResult.success());
        assertNotNull(z);
        assertTrue(z.length() == 5);
        assertEquals(data, z);
        assertNotNull(output);
        assertTrue(output.length() == 5);
        assertNull(rResult.error());
        assertNull(rResult.cause());
    }

    @Test
    public void testBuilderWithPrimitiveInput() throws RenjinException {
        Renjin builder =Renjin.R(serializedTasks, serializedResults);
        RenjinTask rTask = builder.code("rnorm(n, mean)")
                                  .input("n", 10)
                                  .input("mean", 5)
                                  .build();
        RenjinResult rResult = rTask.execute();
        Map<String,Object> inputMap = rResult.input();
        Integer n = (Integer) inputMap.get("n");
        Integer mean = (Integer) inputMap.get("mean");
        SEXP output = rResult.output();
        assertNotNull(builder);
        assertNotNull(rTask);
        assertTrue(rResult.success());
        assertNotNull(n);
        assert(n == 10);
        assertNotNull(mean);
        assert(mean == 5);
        assertNotNull(output);
        assertNull(rResult.error());
        assertNull(rResult.cause());
    }

    @Test
    public void testBuilderWithCodeAndMisnamedInput() throws RenjinException {
        Renjin builder = Renjin.R(serializedTasks, serializedResults);
        RenjinTask rTask = builder.code("x<-n").input("m", 12).build();
        // Input "m" passed, code expect "n", expect !result.success.
        RenjinResult rResult = rTask.execute();
        Map<String,Object> inputMap = rResult.input();
        Integer m = (Integer) inputMap.get("m");
        assertNotNull(builder);
        assertNotNull(rTask);
        assertFalse(rResult.success());
        assertNotNull(m);
        assert(m == 12);
        assertNotNull(rResult.error());
        assertNotNull(rResult.cause());
        assert(rResult.cause() instanceof org.renjin.eval.EvalException);
    }

    @Test
    public void testBuilderWithCodeAndMismatchedInput() throws RenjinException {
        Renjin builder = Renjin.R(serializedTasks, serializedResults);
        RenjinTask rTask = builder.code("x<-2*n").input("n", "bad").build();
        // Input "n" passed but null, expect !result.success.
        RenjinResult rResult = rTask.execute();
        Map<String,Object> inputMap = rResult.input();
        String n = (String) inputMap.get("n");
        assertNotNull(builder);
        assertNotNull(rTask);
        assertFalse(rResult.success());
        assertNotNull(n);
        assert(n == "bad");
        assertNotNull(rResult.error());
        assertNotNull(rResult.cause());
        assert(rResult.cause() instanceof org.renjin.eval.EvalException);
    }

    @Test
    public void testBuilderWithCodeAndMatchedInput() throws RenjinException {
        Renjin builder = Renjin.R(serializedTasks, serializedResults);
        RenjinTask rTask = builder.code("x<-n").input("n", 12).build();
        RenjinResult rResult = rTask.execute();
        Map<String,Object> inputMap = rResult.input();
        Integer n = (Integer) inputMap.get("n");
        SEXP output = rResult.output();
        assertNotNull(builder);
        assertNotNull(rTask);
        assertTrue(rResult.success());
        assertNotNull(n);
        assert(n == 12);
        assertNotNull(output);
        assertTrue(output.isNumeric());
        assertNull(rResult.error());
        assertNull(rResult.cause());
    }

}
