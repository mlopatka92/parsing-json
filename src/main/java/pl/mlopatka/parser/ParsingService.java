package pl.mlopatka.parser;

import java.util.List;
import java.util.Map;

public interface ParsingService {

    List<TransformationDetails> loadTransformationDetails(List<ParsingDetails> parsingDetails);

    void transformTemplate(String pathToTemplate, String destFile, List<TransformationDetails> transformationDetails);
}
