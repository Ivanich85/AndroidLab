import java.util.Arrays;
import java.util.List;

/**
 * Created by ivand on 08.02.2018.
 */
public class Currency {

    private final static List<String> CURRENCIES_LIST = Arrays.asList("AUD", "BGN", "BRL", "CAD", "CHF", "CNY",
            "CZK", "DKK", "GBP", "HKD", "HRK", "HUF", "IDR", "ILS", "INR", "JPY", "KRW", "MXN", "MYR", "NOK", "NZD",
            "PHP", "PLN", "RON", "SEK", "SGD", "THB", "TRY", "USD", "ZAR", "EUR", "RUB");

    static List<String> getCurrenciesList() {
        return CURRENCIES_LIST;
    }
}
