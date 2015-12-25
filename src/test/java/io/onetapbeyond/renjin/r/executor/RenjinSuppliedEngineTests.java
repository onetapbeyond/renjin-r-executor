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
 * RenjinSuppliedEngineTests
 * 
 * Tests renjin-r-executor support for suppliedEngine,
 * a ScriptEngine provided by the calling application.
 */
@RunWith(Parameterized.class)
public class RenjinSuppliedEngineTests {

    private boolean serializedTasks;
    private boolean serializedResults;

    public RenjinSuppliedEngineTests(Map<String,Boolean> params) {
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

   @Test
    public void testNothing() {
        assertTrue(true);
    }

    @Test
    public void testStatelessSuppliedEngine()
        throws ScriptException, RenjinException {

        ScriptEngine suppliedEngine = Renjin.scriptEngine();
        assertNotNull(suppliedEngine);

        //
        // Enable "stateless" for suppliedEngine
        // using build(suppliedEngine, true).
        //
        RenjinTask rTask = Renjin.R(serializedTasks, serializedResults)
                                 .code("x<-n")
                                 .input("n", 12)
                                 .build(suppliedEngine, true);

        RenjinResult rResult = rTask.execute();
        SEXP output = rResult.data();
        assertNotNull(rTask);
        assertTrue(rResult.success());
        assertNotNull(output);
        assertTrue(output.isNumeric());
        assertNull(rResult.error());
        assertNull(rResult.cause());

        //
        // Reuse suppliedEngine, verify object "n" is no
        // longer visible on the stateless engine.
        //
        rTask = Renjin.R(serializedTasks, serializedResults)
                      .code("x<-n")
                      .build(suppliedEngine);

        rResult = rTask.execute();
        output = rResult.data();
        assertNotNull(rTask);
        assertFalse(rResult.success());
        assertNotNull(rResult.error());
        assertNotNull(rResult.cause());
        assert(rResult.cause() instanceof org.renjin.eval.EvalException);
    }

    @Test
    public void testStatefulSuppliedEngine()
        throws ScriptException, RenjinException {

        ScriptEngine suppliedEngine = Renjin.scriptEngine();
        assertNotNull(suppliedEngine);

        //
        // Enable "stateful" for suppliedEngine
        // using build(suppliedEngine) which signifies
        // using build(suppliedEngine, false).
        //
        RenjinTask rTask = Renjin.R(serializedTasks, serializedResults)
                                 .code("x<-n")
                                 .input("n", 12)
                                 .build(suppliedEngine);

        RenjinResult rResult = rTask.execute();
        SEXP output = rResult.data();
        assertNotNull(rTask);
        assertTrue(rResult.success());
        assertNotNull(output);
        assertTrue(output.isNumeric());
        assertNull(rResult.error());
        assertNull(rResult.cause());

        //
        // Reuse suppliedEngine, verify object "n" is
        // still visible on the statelful engine.
        //
        rTask = Renjin.R(serializedTasks, serializedResults)
                      .code("x<-n")
                      .build(suppliedEngine);

        rResult = rTask.execute();
        output = rResult.data();
        assertNotNull(rTask);
        assertTrue(rResult.success());
        assertNotNull(output);
        assertTrue(output.isNumeric());
        assertNull(rResult.error());
        assertNull(rResult.cause());
    }

}
