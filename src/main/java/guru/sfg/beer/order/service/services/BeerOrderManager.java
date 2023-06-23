package guru.sfg.beer.order.service.services;

import com.pulkit.sfgBrewery.events.ValidateBeerOrderResponse;
import guru.sfg.beer.order.service.domain.BeerOrder;

import java.util.UUID;

public interface BeerOrderManager {
  BeerOrder newBeerOrder(BeerOrder beerOrder);
  void sendBeerOrderValidationResult(UUID beerOrderId, Boolean isValid);
}
