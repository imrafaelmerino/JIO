package jio.cli;

public final class ConsolePrinter {

    static String errorResultColor;
    static String successResultColor;
    static String promptColor;

    private ConsolePrinter() {

    }

    public static String printlnResult(String text) {
        synchronizedPrintln(text, successResultColor);
        return null;
    }

    public static String printlnError(String text) {
        synchronizedPrintln(text, errorResultColor);
        return null;
    }

    public static String printlnPrompt(String text) {
        synchronizedPrintln(text, promptColor);
        return null;
    }

    public static String printResult(String text) {
        synchronizedPrint(text, successResultColor);
        return null;
    }

    public static String printError(String text) {
        synchronizedPrint(text, errorResultColor);
        return null;
    }

    public static String printPrompt(String text) {
        synchronizedPrint(text, promptColor);
        return null;
    }


    private static void synchronizedPrintln(String text,
                                            String controlChar
                                           ) {
        synchronized (System.out) {
            if (controlChar != null && !controlChar.isBlank() && !controlChar.isEmpty())
                System.out.println(controlChar + text + ControlChars.RESET.code);
            else
                System.out.println(text);
            ConsoleLogger.log("%s\n".formatted(text));
        }
    }


    private static void synchronizedPrint(String text,
                                          String controlChar
                                         ) {
        synchronized (System.out) {
            if (controlChar != null && !controlChar.isBlank() && !controlChar.isEmpty())
                System.out.print(controlChar + text + ControlChars.RESET.code);
            else
                System.out.print(text);
            ConsoleLogger.log(text);
        }
    }


}
