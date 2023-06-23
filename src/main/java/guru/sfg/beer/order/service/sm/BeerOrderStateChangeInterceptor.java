package guru.sfg.beer.order.service.sm;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BeerOrderStateChangeInterceptor extends
    StateMachineInterceptorAdapter<BeerOrderStatusEnum, BeerOrderEventEnum> {
  private final BeerOrderRepository beerOrderRepository;
  @Override
  public void preStateChange(State<BeerOrderStatusEnum, BeerOrderEventEnum> state,
                             Message<BeerOrderEventEnum> message,
                             Transition<BeerOrderStatusEnum, BeerOrderEventEnum> transition,
                             StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine) {
      Optional.ofNullable(message).flatMap(msg -> Optional.ofNullable(UUID.class.cast(msg.getHeaders()
        .getOrDefault(BeerOrderStateMachineConfig.BEER_ORDER_ID_HEADER, null)))).ifPresent(orderId -> {
      BeerOrder beerOrder = beerOrderRepository.findById(orderId)
          .orElseThrow(() -> new NoSuchElementException("PaymentId not found"));
        beerOrder.setOrderStatus(state.getId());
      beerOrderRepository.saveAndFlush(beerOrder);
    });
  }
}

