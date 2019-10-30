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

package com.optimizely.ab.integration_test.app.support;

import com.optimizely.ab.android.sdk.OptimizelyManager;
import com.optimizely.ab.bucketing.UserProfileService;
import com.optimizely.ab.event.EventProcessor;
import com.optimizely.ab.integration_test.app.models.ApiOptions;
import com.optimizely.ab.integration_test.app.optlyplugins.ProxyEventDispatcher;
import com.optimizely.ab.integration_test.app.support.apis.*;
import com.optimizely.ab.integration_test.app.models.responses.BaseResponse;
import com.optimizely.ab.integration_test.app.optlyplugins.userprofileservices.NoOpService;
import com.optimizely.ab.notification.NotificationCenter;

import java.lang.reflect.Constructor;
import java.util.*;

import static com.optimizely.ab.integration_test.app.optlyplugins.OptimizelyUtils.getEventProcessor;
import static com.optimizely.ab.integration_test.app.optlyplugins.OptimizelyUtils.getNotificationCenter;
import static com.optimizely.ab.integration_test.app.support.Utils.parseYAML;

public class OptimizelyWrapper {
    private final static String OPTIMIZELY_PROJECT_ID = "123123";
    private OptimizelyManager optimizelyManager;
    private static final Map<String, OptimizelyManager> optimizelyInstanceMap = new HashMap<>();
    private static final Map<String, ProxyEventDispatcher> eventHandlerInstanceMap = new HashMap<>();
    private static final Map<String, List<Map<String, Object>>> notificationsInstanceMap = new HashMap<>();

    private List<Map<String, Object>> notifications = new ArrayList<>();

    public void addNotification(Map<String, Object> notificationMap) {
        notifications.add(notificationMap);
    }

    private void setNotifications(List<Map<String, Object>> notifications) {
        this.notifications = notifications;
    }

    public List<Map<String, Object>> getNotifications() {
        return notifications;
    }

    public OptimizelyManager getOptimizelyManager() {
        return optimizelyManager;
    }

    public void initializeOptimizely(ApiOptions apiOptions) {
        String sessionId = apiOptions.getSessionId();
        if (sessionId != null && !sessionId.isEmpty() && optimizelyInstanceMap.containsKey(sessionId)) {
            // Use cached instance / eventHandler
            optimizelyManager = optimizelyInstanceMap.get(sessionId);
            if (eventHandlerInstanceMap.containsKey(apiOptions.getSessionId()))
                apiOptions.setDispatchedEvents(eventHandlerInstanceMap.get(apiOptions.getSessionId()).getDispatchedEvents());

            if (notificationsInstanceMap.containsKey(sessionId)) {
                setNotifications(notificationsInstanceMap.get(sessionId));
            }
        } else {
            UserProfileService userProfileService = null;
            if (apiOptions.getUserProfileService() != null) {
                try {
                    Class<?> userProfileServiceClass = Class.forName("com.optimizely.ab.integration_test.app.optlyplugins.userprofileservices." + apiOptions.getUserProfileService());
                    Constructor<?> serviceConstructor = userProfileServiceClass.getConstructor(ArrayList.class);
                    userProfileService = UserProfileService.class.cast(serviceConstructor.newInstance(apiOptions.getUserProfiles()));
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            if (userProfileService == null) {
                userProfileService = new NoOpService();
            }
            NotificationCenter notificationCenter = getNotificationCenter(apiOptions.getWithListener(), this);
            ProxyEventDispatcher eventHandler = new ProxyEventDispatcher(apiOptions.getDispatchedEvents());

            EventProcessor eventProcessor = getEventProcessor(apiOptions, eventHandler, notificationCenter);

            optimizelyManager = OptimizelyManager.builder(OPTIMIZELY_PROJECT_ID)
                    .withEventHandler(eventHandler)
                    .withNotificationCenter(notificationCenter)
                    .withEventProcessor(eventProcessor)
                    .withUserProfileService(userProfileService)
                    .build(apiOptions.getContext());

            optimizelyManager.initialize(apiOptions.getContext(),
                    apiOptions.getDatafile()
            );
            if (sessionId != null && !sessionId.isEmpty()) {
                optimizelyInstanceMap.put(sessionId, optimizelyManager);
                eventHandlerInstanceMap.put(sessionId, eventHandler);
                notificationsInstanceMap.put(sessionId, getNotifications());
            }
        }
    }

    public BaseResponse callApi(ApiOptions apiOptions) {
        if (optimizelyManager == null) {
            initializeOptimizely(apiOptions);
        }

        Object argumentsObj = parseYAML(apiOptions.getArguments());
        try {
            switch (apiOptions.getApi()) {
                case "activate":
                    return new ActivateAPICall().invokeAPI(this, argumentsObj);
                case "track":
                    return new TrackAPICall().invokeAPI(this, argumentsObj);
                case "is_feature_enabled":
                    return new IsFeatureEnabledAPICall().invokeAPI(this, argumentsObj);
                case "get_variation":
                    return new GetVariationAPICall().invokeAPI(this, argumentsObj);
                case "get_enabled_features":
                    return new GetEnabledFeaturesAPICall().invokeAPI(this, argumentsObj);
                case "get_feature_variable_double":
                    return new GetFeatureVariableDoubleAPICall().invokeAPI(this, argumentsObj);
                case "get_feature_variable_boolean":
                    return new GetFeatureVariableBooleanAPICall().invokeAPI(this, argumentsObj);
                case "get_feature_variable_integer":
                    return new GetFeatureVariableIntegerAPICall().invokeAPI(this, argumentsObj);
                case "get_feature_variable_string":
                    return new GetFeatureVariableStringAPICall().invokeAPI(this, argumentsObj);
                case "get_forced_variation":
                    return new GetForcedVariationAPICall().invokeAPI(this, argumentsObj);
                case "set_forced_variation":
                    return new ForcedVariationAPICall().invokeAPI(this, argumentsObj);
                case "close":
                    return new CloseAPICall().invokeAPI(this, argumentsObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}