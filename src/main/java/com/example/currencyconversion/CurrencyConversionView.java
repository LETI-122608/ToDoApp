package com.example.currencyconversion;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Route("currency")
@PageTitle("Currency Conversion")
@Menu(order = 3, icon = "vaadin:dollar", title = "Currency")
public class CurrencyConversionView extends VerticalLayout {

    private static final String[] CODES = {
            "USD","EUR","GBP","JPY","AUD","CAD","CHF","CNY","INR","BRL","SEK",
            "NOK","NZD","ZAR","SGD","HKD","DKK","PLN","KRW","MXN"
    };

    private static final Map<String,String> NAMES = new LinkedHashMap<>();
    static {
        NAMES.put("USD","US Dollar");
        NAMES.put("EUR","Euro");
        NAMES.put("GBP","British Pound");
        NAMES.put("JPY","Japanese Yen");
        NAMES.put("AUD","Australian Dollar");
        NAMES.put("CAD","Canadian Dollar");
        NAMES.put("CHF","Swiss Franc");
        NAMES.put("CNY","Chinese Yuan");
        NAMES.put("INR","Indian Rupee");
        NAMES.put("BRL","Brazilian Real");
        NAMES.put("SEK","Swedish Krona");
        NAMES.put("NOK","Norwegian Krone");
        NAMES.put("NZD","New Zealand Dollar");
        NAMES.put("ZAR","South African Rand");
        NAMES.put("SGD","Singapore Dollar");
        NAMES.put("HKD","Hong Kong Dollar");
        NAMES.put("DKK","Danish Krone");
        NAMES.put("PLN","Polish Złoty");
        NAMES.put("KRW","South Korean Won");
        NAMES.put("MXN","Mexican Peso");
    }

    private final Conversion conversion = new Conversion();

    private final ComboBox<String> fromCurrency = new ComboBox<>("From");
    private final ComboBox<String> toCurrency   = new ComboBox<>("To");
    private final NumberField amount = new NumberField("Amount");
    private final NumberField markup = new NumberField("Markup % (optional)");

    private final Button swapBtn   = new Button(new Icon(VaadinIcon.EXCHANGE));
    private final Button convertBtn = new Button("Convert");

    private final Span resultMain = new Span();
    private final Span resultRate = new Span();
    private final Span resultMeta = new Span();

    public CurrencyConversionView() {
        setSpacing(true);
        setPadding(true);
        setWidthFull();

        fromCurrency.setItems(CODES);
        fromCurrency.setItemLabelGenerator(code -> code + " — " + NAMES.getOrDefault(code, code));
        fromCurrency.setValue("USD");

        toCurrency.setItems(CODES);
        toCurrency.setItemLabelGenerator(code -> code + " — " + NAMES.getOrDefault(code, code));
        toCurrency.setValue("EUR");

        amount.setStep(1.0);
        amount.setMin(0.0);
        amount.setStepButtonsVisible(true);
        amount.setValue(100.0); // sensible default

        // NumberField only accepts numeric input (professional UX) — Vaadin docs.
        // https://vaadin.com/docs/latest/components/number-field

        markup.setStep(0.1);
        markup.setMin(0.0);
        markup.setPlaceholder("0.0");


        // Swap currencies
        swapBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        swapBtn.getElement().setProperty("title", "Swap");
        swapBtn.addClickListener(e -> {
            String a = fromCurrency.getValue();
            fromCurrency.setValue(toCurrency.getValue());
            toCurrency.setValue(a);
            convert();
        });

        // Convert button
        convertBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        convertBtn.addClickListener(e -> convert());

        // Auto-convert on changes
        fromCurrency.addValueChangeListener(e -> convert());
        toCurrency.addValueChangeListener(e -> convert());
        amount.addValueChangeListener(e -> convert());
        markup.addValueChangeListener(e -> convert());

        // Layout
        HorizontalLayout row1 = new HorizontalLayout(fromCurrency, swapBtn, toCurrency);
        row1.setAlignItems(FlexComponent.Alignment.END);

        HorizontalLayout row2 = new HorizontalLayout(amount, markup, convertBtn);
        row2.setAlignItems(FlexComponent.Alignment.END);

        // result card
        Div card = new Div(resultMain, new Div(resultRate), new Div(resultMeta));
        card.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "8px")
                .set("padding", "0.75rem 1rem")
                .set("background", "var(--lumo-base-color)");

        resultMain.getStyle().set("font-size", "1.25rem").set("font-weight", "600");
        resultRate.getStyle().set("color", "var(--lumo-secondary-text-color)");
        resultMeta.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.85em");

        add(row1, row2, card);

        // first render
        convert();
    }

    private void convert() {
        try {
            String from = fromCurrency.getValue();
            String to   = toCurrency.getValue();
            Double amtD = amount.getValue();
            if (from == null || to == null || amtD == null) {
                resultMain.setText("");
                resultRate.setText("");
                resultMeta.setText("");
                return;
            }
            BigDecimal amt = BigDecimal.valueOf(amtD);

            Conversion.ConversionResult r = conversion.convert(from, to, amt);

            double m = markup.getValue() == null ? 0.0 : markup.getValue();
            BigDecimal markupFactor = BigDecimal.valueOf(1.0 + (m / 100.0));
            BigDecimal withMarkup = r.converted.multiply(markupFactor)
                    .setScale(4, java.math.RoundingMode.HALF_EVEN);

            resultMain.setText(String.format("%s %s ≈ %s %s (mid‑market)",
                    stripTrailingZeros(amt), from, stripTrailingZeros(r.converted), to));

            if (m > 0.0) {
                resultRate.setText(String.format(
                        "1 %s = %s %s • 1 %s = %s %s • with %s%% markup: %s %s",
                        from, stripTrailingZeros(r.rate), to,
                        to, stripTrailingZeros(r.inverseRate), from,
                        stripTrailingZeros(BigDecimal.valueOf(m)),
                        stripTrailingZeros(withMarkup), to
                ));
            } else {
                resultRate.setText(String.format(
                        "1 %s = %s %s • 1 %s = %s %s",
                        from, stripTrailingZeros(r.rate), to,
                        to, stripTrailingZeros(r.inverseRate), from
                ));
            }

            resultMeta.setText(String.format("Last updated: %s • Provider: %s",
                    r.lastUpdatedUtc, r.provider));

        } catch (Exception ex) {
            resultMain.setText("Error converting: " + ex.getMessage());
            resultRate.setText("");
            resultMeta.setText("");
        }
    }


    private static String stripTrailingZeros(BigDecimal v) {
        v = v.stripTrailingZeros();
        return v.scale() < 0 ? v.setScale(0).toPlainString() : v.toPlainString();
    }
}
