package guru.sfg.beer.order.service.sm.actions;

import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.sm.BeerOrderStateMachineConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AllocateOrderFailureAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {
        String orderId =
                (String) stateContext.getMessage().getHeaders().get(BeerOrderStateMachineConfig.BEER_ORDER_ID_HEADER);
       if( stateContext.getMessage().getPayload().equals(BeerOrderEventEnum.ALLOCATION_NO_INVENTORY)) {
           log.error("[OrderId={}] Compensating Txn for partial order allocation.", orderId);
        } else {
           log.error("[OrderId={}] Compensating Txn for failed order allocation.", orderId);
        }

    }
}
