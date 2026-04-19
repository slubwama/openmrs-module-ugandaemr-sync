package org.openmrs.module.ugandaemrsync;

public class TestEnum {
    public static void main(String[] args) {
        String name = "EXTERNAL_SERVICE_UNAVAILABLE";
        System.out.println("Name: " + name);
        System.out.println("Starts with EXT_: " + name.startsWith("EXT_"));
        System.out.println("Contains TIMEOUT: " + name.contains("TIMEOUT"));
        System.out.println("Contains CONNECTION: " + name.contains("CONNECTION"));
    }
}
