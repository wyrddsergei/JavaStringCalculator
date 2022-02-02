/*
 * File: StringCalculator.java
 * -------------------
 * This program allows the user to solve mathematical expressions.
 *
 * All parameters are transferred through program arguments:
 * First argument is the math expression or formula itself.
 * The rest ones are optional, they are the variables
 * for the formula the user decides to use.
 *
 * Variable format: %name_of_variable%=%numeric_value%
 * --------------------
 * The program supports functions such as:
 * sin, cos, tan, atan, log10, log2, sqrt
 */

package com.sgrinevich.string.calculator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

public class StringCalculator {

    /* Operators and their precedence for calculation */
    private static HashMap<Character, Integer> operatorList;

    static {
        operatorList = new HashMap<>();
        operatorList.put('+', 1);
        operatorList.put('-', 1);
        operatorList.put('*', 2);
        operatorList.put('/', 2);
        operatorList.put('^', 3);
    }

    // Array which contains all of the available functions
    private static String[] functionList =
            {"sin", "cos", "tan", "atan", "log10", "log2", "sqrt"};

    /* containers for the RPN conversion */
    private Stack<String> operatorStack = new Stack<>();
    private LinkedList<String> outputQueue = new LinkedList<>();

    /**
     * Reads program arguments and parses them,
     * writing the parsed result into the hash map.
     *
     * @param arguments program arguments
     * @return hash map with all of the variables
     */
    public HashMap<String, Double> getVariables(String[] arguments) {
        HashMap<String, Double> variables = new HashMap<>();

        try {
            for (int i = 1; i < arguments.length; i++) {

                // Argument with removed redundant spaces
                String normalizedArg = arguments[i].replaceAll("\\s","");
                String[] var = normalizedArg.split("=");

                variables.put(var[0], Double.valueOf(var[1]));
            }
        } catch (Exception e) {
            System.out.println("Error: Invalid variable was detected. Please try again.\n" +
                    "Note: Variables should be written in the following format:\n" +
                    "%name_of_variable%=%numeric_value%.");
            System.exit(-1);
        }

        return variables;
    }

    /**
     * By given tokens of the formula and the variables,
     * calculates the result for them.
     *
     * @param formulaTokens array which stores tokens of the formula,
     *                      e.g. 21+2-1 represented as [21, +, 2, -, 1]
     * @param variables hash map with variables and their corresponded values
     * @return the result of the expression
     */
    public double calculate(String[] formulaTokens, HashMap<String, Double> variables) {

        // given infix form converted to postfix
        String[] postfixExpressionTokens
                = convertToRpn(replaceVariables(formulaTokens, variables));
        Stack<Double> operandStack = new Stack<>();

        // Debug message
        System.out.println("Postfix form: " + Arrays.asList(postfixExpressionTokens));

        for (String token : postfixExpressionTokens) {
            if (isCharOperator(token.charAt(0)) && token.length() == 1) {

                // Unary minus at the beginning of the expression
                if (operandStack.size() == 1) {
                    operandStack.push(operandStack.pop() * (-1));
                    continue;
                }

                double num1 = operandStack.pop();
                double num2 = operandStack.pop();

                operandStack.push(getResultByOperator(num1, num2, token));
            } else if (isStringFunction(token))  {
                double num = operandStack.pop();

                operandStack.push(getResultByFunction(num, token));
            }
            else {
                operandStack.push(Double.parseDouble(token));
            }
        }

        return operandStack.pop();
    }


    /**
     * By given two numbers and the binary operator,
     * returns the result of the action
     * this operator is responsible for.
     *
     * @param num1 first number
     * @param num2 second number
     * @param operator action operator (binary)
     * @return the result of the action
     */
    private double getResultByOperator(double num1, double num2, String operator) {
        switch (operator) {
            case "+":
                return num2 + num1;
            case "-":
                return num2 - num1;
            case "*":
                return num2 * num1;
            case "/":
                if (num1 == 0) {  // Prevent division by zero
                    System.out.println ("Error: Division by zero was detected.");
                    System.exit (-1);
                }

                return num2 / num1;
            case "^":
                return Math.pow(num2, num1);
            default:
                return 0.0;
        }
    }

