package org.agromarket.agro_server.service.customer.Impl;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.agromarket.agro_server.common.OrderStatus;
import org.agromarket.agro_server.config.vnpay.VNPayConfig;
import org.agromarket.agro_server.exception.CustomException;
import org.agromarket.agro_server.exception.NotFoundException;
import org.agromarket.agro_server.model.dto.request.CheckoutRequest;
import org.agromarket.agro_server.model.dto.response.OrderResponse;
import org.agromarket.agro_server.model.dto.response.PaymentResponse;
import org.agromarket.agro_server.model.entity.*;
import org.agromarket.agro_server.repositories.customer.*;
import org.agromarket.agro_server.service.customer.PaymentService;
import org.agromarket.agro_server.util.mapper.OrderMapper;
import org.agromarket.agro_server.util.vnpay.VNPayUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

  private final VNPayConfig vnpayConfig;
  private final LineItemRepository lineItemRepository;
  private final UserRepository userRepository;
  private final OrderRepository orderRepository;
  private final CartRepository cartRepository;
  private final OrderMapper orderMapper;
  private final ProductRepository productRepository;

  @Transactional
  @Override
  public PaymentResponse createPayment(
      HttpServletRequest request, CheckoutRequest checkoutRequest) {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = (User) authentication.getPrincipal();
    String userId = String.valueOf(user.getId());

    // make order (temporary)
    OrderResponse orderResponse = makeOrder(Long.parseLong(userId), checkoutRequest);

    String vnp_TxnRef = VNPayUtil.getRandomNumber(8);
    String vnp_IpAddr = VNPayUtil.getIpAddress(request);

    Map<String, String> vnp_Params = buildParams(vnp_TxnRef, vnp_IpAddr, orderResponse);

    String paymentUrl = buildPaymentUrl(vnp_Params);

    PaymentResponse paymentResponse = new PaymentResponse();
    paymentResponse.setStatus("Ok");
    paymentResponse.setMessage("Successfully");
    paymentResponse.setURL(paymentUrl);

    return paymentResponse;
  }

  private OrderResponse makeOrder(long userId, CheckoutRequest checkoutRequest) {
    // check
    checkBeforePay(userId, checkoutRequest.getLineItemIds());

    List<LineItem> lineItems = getLineItemsByIds(checkoutRequest.getLineItemIds());
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new NotFoundException("User cannot fount to create Order"));

    // tao Order
    Order order = new Order();
    order.setUser(user);
    order.setLineItems(lineItems);
    order.setShippingAddress(checkoutRequest.getShippingAddress());
    order.setNote(checkoutRequest.getNote());
    order.setTotalAmount(getAmountFromListLineItem(checkoutRequest.getLineItemIds()));
    order.setStatus(OrderStatus.PENDING);
    order.setIsActive(false); // tram thoi off, doi thanh toan

    List<Product> productToSave = new ArrayList<>();
    for (LineItem lineItem : lineItems) {
      lineItem.setOrder(order);

      // tru quantity san pham
      Product product = lineItem.getProduct();
      product.setQuantity(product.getQuantity() - lineItem.getQuantity());
      productToSave.add(product);
    }
    OrderResponse orderResponse = orderMapper.convertToResponse(orderRepository.save(order));
    lineItemRepository.saveAll(lineItems);

    productRepository.saveAll(productToSave);

    return orderResponse;
  }

  private void checkBeforePay(long userId, String lineItemIds) {

    // 1. check xem user nay co dang thanh toan don hang nao khong
    // (mot user chi thanh toan duoc 1 Order cung luc)
    List<Order> processingOrders = orderRepository.findByUserIdAndStatusPending(userId);
    if (!processingOrders.isEmpty()) {
      throw new CustomException("Current user is processing other Order!", 409);
    }

    // 2. check co cart khong
    Cart cart = cartRepository.findByUserId(userId);
    if (cart == null) {
      throw new NotFoundException("Cart not found with userId " + userId);
    }

    // 3. check lineItems co trong cart khong
    List<LineItem> lineItems = getLineItemsByIds(lineItemIds);
    for (LineItem lineItem : lineItems) {
      if (!cart.getLineItems().contains(lineItem)) {
        throw new CustomException("Line item is not in cart!", 404);
      }
      // 4. check san pham con khong
      if (lineItem.getQuantity() > lineItem.getProduct().getQuantity()) {
        String mesaage =
            lineItem.getProduct().getName() + " does not have enough quantity in stock!";
        throw new CustomException(mesaage, 400);
      }
    }
  }

  @Override
  public double getAmountFromListLineItem(String listLineItems) {
    List<LineItem> lineItems = getLineItemsByIds(listLineItems);
    return lineItems.stream().mapToDouble(LineItem::getPrice).sum();
  }

  private List<LineItem> getLineItemsByIds(String listLineItems) {
    List<Integer> lineItemIds =
        Arrays.stream(listLineItems.split(",")).map(Integer::parseInt).toList();

    List<LineItem> lineItems = new ArrayList<>();
    for (Integer lineItemId : lineItemIds) {
      LineItem lineItem = lineItemRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(lineItemId);
      if (lineItem == null) {
        throw new NotFoundException("Line item cannot be found with id: " + lineItemId);
      }
      lineItems.add(lineItem);
    }
    return lineItems;
  }

  @Transactional
  @Override
  public void handlePaymentSuccess(String orderInfo) {
    // nếu vào được tới đây nghĩa là thanh toán thành công
    // -> đổi trạng thái đơn hàng sang thành công
    // nếu ng dùng không thanh toán (tt thất bại): scheduler sẽ tự reset
    log.info("check orderId: {}", orderInfo);
    Order order =
        orderRepository
            .findById(Long.parseLong(orderInfo))
            .orElseThrow(() -> new NotFoundException("Order cannot found!"));
    log.info("Amount: {}", order.getTotalAmount());
    log.info("User id: {}", order.getUser().getId());

    if (!order.getStatus().equals(OrderStatus.PENDING) || order.getIsDeleted()) {
      throw new CustomException("Failed. Order has not pending yet!", 400);
    }

    // change order status
    order.setStatus(OrderStatus.CONFIRMED);
    order.setIsActive(true);
    orderRepository.save(order);

    // xoa cart ra
    for (LineItem lineItem : order.getLineItems()) {
      lineItem.setCart(null);
    }
    lineItemRepository.saveAll(order.getLineItems());
  }

  private Map<String, String> buildParams(
      String vnp_TxnRef, String vnp_IpAddr, OrderResponse orderResponse) {
    Map<String, String> vnp_Params = new HashMap<>();
    vnp_Params.put("vnp_Version", vnpayConfig.getVersion());
    vnp_Params.put("vnp_Command", vnpayConfig.getCommand());
    vnp_Params.put("vnp_TmnCode", vnpayConfig.getTmnCode());

    double amountGet = orderResponse.getTotalAmount();
    long amount = (long) (amountGet * 100L);

    vnp_Params.put("vnp_Amount", String.valueOf(amount));

    vnp_Params.put("vnp_CurrCode", "VND");
    vnp_Params.put("vnp_BankCode", "NCB");
    vnp_Params.put("vnp_OrderType", vnpayConfig.getOrderType());
    vnp_Params.put("vnp_ReturnUrl", vnpayConfig.getReturnUrl());

    vnp_Params.put("vnp_TxnRef", vnp_TxnRef);

    String orderId = String.valueOf(orderResponse.getId());
    vnp_Params.put("vnp_OrderInfo", orderId);

    vnp_Params.put("vnp_Locale", "vn");
    vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

    Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
    String vnp_CreateDate = formatter.format(cld.getTime());
    vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

    cld.add(Calendar.MINUTE, 15);
    String vnp_ExpireDate = formatter.format(cld.getTime());
    vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

    System.out.println(vnp_Params);

    return vnp_Params;
  }

  private String buildPaymentUrl(Map<String, String> vnp_Params) {
    List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
    Collections.sort(fieldNames);

    StringBuilder hashData = new StringBuilder();
    StringBuilder query = new StringBuilder();

    for (String fieldName : fieldNames) {
      String fieldValue = vnp_Params.get(fieldName);
      if (fieldValue != null && !fieldValue.isEmpty()) {
        // Build hash data
        hashData.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8));
        hashData.append('=');
        hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));

        // Build query
        query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8));
        query.append('=');
        query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));

        query.append('&');
        hashData.append('&');
      }
    }

    if (!query.isEmpty()) query.setLength(query.length() - 1);
    if (!hashData.isEmpty()) hashData.setLength(hashData.length() - 1);

    String vnp_SecureHash = VNPayUtil.hmacSHA512(vnpayConfig.getSecretKey(), hashData.toString());
    return vnpayConfig.getPayUrl() + "?" + query.toString() + "&vnp_SecureHash=" + vnp_SecureHash;
  }
}
