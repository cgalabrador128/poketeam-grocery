package com.grocery.util;

import javafx.scene.control.TextFormatter;

public class GeneralFormatter {

    public TextFormatter<String> createNumberFormatter(int maxLength) {
        return new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*") && newText.length() <= maxLength) {
                return change;
            }
            return null;
        });
    }


}