    /**
     * By given one number and the function name,
     * returns the result of the function
     * where argument is the provided number.
     *
     * @param num number
     * @param function name of the function
     * @return result for the given number and formula
     */
    private double getResultByFunction(double num, String function) {
        boolean containsMinus = function.contains("-");
        double result = 0.0;

        if (containsMinus) {
            System.out.println(function);
            function = function.replaceAll("-", "");
        }

        switch (function) {
            case "sin":
                result = Math.sin(num);
                break;
            case "cos":
                result = Math.cos(num);
                break;
            case "tan":
                result = Math.tan(num);
                break;
            case "atan":
                result = Math.atan(num);
                break;
            case "log10":
                result = Math.log10(num);
                break;
            case "log2":
                result = Math.log(num) / Math.log(2);
                break;
            case "sqrt":
                result = Math.sqrt(num);
        }

        return !containsMinus ? result : result * (-1);
    }

    /**
     * Tokenizes the given formula/math expression,
     * e.g 21+2-1 will be represented as an array [21, +, 2, -, 1]
     *
     * @param formula given formula/math expression
     * @return an array of tokens
     */
    public String[] tokenizeExpression(String formula) {

        // Formula with removed redundant spaces
        String normalizedFormula = formula.replaceAll("\\s","");

        StringBuilder operandBuilder = new StringBuilder();
        LinkedList<String> operandList = new LinkedList<>();

        for (int i = 0; i < normalizedFormula.length(); i++) {
            char currentChar = normalizedFormula.charAt(i);

            if (isCharPartOfNumber(currentChar, normalizedFormula, i)) {

                // If char is anything related to number
                operandBuilder.append(currentChar);
            } else if (Character.isLetter(currentChar)) {

                // If char is variable or function
                operandBuilder.append(currentChar);
            } else if (isCharOperator(currentChar) || isCharParenthesis(currentChar)) {
                if (operandBuilder.length() != 0)
                    operandList.add(operandBuilder.toString());

                operandList.add(String.valueOf(currentChar));
                operandBuilder = new StringBuilder();
            }
        }

        // Put the remaining operand into the list */
        if (operandBuilder.length() != 0)
            operandList.add(operandBuilder.toString());

        return operandList.toArray(new String[0]);
    }

    /**
     * Returns true if the character at the given
     * index is an unary operator.
     *
     * @param formula math expression
     * @param index index of the character
     * @return true if the character is an unary operator
     */
    private boolean isCharUnaryOperator(String formula, int index) {

        // unary operator at the beginning of the formula
        if (index == 0) {
            return formula.charAt(index) == '-';
        }
        char previousChar = formula.charAt(index - 1);

        return (formula.charAt(index) == '-' && (previousChar == '(' || isCharOperator(previousChar)));
    }

    /**
     * Returns true if the given character
     * is an operator.
     *
     * @param ch given character
     * @return true if the given character is an operator
     */
    private boolean isCharOperator(char ch) {
        return operatorList.containsKey(ch);
    }

    /**
     * Returns true if the given character
     * is a parenthesis
     *
     * @param ch given character
     * @return true if the given character is a parenthesis
     */
    private boolean isCharParenthesis(char ch) {
        return (ch == '(' || ch == ')');
    }

    /**
     * Returns true if the given char
     * is anything related to the number
     * (i.g. digit, unary minus or dot)
     *
     * @param givenChar current character in the iteration
     * @param formula initial formula
     * @param index current index in the iteration
     * @return true if the given char
     *         is anything related to the number
     */
    private boolean isCharPartOfNumber(char givenChar, String formula, int index) {
        return (Character.isDigit(givenChar)
                || isCharUnaryOperator(formula, index)
                || givenChar == '.');
    }

