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
package io.onetapbeyond.renjin.r.executor.results;

import static io.onetapbeyond.renjin.r.executor.tasks.RenjinTaskImpl.*;
import io.onetapbeyond.renjin.r.executor.*;
import org.renjin.sexp.SEXP;
import org.renjin.primitives.io.serialization.*;
import java.io.*;
import java.util.Map;
import java.util.HashMap;

/*
 * Concrete implementation of Renjin executor task result.
 * 
 * Instances of {@link RenjinResult} as returned by the
 * {@link RenjinTask#execute} method.
 */
public class RenjinResultImpl implements RenjinResult {

	/*
	 * Generated SUID for RenjinResult, generated using serialver. 
	 */
	private static final long serialVersionUID = 3871346233619381871L;

	/*
	 * Map holding {@link RenjinTask} result data.
	 */
	private Map<String,Object> resultMap;

	public RenjinResultImpl(Map<String,Object> resultMap) {
		this.resultMap = resultMap;
	}

	public boolean success() {
		return (Boolean) resultMap.get("success");
	}

	public String error() {
		return (String) resultMap.get("error");
	}

	public Throwable cause() {
		return (Throwable) resultMap.get("cause");
	}

	public Map<String,Object> input() {

		Map<String,Object> inputMap = new HashMap();

		boolean inputSerialized = (Boolean) resultMap.get("inputSerialized");
		Map<String,Object> sexpInputs =
			(Map<String,Object>) resultMap.get("sexpInputs");
		Map<String,Object> primInputs =
			(Map<String,Object>) resultMap.get("primInputs");

		if(sexpInputs != null) {
			if(inputSerialized) {

				for (Map.Entry<String, Object> pair : sexpInputs.entrySet()) {
					inputMap.put(pair.getKey(),
						deserializeSEXP((byte[]) pair.getValue()));
				}

			} else {
				inputMap.putAll(sexpInputs);
			}
		}

		if(primInputs != null) {
			inputMap.putAll(primInputs);
		}

		return inputMap;
	}

	public SEXP output() {

		boolean outputSerialized = (Boolean) resultMap.get("outputSerialized");
		Object output = resultMap.get("output");

		if(outputSerialized && output != null) {
			output = deserializeSEXP((byte[]) output);
		}

		return (SEXP) output;
	}

	public long timeTaken() {
		return (Long) resultMap.get("timeTaken");
	}

	public String toString() {
		boolean success = (Boolean) resultMap.get("success");
		if(success) 
			return "RenjinResult: [ success ]";
		else {
			String error = (String) resultMap.get("error");
			return "RenjinResult: [ failed ], error=" + error;
		}
	}

}
