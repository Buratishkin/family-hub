package org.buratishkin.familyhub.gateway;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GatewayHealthController {
    @GetMapping("/gateway/health")
    public String health() {
        return "OK";
    }
}