    /**
     * Replaces variables with their corresponded values
     * for the given math expression represented as the list
     * of operands.
     *
     * @param operandArr array which contains all of the operands
     *                   of the expression
     * @param variables map which contains variable names
     *                  and corresponded values for them
     * @return a string array with the inserted values
     */
    private String[] replaceVariables(String[] operandArr, HashMap<String, Double> variables) {

        for (int i = 0; i < operandArr.length; i++) {
            for (String var : variables.keySet()) {
                if (operandArr[i].equals(var)) {
                    operandArr[i] = String.valueOf(variables.get(var));
                } else if (operandArr[i].equals("-" + var)) {                   // negative value of variable
                    operandArr[i] = String.valueOf(variables.get(var) * (-1));
                }
            }

            /* In mathematics, letter 'e' usually represents Euler's number,
             * thus program replaces it with this number's value.
             * (In case if it isn't overwritten with the new one.)
             */
            if (operandArr[i].equals("e") && !variables.containsKey("e")) {
                operandArr[i] = String.valueOf(Math.E);
            }
        }

        return operandArr;
    }

    /**
     * Converts infix form to postfix,
     * also knows as Reverse Polish Notation (RPN),
     * using Shunting-yard algorithm.
     *
     * @param formulaTokens math expression operands/tokens
     *                      stored in the array
     * @return Previously given infix form as a string in RPN
     */
    private String[] convertToRpn(String[] formulaTokens) {

        // While there are tokens to be read
        for (String token : formulaTokens) {
            if (isStringNumber(token)) {
                outputQueue.add(token);
            } else if (isStringFunction(token)) {
                operatorStack.add(token);
            } else if (isCharOperator(token.charAt(0))) {
                while(canAddOperatorsToOutput(token)) {
                    outputQueue.add(operatorStack.pop());
                }

                operatorStack.push(token);
            } else if (isCharParenthesis(token.charAt(0))) {
                processParenthesis(token);
            }
        }

        // If operator stack not empty, pop everything to output queue
        while(!operatorStack.isEmpty()) {
            outputQueue.add(operatorStack.pop());
        }

        /* If the result contains the left parenthesis,
         * the right one wasn't in the formula, so terminate the program. */
        if (outputQueue.contains("(")) {
            System.out.println ("Error: Mismatched parenthesis.");
            System.exit(-1);
        }

        return outputQueue.toArray(new String[0]);
    }

    /**
     * Processes parenthesis and does
     * an action according to what type it is.
     *
     * @param token the given parenthesis.
     */
    private void processParenthesis(String token) {
        switch (token) {
            case "(":
                operatorStack.push(token);
                break;

            case ")":
                while(!operatorStack.peek().equals("(")) {
                    outputQueue.add(operatorStack.pop());

                    // If the left parenthesis wasn't found, terminate the program
                    if (operatorStack.isEmpty()) {
                        System.out.println ("Error: Mismatched parenthesis.");
                        System.exit(-1);
                    }
                }

                if (operatorStack.peek().equals("("))
                    operatorStack.pop();
                break;
        }
    }

    /**
     * Returns true if it is possible
     * to add new operators to the output list
     *
     * @param operator given operator in the iteration
     * @return true if it is possible
     *         to add new operators.
     */
    private boolean canAddOperatorsToOutput(String operator) {
        return !operatorStack.isEmpty() && (isStringFunction(operatorStack.peek())
                || higherPrecedence(operator.charAt(0), operatorStack.peek().charAt(0))
                && !operator.equals("("));
    }

    /**
     * Returns true if the given
     * string is a number
     *
     * @param str string number
     * @return true if the given
     *         string is a number
     */
    private boolean isStringNumber(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Returns true if the given
     * string is a function
     *
     * @param str string function
     * @return true if the given
     *         string is a function
     */
    private boolean isStringFunction(String str) {
        boolean containsMinus = str.contains("-");

        if (containsMinus) {
            System.out.println(str);
            String strNormalized = str.replaceAll("-", "");
            return Arrays.asList(functionList).contains(strNormalized);
        }

        return Arrays.asList(functionList).contains(str);
    }

    /**
     * Returns true if the stack operator
     * has higher precedence than the current one.
     *
     * @param currOperator current operator in the iteration
     * @param stackOperator operator on top of the stack
     * @return true if the stack operator has higher precedence
     */
    private boolean higherPrecedence(char currOperator, char stackOperator) {

        // check if given operators are valid and exist in map
        if (operatorList.get(currOperator) != null && operatorList.get(stackOperator) != null) {
            return (operatorList.get(currOperator) <= operatorList.get(stackOperator)
                    && currOperator != '^');
        }
        return false;
    }
}
