package guru.sfg.beer.order.service.services;

import com.pulkit.sfgBrewery.model.BeerOrderDto;
import guru.sfg.beer.order.service.domain.BeerOrder;

import java.util.UUID;

public interface BeerOrderManager {
  BeerOrder newBeerOrder(BeerOrder beerOrder);
  void sendBeerOrderValidationResult(UUID beerOrderId, Boolean isValid);

  void beerOrderAllocationPassed(BeerOrderDto beerOrderDto);
  void beerOrderAllocationFailed(BeerOrderDto beerOrderDto);
  void beerOrderAllocationPendingInventory(BeerOrderDto beerOrderDto);

    void beerOrderPickedUp(UUID id);
}
