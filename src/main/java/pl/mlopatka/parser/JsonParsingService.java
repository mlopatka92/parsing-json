package pl.mlopatka.parser;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import pl.mlopatka.parser.mappers.Mappers;
import pl.mlopatka.parser.post.DataSuppliers;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonParsingService implements ParsingService {

    private final String resourcePath;
    private static final String DETAILS_DIR = "details";
    private static final String TEMPLATE_DIR = "template";
    private static final String RESULT_DIR = "result";

    private final Gson gson = new Gson();

    public JsonParsingService(String resourcesPath) {
        this.resourcePath = resourcesPath;
    }

    @Override
    public List<TransformationDetails> loadTransformationDetails(List<ParsingDetails> parsingDetails) {
        List<TransformationDetails> transformationDetails = new ArrayList<>();
        parsingDetails.forEach(
                pd -> transformationDetails.addAll(loadSingleTransformationDetails(pd.getDetailsPath(), pd.getMapping(), pd.getPostActions(),
                        pd.getFilters()))
        );

        return transformationDetails;
    }

    public List<TransformationDetails> loadSingleTransformationDetails(String pathToFile, Map<String, String> transformationMapping,
                                                                       Map<String, String> postActions, Map<String, List<String>> filters) {
        JsonArray transformationDetails = (JsonArray) readJson(pathToFile, DETAILS_DIR);
        List<TransformationDetails> parsedDetails = new ArrayList<>();

        for (int i = 0; i < transformationDetails.size(); i++) {
            JsonObject obj = (JsonObject) transformationDetails.get(i);
            if (checkFilterConditions(obj, filters)) {
                parsedDetails.add(createDetails(obj, transformationMapping, postActions));
            }
        }

        return parsedDetails;
    }

    private boolean checkFilterConditions(JsonObject obj, Map<String, List<String>> filters) {
        for (Map.Entry<String, List<String>> entry : filters.entrySet()) {
            if(!checkSingleFilterCondition(obj, entry.getKey(), entry.getValue())) {
                return false;
            }
        }

        return true;
    }

    private boolean checkSingleFilterCondition(JsonObject obj, String attr, List<String> acceptedValues) {
        String attrValue = String.valueOf(getValue(obj, attr));
        return acceptedValues.stream().anyMatch(val -> val.equals(attrValue));
    }

    private TransformationDetails createDetails(JsonObject detailsSource, Map<String, String> config,
                                                Map<String, String> postActions) {
        Map<String, Object> mappingDetails = new HashMap<>();
        config.forEach((k, v) -> mappingDetails.put(k, applyMapper(v, detailsSource)));

        return new TransformationDetails(mappingDetails, postActions);
    }

    private Object applyMapper(String attr, JsonObject detailsSource) {
        String[] allAttr = attr.split(":");
        if (allAttr.length <= 1) {
            return detailsSource.get(attr).getAsString();
        }

        return Mappers.ALL_MAPPERS().get(allAttr[1]).apply(detailsSource.get(allAttr[0]).getAsString());
    }

    @Override
    public void transformTemplate(String pathToTemplate, String destFile, List<TransformationDetails> transformationDetails) {
        JsonElement template = readJson(pathToTemplate, TEMPLATE_DIR);
        try (FileWriter resultFile = new FileWriter(resourcePath + RESULT_DIR + "\\" + destFile)) {
            gson.toJson(transformJson(template, transformationDetails), resultFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JsonElement readJson(String pathToTemplate, String dir) {
        try (FileReader templateFile = new FileReader(resourcePath + dir + "\\" + pathToTemplate)) {
            return gson.fromJson(templateFile, JsonElement.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new IllegalArgumentException("Wrong json file");
    }

    private JsonElement transformJson(JsonElement template, List<TransformationDetails> transformationDetails) {
        JsonArray root = new JsonArray();
        transformationDetails.forEach(
                details -> root.add(createSingleElement(template.deepCopy(), details))
        );

        return root;
    }

    private JsonElement createSingleElement(JsonElement template, TransformationDetails transformationDetails) {
        JsonObject source = (JsonObject) template;
        transformationDetails.getFieldsConfig()
                .forEach((k, v) -> updateSingleAttr(source, k, v));
        transformationDetails.getPostActions()
                .forEach((k, v) -> applySinglePostAction(source, k, v));

        return source;
    }

    private void applySinglePostAction(JsonObject source, String attr, String valueGenerator) {
        String[] generatorInfo = valueGenerator.split(":");
        if (DataSuppliers.HARDCODED.equals(generatorInfo[0])) {
            updateSingleAttr(source, attr, generatorInfo[1]);
            return;
        }

        if (DataSuppliers.SUPPLIER.equals(generatorInfo[0])) {
            updateSingleAttr(source, attr, DataSuppliers.generateValue(generatorInfo[0], generatorInfo[1], null));
            return;
        }

        if (DataSuppliers.FUNCTION.equals(generatorInfo[0])) {
            updateSingleAttr(source, attr, DataSuppliers.generateValue(generatorInfo[0], generatorInfo[1], getValue(source, generatorInfo[2])));
        }
    }

    private void updateSingleAttr(JsonObject source, String attr, Object value) {
        String[] attributes = attr.split("\\.");
        if (attributes.length == 1) {
            source.addProperty(attributes[0], String.valueOf(value));
            return;
        }

        JsonObject destObject = getSingleJsonObject(source, attributes[0]);
        for (int i = 1; i < attributes.length - 1; i++) {
            destObject = getSingleJsonObject(destObject, attributes[i]);
        }

        destObject.addProperty(attributes[attributes.length - 1], String.valueOf(value));
    }

    private Object getValue(JsonObject source, String attr) {
        String[] attributes = attr.split("\\.");
        if (attributes.length == 1) {
            return source.getAsJsonPrimitive(attr).getAsString();
        }

        JsonObject destObject = getSingleJsonObject(source, attributes[0]);
        for (int i = 1; i < attributes.length - 1; i++) {
            destObject = getSingleJsonObject(destObject, attributes[i]);
        }

        return destObject.getAsJsonPrimitive(attributes[attributes.length - 1]).getAsString();
    }

    private JsonObject getSingleJsonObject(JsonObject obj, String attribute) {
        if (attribute.length() > 3 && attribute.endsWith("[0]")) {
            return (JsonObject) ((JsonArray) obj.get(attribute.substring(0, attribute.length() - 3))).get(0);
        }

        if(attribute.contains("[")) {
            String arrayExpresion = attribute.substring(attribute.indexOf("[") + 1, attribute.indexOf("]"));
            String arrayAttribute = attribute.substring(0, attribute.indexOf("["));
            String[] filterParams = arrayExpresion.split("=");

            JsonArray array = (JsonArray) obj.get(arrayAttribute);
            for(int i = 0; i < array.size(); i++) {
                if(array.get(i).getAsJsonObject().getAsJsonPrimitive(filterParams[0]).getAsString().equals(filterParams[1])) {
                    return array.get(i).getAsJsonObject();
                }
            }
        }

        return (JsonObject) obj.get(attribute);
    }

}
