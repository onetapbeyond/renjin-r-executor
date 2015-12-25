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
 * RenjinTaskBuilderTests
 * 
 * Tests renjin-r-executor support for R task building,
 * including "clean", "dirty" and "incomplete" builds.
 */
public class RenjinTaskBuilderTests {

    @Test public void testCleanBuilder() throws RenjinException {
        Renjin builder = Renjin.R();
        assertNotNull(builder);
        builder.code("x<-1");
        RenjinTask rTask = builder.build();
        assertNotNull(rTask);
    }

    @Test(expected=RenjinException.class)
    public void testDirtyBuilder() throws RenjinException {

        Renjin builder = Renjin.R();
        assertNotNull(builder);
        builder.code("x<-1");
        RenjinTask rTask = builder.build();
        assertNotNull(rTask);

        // "Dirty" builder should raise RenjinException.
        RenjinTask dirtyTask = builder.build();
    }

    @Test(expected=RenjinException.class)
    public void testIncompleteBuilder() throws RenjinException {

        Renjin builder = Renjin.R();
        assertNotNull(builder);
        // Incomplete builder (no code) should raise RenjinException.
        RenjinTask rTask = builder.build();
    }

}
