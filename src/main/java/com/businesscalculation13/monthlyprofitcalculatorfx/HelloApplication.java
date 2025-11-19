package com.businesscalculation13.monthlyprofitcalculatorfx;

import javafx.application.Application;
import javafx.stage.Stage;


public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {

        new MonthlyProfitCalculatorFX().start(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}