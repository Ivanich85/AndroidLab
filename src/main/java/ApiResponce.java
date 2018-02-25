import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by ivand on 08.02.2018.
 */
public class ApiResponce {

    //Путь к файлу с данными
    private static final String FILE_PATH = "C:\\Users\\ivand\\IdeaProjects\\AndroidLab\\src\\Output";

    private static final String SERVER_URL = "http://api.fixer.io/";
    private static final String EXIT_APP = "exit";
    private static final String INCORRECT_DATA_ENTRY_MESSAGE = "Incorrect currency name." +
            "\nPlease, try enter the name again or enter \"" + EXIT_APP + "\" to leave the app.";
    private static final String SAME_DATA_ENTRY_MESSAGE = "You entered same currencies." +
            "\nPlease, try enter the name again or enter \"" + EXIT_APP + "\" to leave the app.\n";
    private static final String CONNECTION_LOST_MESSAGE = "Connection lost... Try to receive the query from the file...";
    private static final String NO_DATA_IN_THE_FILE_MESSAGE = "There are no actual data in the file. You need to connect to the Internet";

    private String base;
    private RateObject rates;

    public static void main(String[] args) {

        String currencyFrom;
        String currencyTo;

        Scanner scanner = new Scanner(System.in);

        System.out.println("Please, enter from currency:");
        currencyFrom = inputCurrencyName(scanner);
        System.out.println("Please, enter to currency:");
        currencyTo = inputCurrencyName(scanner);
        while (isSameCurrenciesInput(currencyFrom, currencyTo)) {
            System.out.println(SAME_DATA_ENTRY_MESSAGE);
            System.out.println("Please, enter from currency:");
            currencyFrom = inputCurrencyName(scanner);
            System.out.println("Please, enter to currency:");
            currencyTo = inputCurrencyName(scanner);
        }

        String resultQuery = createSummaryQuery(currencyFrom, currencyTo);
        try {
            printResult(resultQuery);
            writeJsonDateToFile(resultQuery);
        } catch (NullPointerException e) {
            System.out.println(NO_DATA_IN_THE_FILE_MESSAGE);
        }
    }

    //Формируем итоговый запрос
    private static String createSummaryQuery(String from, String to) {
        String fullUrl = SERVER_URL + "latest?base=" + from + "&symbols=" + to;
        try {
            URL serverUrl = new URL(fullUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) serverUrl.openConnection();
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                StringBuilder stringBuilder = new StringBuilder();
                int c;
                while ((c = bufferedReader.read()) != -1) {
                    stringBuilder.append((char) c);
                }
                return stringBuilder.toString();
            }
        } catch (java.net.UnknownHostException hostEx) {
            System.out.println(CONNECTION_LOST_MESSAGE);
            try {
                return readFromFile(FILE_PATH, from, to);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    //Получаем данные из Json
    private static ApiResponce getObjectFromJson(String jsonString) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(RateObject.class, new RatesDeserializer())
                .create();
        return gson.fromJson(jsonString, ApiResponce.class);
    }

    //Выводим результат
    private static void printResult(String jsonString) {
        System.out.print(getObjectFromJson(jsonString).base);
        System.out.print(" => ");
        System.out.print(getObjectFromJson(jsonString).rates.getName());
        System.out.print(" : ");
        System.out.printf("%.3f\n", getObjectFromJson(jsonString).rates.getRate());
    }

    //Проверяем ввод валюты (соответствие существующим на сайте вариантам)
    private static String inputCurrencyName(Scanner sc) {
        String currency = sc.nextLine().toUpperCase();
        checkEntryForExitApp(currency);
        while (!Currency.getCurrenciesList().contains(currency)) {
            System.out.println(INCORRECT_DATA_ENTRY_MESSAGE);
            currency = sc.nextLine().toUpperCase();
            checkEntryForExitApp(currency);
        }
        return currency;
    }

    //Проверка на ввод одинаковых валют
    private static boolean isSameCurrenciesInput(String valueFrom, String valueTo) {
        return valueFrom.equals(valueTo);
    }

    //Проверяем ввод для выхода из приложения
    private static void checkEntryForExitApp(String s) {
        if (s.toLowerCase().equals(EXIT_APP)) {
            System.exit(1);
        }
    }

    //Пишем данные в файл
    private static void writeJsonDateToFile(String jsonText) {

        String jsonTextWithNewLine = jsonText + System.getProperty("line.separator");

        File file = new File(FILE_PATH);
        try (FileWriter fileWriter = new FileWriter(file, true);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            if (!isFileContainsData(jsonText, FILE_PATH)) {
                bufferedWriter.write(jsonTextWithNewLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Проверяем наличие данных в файле
    private static boolean isFileContainsData(String jsonData, String fileName) throws FileNotFoundException {
        Scanner scanner = new Scanner(new FileReader(fileName));
        while (scanner.hasNextLine()) {
            if (jsonData.equals(scanner.nextLine())) {
                return true;
            }
        }
        return false;
    }

    //Читаем данные из файла при отсутствии соединения
    private static String readFromFile(String fileName, String valueFrom, String valueTo) throws FileNotFoundException {
        Scanner scanner = new Scanner(new FileReader(fileName));
        ArrayList<String> fileDataList = new ArrayList<>();
        while (scanner.hasNextLine()) {
            fileDataList.add(scanner.nextLine());
        }
        for (String string : fileDataList) {
            if (string.contains(valueFrom) && string.contains("{\"" + valueTo)) {
                return string;
            }
        }
        return null;
    }

}
