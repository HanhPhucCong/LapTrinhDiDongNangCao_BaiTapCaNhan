package org.agromarket.agro_server.model.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FavoriteRespone extends BaseResponseDTO {
  private long userId;
  private List<ProductResponse> products;
}
