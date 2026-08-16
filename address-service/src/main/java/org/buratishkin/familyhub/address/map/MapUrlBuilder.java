package org.buratishkin.familyhub.address.map;

import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class MapUrlBuilder {

    public String googleByAddress(String address) {
        return "https://www.google.com/maps/search/?api=1&query="
                + encode(address);
    }

    public String googleByCoordinates(double latitude, double longitude) {
        return "https://www.google.com/maps/search/?api=1&query="
                + latitude + "," + longitude;
    }

    public String yandexByAddress(String address) {
        return "https://yandex.ru/maps/?text=" + encode(address);
    }

    public String yandexByCoordinates(double latitude, double longitude) {
        return "https://yandex.ru/maps/?ll="
                + longitude + "," + latitude
                + "&z=16";
    }

    public String twoGisByAddress(String address) {
        return "https://2gis.ru/search/" + encode(address);
    }

    public String twoGisByCoordinates(double latitude, double longitude) {
        return "https://2gis.ru/geo/" + longitude + "," + latitude;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
