package com.example.currencyconversion;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
public class Conversion {
    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/";

    public double convert(String fromCurrency, String toCurrency, double amount) throws Exception {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(API_URL + fromCurrency)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("API Error");
            }
            Gson gson = new Gson();
            String jsonData = response.body().string();
            ExchangeRates rates = gson.fromJson(jsonData, ExchangeRates.class);

            Double rate = rates.rates.get(toCurrency);
            if (rate == null) throw new IllegalArgumentException("Moeda não suportada");
            return amount * rate;
        }
    }

    private class ExchangeRates {
        public java.util.Map<String, Double> rates;
    }
}
