package pl.mlopatka.parser;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class ParsingServiceTest {

    private final ParsingService parsingService = new JsonParsingService("src/test/resources/");

    @Test
    public void shouldCreateTransformationDetails() {
        List<TransformationDetails> details =
                parsingService.loadTransformationDetails(Lists.newArrayList(personsTransformation()));

        assertThat(details.size()).isEqualTo(5);
    }

    @Test
    public void shouldCreateFromResource() {
        //given
        String templatePath = "complexTemplate.json";
        String destPath = "result.json";

        List<TransformationDetails> details = parsingService.loadTransformationDetails(
                Lists.newArrayList(personsTransformation(), documentsMapping()));
        parsingService.transformTemplate(templatePath, destPath, details);

        File resultFile = new File("src/test/resources/result/" + destPath);
        assertThat(resultFile).exists();
    }

    private ParsingDetails personsTransformation() {
        String detailsPath = "fromPersons.json";

        Map<String, String> mapping = new HashMap<>();
        mapping.put("name", "firstname");
        mapping.put("surname", "lastname");
        mapping.put("productName:product", "private.values[0].productType");

        Map<String, String> postAction = new HashMap<>();
        postAction.put("private.id", "sup:id-6");
        postAction.put("private.values[0].acc", "fun:iban:private.cur");

        Map<String, List<String>> filters = new HashMap<>();
        filters.put("productName", Lists.newArrayList("product1", "product2", "product3"));

        return new ParsingDetails(detailsPath, mapping, postAction, filters);
    }

    private ParsingDetails documentsMapping() {
        String detailsPath = "fromDocuments.json";

        Map<String, String> mapping = new HashMap<>();
        mapping.put("type", "documents[0].name");
        mapping.put("number", "aggregates[id=2].name");

        Map<String, String> postAction = new HashMap<>();
        postAction.put("code", "hardcoded:1234");

        return new ParsingDetails(detailsPath, mapping, postAction);
    }

    @Test
    public void shouldTransformFile() {
        //given
        String pathToTemplate = "template01.json";
        String desPath = "transformed01.json";
        TransformationDetails transformData = new TransformationDetails(prepareTransformData(), new HashMap<>());

        //when
        parsingService.transformTemplate(pathToTemplate, desPath, Lists.newArrayList(transformData));

        //then
        File resultFile = new File("src/test/resources/result/" + desPath);
        assertThat(resultFile).exists();
        assertThat(resultFile).hasContent("{\"name\":\"Eve\",\"surname\":\"surname\",\"age\":\"25\"}");
    }

    private Map<String, Object> prepareTransformData() {
        Map<String, Object> transformData = new HashMap<>();
        transformData.put("name", "Eve");
        transformData.put("age", 25);

        return transformData;
    }

}
