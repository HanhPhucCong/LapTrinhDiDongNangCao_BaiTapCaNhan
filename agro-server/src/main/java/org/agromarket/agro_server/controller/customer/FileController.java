package org.agromarket.agro_server.controller.customer;

import lombok.RequiredArgsConstructor;
import org.agromarket.agro_server.common.BaseResponse;
import org.agromarket.agro_server.exception.CustomException;
import org.agromarket.agro_server.model.dto.response.CloudinaryResponse;
import org.agromarket.agro_server.service.customer.CloudinaryService;
import org.agromarket.agro_server.util.file.FileUploadUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {
  private final CloudinaryService cloudinaryService;

  @PreAuthorize("hasAnyAuthority('ADMIN', 'CUSTOMER')")
  @PostMapping("/image/upload")
  public ResponseEntity<BaseResponse> uploadImage(
      @RequestParam(name = "file", required = true) MultipartFile file) throws CustomException {
    FileUploadUtil.assertAllowedImage(file);
    final String fileName = FileUploadUtil.getFileName(file.getOriginalFilename());
    final CloudinaryResponse response = cloudinaryService.uploadFile(file, fileName);

    return ResponseEntity.ok(
        new BaseResponse("Tải ảnh lên Coudinary thành công.", HttpStatus.OK.value(), response));
  }

  @PreAuthorize("hasAnyAuthority('ADMIN', 'CUSTOMER')")
  @DeleteMapping("/image/delete")
  public ResponseEntity<BaseResponse> deleteImage(@RequestParam("url") String url)
      throws IOException {
    try {
      cloudinaryService.deleteByUrl(url);
      return ResponseEntity.ok(
          new BaseResponse("Xóa ảnh thành công.", HttpStatus.OK.value(), null));
    } catch (CustomException e) {
      return ResponseEntity.status(e.getStatusCode())
          .body(new BaseResponse(e.getMessage(), e.getStatusCode(), null));
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new BaseResponse(
                  "Lỗi hệ thống. Vui lòng thử lại.",
                  HttpStatus.INTERNAL_SERVER_ERROR.value(),
                  null));
    }
  }

  @PreAuthorize("hasAnyAuthority('ADMIN', 'CUSTOMER')")
  @PostMapping("/video/upload")
  public ResponseEntity<BaseResponse> uploadIVideo(
      @RequestParam(name = "file", required = true) MultipartFile file) throws CustomException {
    FileUploadUtil.assertAllowedVideo(file);
    final String fileName = FileUploadUtil.getFileName(file.getOriginalFilename());
    final CloudinaryResponse response = cloudinaryService.uploadVideo(file, fileName);

    return ResponseEntity.ok(
        new BaseResponse("Tải video lên Coudinary thành công.", HttpStatus.OK.value(), response));
  }
}
