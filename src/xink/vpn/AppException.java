/*
 * Copyright 2011 yingxinwu.g@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xink.vpn;

public class AppException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private int messageCode;

	private Object[] messageArgs;

	public AppException(final String detailMessage) {
		super(detailMessage);
	}

	public AppException(final String message, final int msgCode,
			final Object... msgArgs) {
		super(message);
		this.messageCode = msgCode;
		this.messageArgs = msgArgs;
	}

	public AppException(final String message, final Throwable throwable,
			final int msgCode, final Object... msgArgs) {
		super(message, throwable);
		this.messageCode = msgCode;
		this.messageArgs = msgArgs;
	}

	public AppException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public int getMessageCode() {
		return messageCode;
	}

	public Object[] getMessageArgs() {
		return messageArgs;
	}

}
