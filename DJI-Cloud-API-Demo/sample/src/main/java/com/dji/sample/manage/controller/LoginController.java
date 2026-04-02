package com.dji.sample.manage.controller;

import com.dji.sample.common.error.CommonErrorEnum;
import com.dji.sample.manage.model.dto.UserDTO;
import com.dji.sample.manage.model.dto.UserLoginDTO;
import com.dji.sample.manage.service.IUserService;
import com.dji.sdk.common.HttpResultResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static com.dji.sample.component.AuthInterceptor.PARAM_TOKEN;

@RestController
@RequestMapping("${url.manage.prefix}${url.manage.version}")
public class LoginController {

    @Autowired
    private IUserService userService;

    @PostMapping("/login")
    public HttpResultResponse login(@RequestBody UserLoginDTO loginDTO, HttpServletRequest request) {

        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        HttpResultResponse response = userService.userLogin(username, password, loginDTO.getFlag());
        rewriteMqttAddrForClient(response, request.getServerName());
        return response;
    }

    @PostMapping("/token/refresh")
    public HttpResultResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String token = request.getHeader(PARAM_TOKEN);
        Optional<UserDTO> user = userService.refreshToken(token);

        if (user.isEmpty()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return HttpResultResponse.error(CommonErrorEnum.NO_TOKEN.getMessage());
        }

        HttpResultResponse result = HttpResultResponse.success(user.get());
        rewriteMqttAddrForClient(result, request.getServerName());
        return result;
    }

    private void rewriteMqttAddrForClient(HttpResultResponse response, String serverName) {
        if (response == null || response.getData() == null || !StringUtils.hasText(serverName)) {
            return;
        }
        if (!(response.getData() instanceof UserDTO)) {
            return;
        }

        UserDTO user = (UserDTO) response.getData();
        if (!StringUtils.hasText(user.getMqttAddr())) {
            return;
        }

        String mqttAddr = user.getMqttAddr();
        mqttAddr = mqttAddr.replace("://localhost", "://" + serverName)
                .replace("://127.0.0.1", "://" + serverName);
        user.setMqttAddr(mqttAddr);
    }
}
