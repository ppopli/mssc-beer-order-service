package guru.sfg.beer.order.service.services;

import com.pulkit.sfgBrewery.model.BeerOrderDto;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.sm.BeerOrderStateChangeInterceptor;
import guru.sfg.beer.order.service.sm.BeerOrderStateMachineConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BeerOrderManagerImpl implements BeerOrderManager {
  private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachineFactory;
  private final BeerOrderRepository beerOrderRepository;

  private final BeerOrderStateChangeInterceptor beerOrderStateChangeInterceptor;

  @Transactional
  @Override
  public BeerOrder newBeerOrder(BeerOrder beerOrder) {
    beerOrder.setId(null);
    beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);
    BeerOrder savedBeerOrder = beerOrderRepository.saveAndFlush(beerOrder);
    sendBeerOrderEvent(savedBeerOrder, BeerOrderEventEnum.VALIDATE_ORDER);
    return savedBeerOrder;
  }

  @Override
  @Transactional
  public void sendBeerOrderValidationResult(UUID beerOrderId, Boolean isValid) {
    BeerOrder beerOrder = beerOrderRepository.findById(beerOrderId)
        .orElseThrow(() -> new RuntimeException("BeerOrder not found"));
    if (isValid) {
      sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_PASSED);
      BeerOrder validatedBeerOrder = beerOrderRepository.findOneById(beerOrderId);
      sendBeerOrderEvent(validatedBeerOrder, BeerOrderEventEnum.ALLOCATE_ORDER);
    } else {
      sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_FAILED);
    }
  }

  @Override
  @Transactional
  public void beerOrderAllocationPassed(BeerOrderDto beerOrderDto) {
    BeerOrder beerOrder = beerOrderRepository.findOneById(beerOrderDto.getId());
    sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_SUCCESS);
    updateAllocateQty(beerOrderDto, beerOrder);
  }

  @Override
  @Transactional
  public void beerOrderAllocationFailed(BeerOrderDto beerOrderDto) {
    BeerOrder beerOrder = beerOrderRepository.findOneById(beerOrderDto.getId());
    sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_FAILED);
  }

  @Override
  @Transactional
  public void beerOrderAllocationPendingInventory(BeerOrderDto beerOrderDto) {
    BeerOrder beerOrder = beerOrderRepository.findOneById(beerOrderDto.getId());
    sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);
    updateAllocateQty(beerOrderDto, beerOrder);
  }

  @Override
  public void beerOrderPickedUp(UUID id) {
    Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(id);
    beerOrderOptional.ifPresentOrElse(beerOrder -> {
      // do processing
      sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.BEERORDER_PICKED_UP);
    }, () -> log.error("Beer Order not found " + id) );
  }

  private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEventEnum eventEnum) {
    StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = build(beerOrder);
    sm.sendEvent(MessageBuilder.withPayload(eventEnum).setHeader(BeerOrderStateMachineConfig.BEER_ORDER_ID_HEADER,
        beerOrder.getId().toString()).build());
  }

  private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> build(BeerOrder beerOrder) {
    StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm =
        stateMachineFactory.getStateMachine(beerOrder.getId());

    sm.stop();
    sm.getStateMachineAccessor()
        .doWithAllRegions(sma -> {
          sma.resetStateMachine(new DefaultStateMachineContext<>(beerOrder.getOrderStatus(),
              null, null, null));
          sma.addStateMachineInterceptor(beerOrderStateChangeInterceptor);
        });
    sm.start();
    return sm;
  }

  private void updateAllocateQty(BeerOrderDto beerOrderDto, BeerOrder beerOrder) {
    BeerOrder allocatedOrder =  beerOrderRepository.findOneById(beerOrderDto.getId());

    allocatedOrder.getBeerOrderLines().forEach(beerOrderLine -> {
      beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
        if(beerOrderLine.getId().equals(beerOrderLineDto.getId())) {
          beerOrderLine.setQuantityAllocated(beerOrderLineDto.getQuantityAllocated());
        }
      });
    });
    beerOrderRepository.saveAndFlush(beerOrder);
  }
}
