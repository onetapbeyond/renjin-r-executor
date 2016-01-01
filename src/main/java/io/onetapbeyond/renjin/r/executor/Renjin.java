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

import io.onetapbeyond.renjin.r.executor.tasks.RenjinTaskImpl;

import javax.script.*;
import org.renjin.sexp.*;
import org.renjin.eval.Context;
import org.renjin.primitives.io.serialization.*;

import java.io.Reader;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.util.Map;
import java.util.HashMap;

/**
 * Builder of executable R tasks targeting the Renjin interpreter
 * for scientific computing, reproducible research and
 * data analysis based on R. To create an instance of a
 * {@link RenjinTask} capable of executing any function or script
 * within
 * <a href="http://packages.renjin.org/" target="_blank">
 * Renjin-compatible R packages</a>,
 * use the following builder pattern:
 * <pre> 
 * {@code
 * RenjinTask rTask = Renjin.R().code(rCode).input(rInput).build();
 * }
 * </pre>
 * The executable R code for the task is specified using the
 * {@link #code(java.io.Reader code) code(...)} methods.
 * One or more data inputs are passed on an executable R task using the
 * {@link #input(java.util.Map) input(...)}
 * methods.
 * If your application runs in a single JVM environment you can use
 * the {@link #R(boolean,boolean) Renjin.R(boolean, boolean)} method
 * to optionally bypass {@link RenjinTask} and {@link RenjinResult}
 * serialization.
 */
public class Renjin {

	/*
	 * RenjinTask R code supplied on builder.
	 */
	private String code;
	/*
	 * RenjinTask SEXP data inputs.
	 */
	private Map<String,Object> sexpInputs = new HashMap<String,Object>();
	/*
	 * RenjinTask primitive data inputs.
	 */
	private Map<String,Object> primInputs = new HashMap<String,Object>();
	/*
	 * By default, all data maintained on RenjinTask are serialized.
	 */
	private boolean disableTaskSerialization;
	/*
	 * By default, all data maintained on RenjinResult are serialized.
	 */
	private boolean disableResultSerialization;

	/*
	 * Flag tracking status of Renjin builder instance to enforce "clean"
	 * builder policy. If more than one attempt is made to call build(...)
	 * on an instance, "dirty" builder raises exception.
	 */
	private boolean cleanBuilder = true;

	private Renjin() {}

	private Renjin(boolean disableTaskSerialization,
				   boolean disableResultSerialization) {
		this.disableTaskSerialization = disableTaskSerialization;
		this.disableResultSerialization = disableResultSerialization;
	}

	/**
	 * Create an instance of a Renjin task builder.
	 * @return an instance of a Renjin task builder
	 */
	public static Renjin R() {
		return new Renjin();
	}

	/**
	 * Create an instance of a Renjin task builder. By default, both 
	 * {@link RenjinTask} and {@link RenjinResult} are serialized. If either
	 * your {@link RenjinTask} or {@link RenjinResult} do not require
	 * serialization, for example, when executing within a single JVM
	 * environment, then enable the <i>disableTaskSerialization</i> and
	 * <i>disableResultSerialization</i> parameters respectively.
	 * @param disableTaskSerialization disable {@link RenjinTask}
	 * serialization
	 * @param disableResultSerialization disable {@link RenjinResult}
	 * serialization
	 * @return the current Renjin task builder instance
	 */
	public static Renjin R(boolean disableTaskSerialization,
						   boolean disableResultSerialization) {
		return new Renjin(disableTaskSerialization,
							disableResultSerialization);
	}

	/**
	 * Specify the R code for the executable R task.
	 * @param code the R code to execute on the {@link RenjinTask}
	 * @return the current Renjin task builder instance
	 * @throws RenjinException if R code provided is null
	 */
	public Renjin code(String code) throws RenjinException {

		if(code == null)
			throw new RenjinException();

		this.code = code;
		return this;
	}

	/**
	 * Specify the R code for the executable R task.
	 * @param code the R code to execute on the {@link RenjinTask}
	 * @return the current Renjin task builder instance
	 * @throws RenjinException if {@link java.io.Reader} provided could not be read
	 */
	public Renjin code(Reader code) throws RenjinException {

		StringBuilder codeBuf = new StringBuilder();

		try {
			BufferedReader br = new BufferedReader(code);
			String codeLine = null;
			while ((codeLine = br.readLine()) != null)
		    	codeBuf.append(codeLine);
		} catch(Exception rex) {
			throw new RenjinException(rex);
		}

		this.code = codeBuf.toString();
		return this;
	}

