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
        mapping.put("firstname", "name");
        mapping.put("lastname", "surname");
        mapping.put("private.values[0].productType", "productName:product");

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
        mapping.put("documents[0].name", "type");
        mapping.put("aggregates[id=2].name", "number");

        Map<String, String> postAction = new HashMap<>();
        postAction.put("code", "hardcoded:1234");

        return new ParsingDetails(detailsPath, mapping, postAction);
    }
}
