import java.util.Scanner;


public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Калькулятор. Введите выражение (например: 2 + 3 * (4 - 1)^2)");
        System.out.println("Для выхода введите 'exit'.");

        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) break;
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) continue;
            if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("выход")) break;

            try {
                double result = new Parser(line).parse();
                System.out.println("= " + format(result));
            } catch (ArithmeticException | IllegalArgumentException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
        System.out.println("До свидания!");
    }

    /** Убирает лишний ".0" у целых результатов. */
    private static String format(double value) {
        if (value == Math.rint(value) && !Double.isInfinite(value) && Math.abs(value) < 1e15) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    /**
     * Парсер методом рекурсивного спуска.
     * Грамматика:
     *   expression = term { ("+" | "-") term }
     *   term       = unary { ("*" | "/" | "%") unary }
     *   unary      = ("+" | "-") unary | power     (поэтому -2^2 = -4)
     *   power      = primary [ "^" unary ]          (степень правоассоциативна)
     *   primary    = число | константа | функция "(" expression ")" | "(" expression ")"
     */
    static class Parser {
        private final String input;
        private int pos = 0;

        Parser(String input) {
            this.input = input;
        }

        double parse() {
            double value = expression();
            skipSpaces();
            if (pos < input.length()) {
                throw new IllegalArgumentException("неожиданный символ '" + input.charAt(pos) + "'");
            }
            return value;
        }

        private double expression() {
            double value = term();
            while (true) {
                if (match('+')) value += term();
                else if (match('-')) value -= term();
                else return value;
            }
        }

        private double term() {
            double value = unary();
            while (true) {
                if (match('*')) {
                    value *= unary();
                } else if (match('/')) {
                    double divisor = unary();
                    if (divisor == 0) throw new ArithmeticException("деление на ноль");
                    value /= divisor;
                } else if (match('%')) {
                    double divisor = unary();
                    if (divisor == 0) throw new ArithmeticException("деление на ноль");
                    value %= divisor;
                } else {
                    return value;
                }
            }
        }

        private double unary() {
            if (match('+')) return unary();
            if (match('-')) return -unary();
            return power();
        }

        private double power() {
            double base = primary();
            if (match('^')) {
                return Math.pow(base, unary());
            }
            return base;
        }

        private double primary() {
            skipSpaces();
            if (pos >= input.length()) {
                throw new IllegalArgumentException("выражение оборвалось");
            }

            if (match('(')) {
                double value = expression();
                expect(')');
                return value;
            }

            char c = input.charAt(pos);
            if (Character.isDigit(c) || c == '.') {
                return number();
            }
            if (Character.isLetter(c)) {
                return identifier();
            }
            throw new IllegalArgumentException("неожиданный символ '" + c + "'");
        }

        private double number() {
            int start = pos;
            while (pos < input.length()
                    && (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.')) {
                pos++;
            }
            String text = input.substring(start, pos).replace(',', '.');
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("некорректное число '" + text + "'");
            }
        }

        private double identifier() {
            int start = pos;
            while (pos < input.length() && Character.isLetter(input.charAt(pos))) {
                pos++;
            }
            String name = input.substring(start, pos).toLowerCase();

            switch (name) {
                case "pi": return Math.PI;
                case "e":  return Math.E;
            }

            expect('(');
            double arg = expression();
            expect(')');

            switch (name) {
                case "sqrt":
                    if (arg < 0) throw new ArithmeticException("корень из отрицательного числа");
                    return Math.sqrt(arg);
                case "sin": return Math.sin(arg);
                case "cos": return Math.cos(arg);
                case "tan": return Math.tan(arg);
                case "abs": return Math.abs(arg);
                case "ln":
                    if (arg <= 0) throw new ArithmeticException("логарифм от неположительного числа");
                    return Math.log(arg);
                case "log":
                    if (arg <= 0) throw new ArithmeticException("логарифм от неположительного числа");
                    return Math.log10(arg);
                default:
                    throw new IllegalArgumentException("неизвестная функция '" + name + "'");
            }
        }

        private void skipSpaces() {
            while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
                pos++;
            }
        }

        private boolean match(char expected) {
            skipSpaces();
            if (pos < input.length() && input.charAt(pos) == expected) {
                pos++;
                return true;
            }
            return false;
        }

        private void expect(char expected) {
            if (!match(expected)) {
                throw new IllegalArgumentException("ожидался символ '" + expected + "'");


                //fddg
            }
        }
    }
}