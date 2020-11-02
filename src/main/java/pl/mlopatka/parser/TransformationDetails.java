package pl.mlopatka.parser;

import java.util.Map;

public class TransformationDetails {

    private Map<String, Object> fieldsConfig;
    private Map<String, String> postActions;

    public TransformationDetails() {
    }

    public TransformationDetails(Map<String, Object> fieldsConfig, Map<String, String> postActions) {
        this.fieldsConfig = fieldsConfig;
        this.postActions = postActions;
    }

    public Map<String, Object> getFieldsConfig() {
        return fieldsConfig;
    }

    public Map<String, String> getPostActions() {
        return postActions;
    }

}
