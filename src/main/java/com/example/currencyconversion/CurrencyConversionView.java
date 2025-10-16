package com.example.currencyconversion;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("currency")
@PageTitle("Currency Conversion")
@Menu(order = 3, icon = "vaadin:dollar", title = "Currency")
public class CurrencyConversionView extends VerticalLayout {

    private static final String[] CURRENCIES = {
            "USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "CNY", "INR", "BRL", "SEK",
            "NOK", "NZD", "ZAR", "SGD", "HKD", "DKK", "PLN", "KRW", "MXN"
    };

    private final Conversion conversion = new Conversion();

    final ComboBox<String> fromCurrency;
    final ComboBox<String> toCurrency;
    final TextField value;
    final Button convertBtn;
    final Span resultSpan;

    public CurrencyConversionView() {
        fromCurrency = new ComboBox<>("From Currency");
        fromCurrency.setItems(CURRENCIES);
        fromCurrency.setValue("USD");

        toCurrency = new ComboBox<>("To Currency");
        toCurrency.setItems(CURRENCIES);
        toCurrency.setValue("EUR");

        value = new TextField("Value");
        value.setPlaceholder("Valor a converter");
        value.setAriaLabel("Valor");

        convertBtn = new Button("Convert", event -> convertCurrency());
        convertBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        resultSpan = new Span();
        resultSpan.getStyle().set("font-weight", "bold").set("font-size", "1.15em");

        // Adiciona o resultado acima do formulário
        add(resultSpan, fromCurrency, toCurrency, value, convertBtn);
    }

    private void convertCurrency() {
        try {
            double result = conversion.convert(
                    fromCurrency.getValue(),
                    toCurrency.getValue(),
                    Double.parseDouble(value.getValue())
            );
            resultSpan.setText(
                    value.getValue() + " " + fromCurrency.getValue() + " ≈ " +
                            String.format("%.4f", result) + " " + toCurrency.getValue()
            );
        } catch (Exception e) {
            resultSpan.setText("Error: " + e.getMessage());
        }
    }
}
