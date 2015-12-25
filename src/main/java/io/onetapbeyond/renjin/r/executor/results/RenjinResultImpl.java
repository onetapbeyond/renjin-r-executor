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
	 * RenjinResult success indicator.
	 */
	private boolean success;

	/*
	 * RenjinResult error message on failure.
	 */
	private String  error;

	/*
	 * RenjinResult error exception on failure.
	 */
	private Throwable cause;

	/*
	 * RenjinResult result data in SEXP serialized form.
	 */
	private byte[] serializedData;

	/*
	 * RenjinResult result data in SEXP native form.
	 */
	private transient SEXP sexpData;

	/*
	 * RenjinResult task time taken indicator.
	 */
	private long timeTaken;

	public RenjinResultImpl(SEXP sexpData, long timeTaken) {
		this.success = true;
		this.sexpData = sexpData;
		this.timeTaken = timeTaken;
	}

	public RenjinResultImpl(byte[] serializedData, long timeTaken) {
		this.success = true;
		this.serializedData = serializedData;
		this.timeTaken = timeTaken;
	}

	public RenjinResultImpl(String error, Throwable cause) {
		this.success = false;
		this.error = error;
		this.cause = cause;
	}

	public boolean success() {
		return success;
	}

	public String error() {
		return error;
	}

	public Throwable cause() {
		return cause;
	}

	public SEXP data() {

		if(serializedData != null) {
			sexpData = deserializeInput("result", serializedData);
		}

		return sexpData;
	}

	public long timeTaken() {
		return timeTaken;
	}

	public String toString() {
		return "RenjinResult: [ " + success + " ]";
	}

}
