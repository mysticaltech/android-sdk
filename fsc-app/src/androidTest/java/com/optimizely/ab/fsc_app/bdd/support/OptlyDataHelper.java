package com.optimizely.ab.fsc_app.bdd.support;

import com.optimizely.ab.android.sdk.OptimizelyManager;
import com.optimizely.ab.android.user_profile.DefaultUserProfileService;
import com.optimizely.ab.config.EventType;
import com.optimizely.ab.config.Experiment;
import com.optimizely.ab.config.ProjectConfig;
import com.optimizely.ab.config.Variation;
import com.optimizely.ab.config.parser.ConfigParseException;
import com.optimizely.ab.config.parser.DefaultConfigParser;
import com.optimizely.ab.fsc_app.bdd.optlyplugins.userprofileservices.TestUserProfileService;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class OptlyDataHelper {
    public static ProjectConfig projectConfig;

    public static ArrayList<LinkedHashMap> getUserProfiles(OptimizelyManager optimizely) {
        com.optimizely.ab.bucketing.UserProfileService service = optimizely.getUserProfileService();
        ArrayList<LinkedHashMap> userProfiles = new ArrayList<>();
        if (service.getClass() != DefaultUserProfileService.class) {
            TestUserProfileService testService = (TestUserProfileService) service;
            if (testService.getUserProfiles() != null)
                userProfiles = new ArrayList<LinkedHashMap>(testService.getUserProfiles());
        }

        return userProfiles;
    }

    public static Variation getVariationByKey(Experiment experiment, String variationKey) {
        if (experiment == null)
            return null;

        Variation variation = experiment.getVariationKeyToVariationMap().get(variationKey);

        return variation;
    }

    public static EventType getEventByKey(String eventKey) {
        if (eventKey == null)
            return null;

        EventType eventType = projectConfig.getEventNameMapping().get(eventKey);

        return eventType;
    }

    public static void initializeProjectConfig(String datafile) {
        try {
            projectConfig = DefaultConfigParser.getInstance().parseProjectConfig(datafile);
        } catch (ConfigParseException e) {
            e.printStackTrace();
        }
    }

    public static Experiment getExperimentByKey(String experimentKey) {
        if (projectConfig == null)
            return null;

        Experiment experiment = projectConfig.getExperimentForKey(experimentKey, null);

        return experiment;
    }
}

