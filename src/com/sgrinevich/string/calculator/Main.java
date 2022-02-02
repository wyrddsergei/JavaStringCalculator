/*
 * File: Main.java
 * -------------------
 * Main class of the program.
 * The arguments are being transferred here.
 */

package com.sgrinevich.string.calculator;

import java.util.HashMap;
public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println ("Error: No arguments were provided!");
            System.exit(-1);
        }

        HashMap<String, Double> variables;                         // Hash map which stores variables values
        StringCalculator calculator = new StringCalculator();      // Instance of the calculator
        String formula = args[0];                                  // First argument is a formula
        String[] tokenizedFormula;                                 // Tokenized formula

        tokenizedFormula = calculator.tokenizeExpression(formula);
        variables = calculator.getVariables(args);

        // (Debug messages)
        // Display given formula
        System.out.println("Given formula: " + formula);

        System.out.print("Formula tokens: ");

        // Display formula after it was tokenized
        for (String i : tokenizedFormula)
            System.out.print(i + " ");
        System.out.println();

        System.out.println("Result: " + calculator.calculate(tokenizedFormula, variables));
    }
}