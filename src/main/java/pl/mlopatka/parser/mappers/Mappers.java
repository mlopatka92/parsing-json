package pl.mlopatka.parser.mappers;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Mappers {

    public static Map<String, Function<? super Object, ? super Object>> ALL_MAPPERS() {
        Map<String, Function<? super Object, ? super Object>> mappers = new HashMap<>();
        mappers.put("product", s -> productsMapping().get(s.toString()));

        return mappers;
    }

    private static Map<String, Object> productsMapping() {
        Map<String, Object> productsMapping = new HashMap<>();
        productsMapping.put("product1", "abc1");
        productsMapping.put("product2", "w-13");
        productsMapping.put("product3", "o-55");
        productsMapping.put("product4", "de037");
        productsMapping.put("product5", "pk4");

        return productsMapping;
    }

}
