package org.agromarket.agro_server.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RenewPasswordRequest {
  @NotBlank(message = "Vui lòng nhập mật khẩu")
  @Pattern(
      regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()\\-+]).{8,20}$",
      message =
          "Mật khẩu phải chứa ít nhất 1 chữ số, 1 chữ thường,"
              + " 1 chữ hoa, 1 ký tự đặc biệt và có độ dài từ 8-20 ký tự")
  private String password;

  @NotBlank(message = "Vui lòng nhập lại mật khẩu")
  private String comfirmPassword;

  @NotBlank(message = "Vui lòng nhập mã xác thực khôi phục mật khẩu")
  private String resetPasswordCode;
}
