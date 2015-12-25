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
import org.renjin.eval.EvalException;

/*
 * RenjinScriptEngineTests
 * 
 * Tests renjin-r-executor support for simple ScriptEngine
 * instance creation. Tests focus on Renjin ScriptEngine
 * runtime behavior, not renjin-r-executor runtime behavior.
 */
public class RenjinScriptEngineTests {

    @Test(expected=EvalException.class)
    public void testRenjinRemoveX()
        throws ScriptException, EvalException {

        // Create ScriptEngine instance.
        ScriptEngine rEngine = Renjin.scriptEngine();
        assertNotNull(rEngine);

        // Create x in workspace.
        Object xorig = rEngine.eval("x <- 1");
        assertNotNull(xorig);

        // Retrieve x from workspace.
        Object xretrieved = rEngine.eval("x");
        assertNotNull(xretrieved);

        // Remove x from workspace.
        Object rmx = rEngine.eval("rm(x)");
        assert(rmx instanceof org.renjin.sexp.Null);

        // List objects in workspace.
        Object ls = rEngine.eval("ls()");
        assert(ls instanceof org.renjin.sexp.StringArrayVector);

        // Attempt to retrieve X from workspace, 
        // should raise an EvalException.
        Object nox = rEngine.eval("x");
    }

    @Test(expected=EvalException.class)
    public void testRenjinRemoveAll()
        throws ScriptException, EvalException {

        // Create ScriptEngine instance.
        ScriptEngine rEngine = Renjin.scriptEngine();
        assertNotNull(rEngine);

        // Create x in workspace.
        Object xorig = rEngine.eval("x <- 1");
        assertNotNull(xorig);

        // Retrieve x from workspace.
        Object xretrieved = rEngine.eval("x");
        assertNotNull(xretrieved);

        // Remove all objects from workspace.
        Object rmall = rEngine.eval("rm(list = ls())");

        // List objects in workspace.
        Object ls = rEngine.eval("ls()");
        assert(ls instanceof org.renjin.sexp.StringArrayVector);

        // Attempt to retrieve X from workspace, 
        // should raise an EvalException.
        Object nox = rEngine.eval("x");
    }

}
