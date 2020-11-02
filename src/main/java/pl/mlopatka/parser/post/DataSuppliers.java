package pl.mlopatka.parser.post;

import org.iban4j.CountryCode;
import org.iban4j.Iban;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

public class DataSuppliers {

    public static final String SUPPLIER = "sup";
    public static final String FUNCTION = "fun";
    public static final String HARDCODED = "hardcoded";

    public static Object generateValue(String supplierType, String supplierMethod, Object input) {
        if(SUPPLIER.equals(supplierType)) {
            return SUPPLIERS().get(supplierMethod).get();
        }

        if(FUNCTION.equals(supplierType)) {
            return FUNCTIONS().get(supplierMethod).apply(input);
        }

        if(HARDCODED.equals(supplierType)) {
            return input;
        }

        return "";
    }

    private static Map<String, Supplier<? super Object>> SUPPLIERS() {
        Map<String, Supplier<? super Object>> supplierMap = new HashMap<>();
        supplierMap.put("id-6", () -> new Random().nextInt(999999 - 100000) + 100000);

        return supplierMap;
    }

    private static Map<String, Function<? super Object, ? super Object>> FUNCTIONS() {
        Map<String, Function<? super Object, ? super Object>> functionMap = new HashMap<>();
        functionMap.put("iban", a -> {
            switch ((String) a) {
                case "EUR":
                    return Iban.random(CountryCode.DE);
                case "USD":
                    return Iban.random(CountryCode.US);
                case "CHF":
                    return Iban.random(CountryCode.CH);
                case "PL":
                    return Iban.random(CountryCode.PL);
                default:
                    return Iban.random(CountryCode.GB);
            }
        });

        return functionMap;
    }

}
