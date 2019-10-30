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

package com.optimizely.ab.integration_test.app.support.apis;

import com.optimizely.ab.config.Variation;
import com.optimizely.ab.integration_test.app.support.OptimizelyWrapper;
import com.optimizely.ab.integration_test.app.models.apiparams.ForcedVariationParams;
import com.optimizely.ab.integration_test.app.models.responses.BaseResponse;
import com.optimizely.ab.integration_test.app.models.responses.ListenerMethodResponse;

public class ForcedVariationAPICall extends APICall<Object> {

    public ForcedVariationAPICall() {
        super();
    }

    @Override
    public BaseResponse invokeAPI(OptimizelyWrapper optimizelyWrapper, Object desreailizeObject) {
        ForcedVariationParams forcedVariationParams = mapper.convertValue(desreailizeObject, ForcedVariationParams.class);
        return setForcedVariation(optimizelyWrapper, forcedVariationParams);
    }

    private ListenerMethodResponse<Object> setForcedVariation(OptimizelyWrapper optimizelyWrapper, ForcedVariationParams forcedVariationParams) {

        Boolean forcedVariation = optimizelyWrapper.getOptimizelyManager().getOptimizely().setForcedVariation(
                forcedVariationParams.getExperimentKey(),
                forcedVariationParams.getUserId(),
                forcedVariationParams.getForcedVariationKey()
        );

        if (!forcedVariation) {
            return sendResponse(forcedVariation, optimizelyWrapper);
        }

        Variation variation = optimizelyWrapper.getOptimizelyManager().getOptimizely().getForcedVariation(
                forcedVariationParams.getExperimentKey(),
                forcedVariationParams.getUserId());
        String variationKey = null;
        if(variation != null)
            variationKey = variation.getKey();

        return sendResponse(variationKey, optimizelyWrapper);
    }
}