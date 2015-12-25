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
package io.onetapbeyond.renjin.r.executor.manager;

import io.onetapbeyond.renjin.r.executor.Renjin;
import javax.script.*;
import java.util.concurrent.LinkedBlockingQueue;

/*
 * RenjinManager manages an elastic pool of Renjin
 * javax.script.ScriptEngine instances.
 */
public enum RenjinManager {

	POOL;

	/*
	 * Retrieve a Renjin ScriptEngine intance from the pool.
	 */
	public ScriptEngine getEngine() {

		ScriptEngine engine = enginePool.poll();

		if(engine == null) {
			/*
			 * Add new Renjin ScriptEngine instance to the pool.
			 */
			try {
				long bstart = System.currentTimeMillis();
				engine = Renjin.scriptEngine();
				long bend = System.currentTimeMillis();
				long taken = bend-bstart;
			} catch(Exception eex) {
				engine = null;
			}
		}
		return engine;
	}

	/*
	 * Return a Renjin ScriptEngine instance to the pool.
	 */
	public void releaseEngine(ScriptEngine engine) {
		try {
			/*
			 * Return engine to pool if elastic pool
			 * size limit not reached, otherwise drop.
			 */
			if(enginePool.size() < maxEnginesInPool)
				enginePool.add(engine);
		} catch(NullPointerException nex) {}
	}


	/*
	 * Renjin ScriptEngine elastic pool, unbounded.
	 */
	private LinkedBlockingQueue<ScriptEngine> enginePool;

	/*
	 * Renjin ScriptEngine pool size limit. Custom limit
	 * can be set using System property, otherwise defaults.
	 */
	private long maxEnginesInPool =
		Long.getLong("renjin-r-executor-pool-size-limit", 12);

	/*
	 * Initialize Renjin ScriptEngine elastic pool with
	 * a single ScriptEngine instance.
	 */
	private RenjinManager() {
		enginePool = new LinkedBlockingQueue<ScriptEngine>();
		try {
			ScriptEngine seedEngine = Renjin.scriptEngine();
			enginePool.put(seedEngine);
		} catch(InterruptedException iex) {}
	}

}