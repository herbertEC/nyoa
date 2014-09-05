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

import net.htjs.mobile.nyoa.R;

public class NoActiveVpnException extends AppException {

    private static final long serialVersionUID = 1L;

    public NoActiveVpnException(final String detailMessage) {
        super(detailMessage, R.string.err_no_active_vpn);
    }

    public NoActiveVpnException(final String detailMessage, final Throwable throwable) {
        super(detailMessage, throwable, R.string.err_no_active_vpn);
    }

}
