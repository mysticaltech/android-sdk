/****************************************************************************
 * Copyright 2019, Optimizely, Inc. and contributors                        *
 *                                                                          *
 * Licensed under the Apache License, Version 2.0 (the "License");          *
 * you may not use this file except in compliance with the License.         *
 * You may obtain a copy of the License at                                  *
 *                                                                          *
 *    http://www.apache.org/licenses/LICENSE-2.0                            *
 *                                                                          *
 * Unless required by applicable law or agreed to in writing, software      *
 * distributed under the License is distributed on an "AS IS" BASIS,        *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 * See the License for the specific language governing permissions and      *
 * limitations under the License.                                           *
 ***************************************************************************/

package com.optimizely.ab.fsc_app.bdd.models.responses;

import com.fasterxml.jackson.annotation.JsonProperty;


import java.util.List;
import java.util.Map;

public class ListenerMethodResponse<T>  extends BaseListenerMethodResponse {
    @JsonProperty("result")
    private T result;

    public ListenerMethodResponse(T result, List<Map<String, Object>> listenerCalled) {
        this.result = result;
        this.listenerCalled = listenerCalled;
    }

    public void setListenerCalled(List<Map<String, Object>> listenerCalled) {
        this.listenerCalled = listenerCalled;
    }

    public List<Map<String, Object>> getListenerCalled() {
        return listenerCalled;
    }

    @Override
    public Boolean compareResults(Object expectedVal) {
        if (expectedVal == result) {
            return true;
        }
        if (expectedVal.equals("NULL")) {
            expectedVal = null;
            return expectedVal == result;
        } else if (expectedVal.equals("true") || expectedVal.equals("false")) {
            expectedVal = Boolean.parseBoolean((String) expectedVal);
        } else if (result instanceof Number) {
            try {
                expectedVal = Double.parseDouble((String) expectedVal);
            } catch (Exception e) {}
        }
        return expectedVal.equals(result);
    }

}
