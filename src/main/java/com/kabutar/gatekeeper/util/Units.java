package com.kabutar.gatekeeper.util;

import java.util.Map;

public class Units {
    public static class Time{
        public static String SECOND = "S";
        public static String MINUTE = "M";
        public static String HOUR = "H";
        public static String DAY = "D";

        public static  Map<String,Integer> MULTIPLIER = Map.of(
                "S", 1,
                "M", 60,
                "H", 60*60,
                "D", 60*60*24
        );
    }
}