	/**
	 * Specify a data input for the executable R task.
	 * @param name data input name
	 * @param value data input value
	 * @return the current Renjin task builder instance
	 * @throws RenjinException if input data could not be serialized
	 */
	public Renjin input(String name, Object value) throws RenjinException {

		try {

			if(value instanceof SEXP) {

				if(disableTaskSerialization) {

					/*
					 * Capture SEXP encoded input data.
					 */
					sexpInputs.put(name, value);

				} else {

					/*
					 * Capture SEXP serialized input data.
					 */
					sexpInputs.put(name,
						RenjinTaskImpl.serializeSEXP((SEXP) value));

				}

			} else {

				/*
				 * Capture primitive input data.
				 */
				primInputs.put(name, value);
			}

		} catch(Exception iex) {
			throw new RenjinException(iex);
		}
		return this;
	}

	/**
	 * Specify data inputs for the executable R task.
	 * @param nameValues a {@link java.util.Map} of data input name-value pairs.
	 * @return the current Renjin task builder instance
	 * @throws RenjinException if input data could not be serialized
	 */
	public Renjin input(Map<String,Object> nameValues) throws RenjinException {

		try {
			for (Map.Entry<String, Object> pair : nameValues.entrySet()) {
				// Delegate to input(String, Object) method.
				input(pair.getKey(), pair.getValue());	
			}
		} catch(Exception iex) {
			throw new RenjinException(iex);
		}
		return this;
	}

	/**
	 * Create an instance of an executable R task.
	 * @return an instance of a new executable {@link RenjinTask}
	 * @throws RenjinException if executable R task definition is incomplete
	 */
	public RenjinTask build() throws RenjinException {
		return build(null);
	}

	/**
	 * Create an instance of an executable R task.
	 * @param suppliedEngine a {@link javax.script.ScriptEngine} supplied by
	 * the calling application to use when executing the task
	 * @return an instance of a new executable {@link RenjinTask}
	 * @throws RenjinException if {@link RenjinTask} definition is incomplete
	 */
	public RenjinTask build(ScriptEngine suppliedEngine) throws RenjinException {
		return build(suppliedEngine, false);
	}

	/**
	 * Create an instance of an executable R task.
	 * @param suppliedEngine a {@link javax.script.ScriptEngine} supplied by
	 * the calling application to use when executing the task
	 * {@link javax.script.ScriptEngine} on which to
	 * execute the R task
	 * @param autoClearSuppliedEngine enable if the suppliedEngine bindings
	 * and workspace should be cleared following R task execution
	 * @return an instance of a new executable {@link RenjinTask}
	 * @throws RenjinException if {@link RenjinTask} definition is incomplete
	 */
	public RenjinTask build(ScriptEngine suppliedEngine,
							boolean autoClearSuppliedEngine)
											throws RenjinException {

		/*
		 * Enforce "clean" Rejnin builder policy.
		 */
		if(!cleanBuilder)
			throw new RenjinException("Renjin builder deactivated.");
		else
			cleanBuilder = false;
  
		/*
		 * Enforce required properties on RenjinTask.
		 */
		if(code == null)
			throw new RenjinException("R code on task not specified.");

		return new RenjinTaskImpl(code, sexpInputs, primInputs,
					 disableTaskSerialization, disableResultSerialization,
					 suppliedEngine, autoClearSuppliedEngine);
	}

	/*
	 * Renjin ScriptEngine engine name.
	 */
	private static final String RENJIN_ENGINE = "Renjin";
	private static final ScriptEngineManager scriptEngineManager =
										new ScriptEngineManager();

	/**
	 * Renjin {@link javax.script.ScriptEngine} factory. Provided as
	 * a convenience method where external control over the Renjin
	 * runtime environment is required. Intended to be used in 
	 * conjunction with the
	 * {@link Renjin#build(ScriptEngine)} and the
	 * {@link Renjin#build(ScriptEngine, boolean)} methods.
	 * @return an instance of a Renjin {@link javax.script.ScriptEngine}
	 */
	public static ScriptEngine scriptEngine() {
		return scriptEngineManager.getEngineByName(RENJIN_ENGINE);
	}

}
