package org.buratishkin.familyhub.address.enums;

public enum StreetType {
    STREET("улица"),
    AVENUE("авеню"),
    BOULEVARD("бульвар"),
    LANE("переулок"),
    DRIVE("проезд"),
    ROAD("дорога"),
    HIGHWAY("шоссе"),
    ALLEY("аллея"),
    EMBANKMENT("набережная"),
    SQUARE("площадь"),
    PROSPECT("проспект"),
    OTHER("другое");

    private final String russianName;

    StreetType(String russianName) {
        this.russianName = russianName;
    }

    public String getRussianName() {
        return russianName;
    }
}
