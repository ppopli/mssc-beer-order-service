package guru.sfg.beer.order.service.sm.actions;

import com.pulkit.sfgBrewery.events.AllocateBeerOrderFailureEvent;
import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.sm.BeerOrderStateMachineConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AllocateOrderFailureAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {
    private final JmsTemplate jmsTemplate;
    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {
        String orderId =
                (String) stateContext.getMessage().getHeaders().get(BeerOrderStateMachineConfig.BEER_ORDER_ID_HEADER);
       if( stateContext.getMessage().getPayload().equals(BeerOrderEventEnum.ALLOCATION_NO_INVENTORY)) {
           log.error("[OrderId={}] Compensating Txn for partial order allocation.", orderId);
        } else {
           log.error("[OrderId={}] Compensating Txn for failed order allocation.", orderId);
        }

       jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_FAILURE_QUEUE,
               AllocateBeerOrderFailureEvent
                       .builder()
                       .orderId(UUID.fromString(orderId))
                       .build());
    }
}
