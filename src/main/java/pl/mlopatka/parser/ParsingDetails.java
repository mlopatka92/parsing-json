package pl.mlopatka.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParsingDetails {

    private String detailsPath;
    private Map<String, String> mapping;
    private Map<String, String> postActions;
    private Map<String, List<String>> filters;

    public ParsingDetails(String detailsPath, Map<String, String> mapping, Map<String, String> postActions) {
        this.detailsPath = detailsPath;
        this.mapping = mapping;
        this.postActions = postActions;
        this.filters = new HashMap<>();
    }

    public ParsingDetails(String detailsPath, Map<String, String> mapping, Map<String, String> postActions,
                          Map<String, List<String>> filters) {
        this.detailsPath = detailsPath;
        this.mapping = mapping;
        this.postActions = postActions;
        this.filters = filters;
    }

    public String getDetailsPath() {
        return detailsPath;
    }

    public Map<String, String> getMapping() {
        return mapping;
    }

    public Map<String, String> getPostActions() {
        return postActions;
    }

    public Map<String, List<String>> getFilters() {
        return filters;
    }
}
