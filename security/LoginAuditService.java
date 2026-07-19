package com.sharkdom.security;

import com.sharkdom.dto.IpInfoResponse;
import com.sharkdom.entity.user.User;
import com.sharkdom.repository.user.UserRepository;
import com.sharkdom.service.email.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua_parser.Client;
import ua_parser.Parser;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAuditService {

    private final IpInfoService ipInfoService;
    private final EmailService emailService;
    private final UserRepository userRepository;

    private final Parser uaParser = new Parser();

    public void handleSuccessfulLogin(String email, HttpServletRequest request) throws Exception {
        log.info("Handling successful login for email: {}", email);
        String ip = IpUtil.getClientIp(request);
        String device = request.getHeader("User-Agent");
        String deviceInfo = parseDevice(device);
        IpInfoResponse ipInfo = ipInfoService.getIpDetails(ip);
        log.info("IP INFO: {}", ipInfo.toString());
        log.info("Client IP: {}", ip);
        log.info("Device IP: {}", device);
        String location = buildLocation(ipInfo);
        log.info("IP location: {}", location);
        String loginTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"))
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm (z)"));
        log.info("Login time: {}", loginTime);
        var optUser = userRepository.findByEmail(email);
        if (optUser.isPresent())
        {
            User user = optUser.get();
            user.setLastLoginTime(Instant.now());
            userRepository.save(user);
        }
        emailService.sendConfirmLoginInvite("login_email_attempt",email,userRepository.findByEmail(email).get().getName(),loginTime,location,deviceInfo,ip);
        log.info("OTP login success email sent to {}", email);
    }

    private String buildLocation(IpInfoResponse ipInfo) {
        if (ipInfo == null) return "Unknown location";

        StringBuilder location = new StringBuilder();

        if (ipInfo.getCity() != null && !ipInfo.getCity().isBlank())
            location.append(ipInfo.getCity());

        if (ipInfo.getRegion() != null && !ipInfo.getRegion().isBlank()) {
            if (location.length() > 0) location.append(", ");
            location.append(ipInfo.getRegion());
        }

        if (ipInfo.getCountry() != null && !ipInfo.getCountry().isBlank()) {
            if (location.length() > 0) location.append(", ");
            location.append(ipInfo.getCountry());
        }

        return location.length() > 0 ? location.toString() : "Unknown location";
    }

    private String parseDevice(String userAgent) {

        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown Device";
        }

        Client client = uaParser.parse(userAgent);

        String browser = client.userAgent.family;
        String os = client.os.family;

        if ("Other".equalsIgnoreCase(browser) && "Other".equalsIgnoreCase(os)) {
            return userAgent; // show raw value instead
        }

        return browser + " on " + os;
    }
}
