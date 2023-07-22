package guru.sfg.beer.order.service.services.testcomponents;

import com.pulkit.sfgBrewery.events.AllocateBeerOrderRequest;
import com.pulkit.sfgBrewery.events.AllocateBeerOrderResult;
import guru.sfg.beer.order.service.config.JmsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderAllocationListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
    public void listen(@Payload AllocateBeerOrderRequest request) {
        log.debug("Received message for Beer order allocation");
        jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE,
                AllocateBeerOrderResult
                        .builder()
                        .beerOrderDto(request.getBeerOrderDto())
                        .allocationError(false)
                        .pendingInventory(false)
                        .build());
    }
}
