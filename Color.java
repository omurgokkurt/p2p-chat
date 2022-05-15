package com.company;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Color {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public List<String> colors;
    public Iterator<String> iterator;

    public Color() {
        colors = new ArrayList<>();
        colors.add(ANSI_BLUE);  colors.add(ANSI_RED); colors.add(ANSI_GREEN); colors.add(ANSI_PURPLE); colors.add(ANSI_CYAN); colors.add(ANSI_YELLOW); colors.add(ANSI_BLACK );
        this.iterator = colors.iterator();

    }

    public String assignColor() {
        if (this.iterator.hasNext()) {
            return iterator.next();
        } else {
            this.iterator = colors.iterator();
            return assignColor();
        }
    }

    public static String red(String s) {
        return ANSI_RED + s + ANSI_RESET;
    }

    public static String blue(String s) {
        return ANSI_BLUE + s + ANSI_RESET;
    }

    public static String yellow(String s) {
        return ANSI_YELLOW + s + ANSI_RESET;
    }

    public static String green(String s) {
        return ANSI_GREEN + s + ANSI_RESET;
    }

}
