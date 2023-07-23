package guru.sfg.beer.order.service.services.testcomponents;

import com.pulkit.sfgBrewery.events.ValidateBeerOrderRequest;
import com.pulkit.sfgBrewery.events.ValidateBeerOrderResponse;
import guru.sfg.beer.order.service.config.JmsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderValidationListener {

  private final JmsTemplate jmsTemplate;

  @JmsListener(destination = JmsConfig.VALIDATE_ORDER_QUEUE)
  public void listen(Message message) {
    boolean isValid = true;
    ValidateBeerOrderRequest request = (ValidateBeerOrderRequest) message.getPayload();

    if (Objects.equals(request.getBeerOrderDto().getCustomerRef(), "fail-validation")) {
      isValid = false;
    }

    if (Objects.equals(request.getBeerOrderDto().getCustomerRef(), "dont-validate")) {
      return;
    }
    jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_RESULT_QUEUE, ValidateBeerOrderResponse
        .builder()
        .orderId(request.getBeerOrderDto().getId())
        .isValid(isValid).build());
  }
}
