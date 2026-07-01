package com.linktracker.service.geo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linktracker.dto.GeoLocationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Default {@link GeoLocationService} implementation backed by the free
 * ip-api.com HTTP API. Swap {@code geo.provider=maxmind} (and provide a
 * corresponding implementation) to use a local MaxMind GeoLite2 database
 * instead -- no other code needs to change.
 */
@Slf4j
@Service
public class IpApiGeoLocationService implements GeoLocationService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public IpApiGeoLocationService(@Value("${geo.ip-api.base-url:http://ip-api.com}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    private static final String FIELDS = "status,message,country,countryCode,regionName,city,timezone,isp,"
            + "lat,lon,mobile,proxy,hosting,query";

    @Override
    public GeoLocationResult lookup(String ip) {
        if (ip == null || ip.isBlank() || isPrivateOrLocal(ip)) {
            return GeoLocationResult.empty();
        }
        try {
            String body = restClient.get()
                    .uri("/json/{ip}?fields={fields}", ip, FIELDS)
                    .retrieve()
                    .body(String.class);

            JsonNode node = objectMapper.readTree(body);
            if (node == null || !"success".equalsIgnoreCase(node.path("status").asText())) {
                return GeoLocationResult.empty();
            }

            boolean isMobile = node.path("mobile").asBoolean(false);
            boolean isProxy = node.path("proxy").asBoolean(false);
            boolean isHosting = node.path("hosting").asBoolean(false);

            return GeoLocationResult.builder()
                    .country(textOrNull(node, "country"))
                    .countryCode(textOrNull(node, "countryCode"))
                    .city(textOrNull(node, "city"))
                    .region(textOrNull(node, "regionName"))
                    .timezone(textOrNull(node, "timezone"))
                    .isp(textOrNull(node, "isp"))
                    .latitude(node.hasNonNull("lat") ? node.path("lat").asDouble() : null)
                    .longitude(node.hasNonNull("lon") ? node.path("lon").asDouble() : null)
                    .networkType(isMobile ? "mobile" : "fixed")
                    .vpnOrProxy(isProxy)
                    .datacenter(isHosting)
                    .build();
        } catch (Exception ex) {
            log.warn("Geolocation lookup failed for IP {}: {}", ip, ex.getMessage());
            return GeoLocationResult.empty();
        }
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private boolean isPrivateOrLocal(String ip) {
        return ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1")
                || ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.16.");
    }
}
