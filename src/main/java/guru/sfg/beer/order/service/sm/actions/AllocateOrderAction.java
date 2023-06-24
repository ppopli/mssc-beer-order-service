package guru.sfg.beer.order.service.sm.actions;

import com.pulkit.sfgBrewery.events.AllocateBeerOrderRequest;
import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.sm.BeerOrderStateMachineConfig;
import guru.sfg.beer.order.service.web.mappers.BeerOrderMapper;
import guru.sfg.beer.order.service.web.mappers.BeerOrderMapperImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class AllocateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

  private final BeerOrderMapper beerOrderMapper;
  private final JmsTemplate jmsTemplate;
  private final BeerOrderRepository beerOrderRepository;
  @Override
  public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {
    String beerOrderId = (String) stateContext.getMessage()
        .getHeaders()
        .get(BeerOrderStateMachineConfig.BEER_ORDER_ID_HEADER);
    BeerOrder beerOrder = beerOrderRepository.findOneById(UUID.fromString(beerOrderId));
    AllocateBeerOrderRequest allocateBeerOrderRequest = AllocateBeerOrderRequest
        .builder()
        .beerOrderDto(beerOrderMapper.beerOrderToDto(beerOrder))
        .build();
    jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_QUEUE, allocateBeerOrderRequest);
  }
}
