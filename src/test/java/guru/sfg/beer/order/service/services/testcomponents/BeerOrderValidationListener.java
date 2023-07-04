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

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderValidationListener {

  private final JmsTemplate jmsTemplate;

  @JmsListener(destination = JmsConfig.VALIDATE_ORDER_QUEUE)
  public void listen(Message message) {
    log.debug("### I Ran###");
    ValidateBeerOrderRequest request = (ValidateBeerOrderRequest) message.getPayload();
    jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_RESULT_QUEUE, ValidateBeerOrderResponse
        .builder()
        .orderId(request.getBeerOrderDto().getId())
        .isValid(true).build());
  }
}
