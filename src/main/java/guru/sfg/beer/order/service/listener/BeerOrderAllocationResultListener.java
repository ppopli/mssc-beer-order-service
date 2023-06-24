package guru.sfg.beer.order.service.listener;

import com.pulkit.sfgBrewery.events.AllocateBeerOrderResult;
import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BeerOrderAllocationResultListener {
  private final BeerOrderManager beerOrderManager;

  @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE)
  public void listen(AllocateBeerOrderResult allocateBeerOrderResult) {
    if (!allocateBeerOrderResult.getAllocationError() && !allocateBeerOrderResult.getPendingInventory()) {
      beerOrderManager.beerOrderAllocationPassed(allocateBeerOrderResult.getBeerOrderDto());
    } else if (!allocateBeerOrderResult.getAllocationError() && allocateBeerOrderResult.getPendingInventory()) {
      beerOrderManager.beerOrderAllocationPendingInventory(allocateBeerOrderResult.getBeerOrderDto());
    } else if (allocateBeerOrderResult.getAllocationError()){
      beerOrderManager.beerOrderAllocationFailed(allocateBeerOrderResult.getBeerOrderDto());
    }
  }
}
