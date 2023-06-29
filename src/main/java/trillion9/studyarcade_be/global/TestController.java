package trillion9.studyarcade_be.global;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.server.WebServer;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Component
@RestController
public class TestController {

    @Autowired
    private WebServerApplicationContext applicationContext;

    @GetMapping("api/time")
    public String getCurrentTime() {
        return "현재 서버 시간 : " + LocalDateTime.now();
    }

    @GetMapping("api/port")
    public String getPortNumber() {
        WebServer webServer = applicationContext.getWebServer();
        int port = webServer.getPort();
        return "현재 사용 중인 서버 포트 번호: " + port;
    }
}
