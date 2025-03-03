package org.agromarket.agro_server.component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.agromarket.agro_server.common.BaseResponse;
import org.agromarket.agro_server.common.ErrorCode;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {

    String message;
    String errorCode;

    if (authException instanceof BadCredentialsException) {
      // sai thông tin đăng nhập
      message =
          "Thông tin đăng nhập không chính xác. Vui lòng kiểm tra lại email và mật khẩu của bạn.";
      errorCode = ErrorCode.INVALID_CREDENTIALS.name();
    } else {
      // hết hạn token
      message = "Thông tin xác thực không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập và thử lại.";
      errorCode = ErrorCode.TOKEN_EXPIRED.name();
    }

    BaseResponse baseResponse =
        new BaseResponse(message, HttpServletResponse.SC_UNAUTHORIZED, errorCode);
    response.setContentType("application/json;charset=UTF-8");

    // Trả về phản hồi chi tiết với mã lỗi và thông điệp
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    response.getWriter().write(baseResponse.toJson());
  }
}
