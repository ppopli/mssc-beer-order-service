package guru.sfg.beer.order.service.listener;

import com.pulkit.sfgBrewery.events.ValidateBeerOrderResponse;
import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class BeerOrderValidationResponseListener {
  private final BeerOrderManager beerOrderManager;

  @JmsListener(destination = JmsConfig.VALIDATE_ORDER_RESULT_QUEUE)
  public void listen(ValidateBeerOrderResponse validateBeerOrderResponse) {
    final UUID beerOrderId = validateBeerOrderResponse.getOrderId();
    log.debug("[beerOrderId={}] validation result.", beerOrderId);
    beerOrderManager.sendBeerOrderValidationResult(beerOrderId,
        validateBeerOrderResponse.getIsValid());
  }
}
