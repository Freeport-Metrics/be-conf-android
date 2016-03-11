package com.freeportmetrics.beaconf;

/**
 * Created by marcin on 2016-03-09.
 */
public class ConfigurationService {

    public ConfigurationItem[] getConfiguration() throws Exception{
        // TODO: call server
        ConfigurationItem[] configurationItems = {new ConfigurationItem("beacon 1", 5.0f), new ConfigurationItem("beacon 2", 10.0f)};
        return configurationItems;
    }
}
