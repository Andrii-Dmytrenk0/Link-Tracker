package com.linktracker.service;

import com.linktracker.dto.DeviceInfo;
import com.linktracker.util.DeviceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua_parser.Client;
import ua_parser.Parser;

import java.util.regex.Pattern;

/**
 * Parses the {@code User-Agent} header into browser, OS and device
 * information using the uap-core regex database (via uap-java).
 */
@Slf4j
@Service
public class UserAgentParsingService {

    private final Parser uaParser = new Parser();

    private static final Pattern BOT_PATTERN = Pattern.compile(
            "bot|crawler|spider|crawling|googlebot|bingbot|yandexbot|duckduckbot|baiduspider|" +
                    "facebookexternalhit|slurp|ia_archiver|semrushbot|ahrefsbot|mj12bot|petalbot|" +
                    "applebot|telegrambot|whatsapp|curl|wget|python-requests|okhttp|headlesschrome",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern TABLET_HINT = Pattern.compile("ipad|tablet|kindle|playbook|nexus 7|nexus 9",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern MOBILE_HINT = Pattern.compile("mobi|iphone|android.*mobile|windows phone",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern TV_HINT = Pattern.compile("smart-tv|smarttv|googletv|appletv|hbbtv|netcast|tizen",
            Pattern.CASE_INSENSITIVE);

    public DeviceInfo parse(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return DeviceInfo.builder()
                    .browser("Unknown")
                    .os("Unknown")
                    .deviceType(DeviceType.UNKNOWN)
                    .bot(false)
                    .build();
        }

        boolean bot = BOT_PATTERN.matcher(userAgent).find();

        try {
            Client client = uaParser.parse(userAgent);

            DeviceType deviceType;
            if (bot) {
                deviceType = DeviceType.BOT;
            } else if (TV_HINT.matcher(userAgent).find()) {
                deviceType = DeviceType.SMART_TV;
            } else if (TABLET_HINT.matcher(userAgent).find()) {
                deviceType = DeviceType.TABLET;
            } else if (MOBILE_HINT.matcher(userAgent).find()) {
                deviceType = DeviceType.MOBILE;
            } else if ("Other".equalsIgnoreCase(client.device.family)) {
                deviceType = DeviceType.DESKTOP;
            } else {
                deviceType = DeviceType.DESKTOP;
            }

            return DeviceInfo.builder()
                    .browser(client.userAgent.family)
                    .browserVersion(versionString(client.userAgent.major, client.userAgent.minor))
                    .os(client.os.family)
                    .osVersion(versionString(client.os.major, client.os.minor))
                    .deviceManufacturer(deviceType == DeviceType.MOBILE || deviceType == DeviceType.TABLET
                            ? client.device.family : null)
                    .deviceModel(client.device.family)
                    .deviceType(deviceType)
                    .bot(bot)
                    .build();
        } catch (Exception ex) {
            log.debug("Failed to parse User-Agent '{}': {}", userAgent, ex.getMessage());
            return DeviceInfo.builder()
                    .browser("Unknown")
                    .os("Unknown")
                    .deviceType(bot ? DeviceType.BOT : DeviceType.UNKNOWN)
                    .bot(bot)
                    .build();
        }
    }

    private String versionString(String major, String minor) {
        if (major == null) {
            return null;
        }
        return minor != null ? major + "." + minor : major;
    }
}
