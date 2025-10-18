package com.example.currencyconversion;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FX conversion with:
 * - shared OkHttp client
 * - ExchangeRate-API v6 (keyed) OR open.er-api.com (no key)
 * - BigDecimal math + rounding
 * - 15 min cache per base
 */
public class Conversion {

    // Keyed plan (if you have FX_API_KEY)
    private static final String ERA_V6_KEYED = "https://v6.exchangerate-api.com/v6/%s/latest/%s";
    // Open, no key required (daily updates)
    private static final String ERA_V6_OPEN  = "https://open.er-api.com/v6/latest/%s";

    private static final Gson GSON = new Gson();

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(8))
            .readTimeout(Duration.ofSeconds(8))
            .callTimeout(Duration.ofSeconds(10))
            .build();

    private static final ConcurrentHashMap<String, CacheEntry> CACHE = new ConcurrentHashMap<>();
    private static final Duration TTL = Duration.ofMinutes(15);

    public ConversionResult convert(String fromCurrency, String toCurrency, BigDecimal amount) throws Exception {
        if (fromCurrency == null || toCurrency == null) throw new IllegalArgumentException("Currency is null");
        if (amount == null) amount = BigDecimal.ZERO;

        RateBundle bundle = getRates(fromCurrency);
        BigDecimal rate = bundle.rates.get(toCurrency);
        if (rate == null) throw new IllegalArgumentException("Unsupported currency: " + toCurrency);

        BigDecimal converted = amount.multiply(rate);
        BigDecimal inverse = BigDecimal.ONE.divide(rate, 10, RoundingMode.HALF_EVEN);

        return new ConversionResult(
                fromCurrency, toCurrency,
                scale(converted, 4),
                scale(rate, 6),
                scale(inverse, 6),
                bundle.updatedAtUtc,
                bundle.provider
        );
    }

    // --- types ---

    public static final class ConversionResult {
        public final String from, to;
        public final BigDecimal converted, rate, inverseRate;
        public final String lastUpdatedUtc, provider;
        public ConversionResult(String from, String to, BigDecimal converted, BigDecimal rate,
                                BigDecimal inverseRate, String lastUpdatedUtc, String provider) {
            this.from = from; this.to = to;
            this.converted = converted; this.rate = rate; this.inverseRate = inverseRate;
            this.lastUpdatedUtc = lastUpdatedUtc; this.provider = provider;
        }
    }

    private static final class CacheEntry {
        final RateBundle bundle; final Instant fetchedAt;
        CacheEntry(RateBundle b) { this.bundle = b; this.fetchedAt = Instant.now(); }
        boolean fresh() { return Instant.now().isBefore(fetchedAt.plus(TTL)); }
    }

    private static final class RateBundle {
        final String base; final Map<String, BigDecimal> rates; final String updatedAtUtc; final String provider;
        RateBundle(String base, Map<String, BigDecimal> rates, String updatedAtUtc, String provider) {
            this.base = base; this.rates = rates; this.updatedAtUtc = updatedAtUtc; this.provider = provider;
        }
    }

    private RateBundle getRates(String base) throws Exception {
        Objects.requireNonNull(base);
        base = base.trim().toUpperCase();

        CacheEntry cached = CACHE.get(base);
        if (cached != null && cached.fresh()) return cached.bundle;

        String key = Optional.ofNullable(System.getenv("FX_API_KEY"))
                .orElse(System.getProperty("FX_API_KEY"));

        RateBundle bundle = (key != null && !key.isBlank())
                ? fetchEraV6Keyed(key.trim(), base)
                : fetchOpenV6(base);

        CACHE.put(base, new CacheEntry(bundle));
        return bundle;
    }

    // --- fetchers ---

    private RateBundle fetchEraV6Keyed(String apiKey, String base) throws Exception {
        String url = String.format(ERA_V6_KEYED, apiKey, base);
        Request req = new Request.Builder().url(url).build();
        try (Response res = CLIENT.newCall(req).execute()) {
            if (!res.isSuccessful()) throw new RuntimeException("ExchangeRate-API v6 error: HTTP " + res.code());
            String json = res.body().string();
            EraV6 dto = GSON.fromJson(json, EraV6.class);
            if (!"success".equalsIgnoreCase(dto.result)) {
                throw new RuntimeException("ExchangeRate-API v6 result=" + dto.result);
            }
            return new RateBundle(dto.base_code, dto.rates(), dto.time_last_update_utc, "ExchangeRate-API (keyed)");
        }
    }

    private RateBundle fetchOpenV6(String base) throws Exception {
        String url = String.format(ERA_V6_OPEN, base);
        Request req = new Request.Builder().url(url).build();
        try (Response res = CLIENT.newCall(req).execute()) {
            if (!res.isSuccessful()) throw new RuntimeException("open.er-api.com error: HTTP " + res.code());
            String json = res.body().string();
            EraV6 dto = GSON.fromJson(json, EraV6.class);
            if (!"success".equalsIgnoreCase(dto.result)) {
                throw new RuntimeException("open.er-api.com result=" + dto.result);
            }
            return new RateBundle(dto.base_code, dto.rates(), dto.time_last_update_utc, "ExchangeRate-API (Open)");
        }
    }

    // --- DTO for both keyed and open v6 responses ---
    private static final class EraV6 {
        String result;
        String base_code;
        String time_last_update_utc;
        Map<String, BigDecimal> rates;
        Map<String, BigDecimal> rates() { return rates; }
    }

    // --- helpers ---
    private static BigDecimal scale(BigDecimal v, int scale) {
        return v.setScale(scale, RoundingMode.HALF_EVEN);
    }
}
