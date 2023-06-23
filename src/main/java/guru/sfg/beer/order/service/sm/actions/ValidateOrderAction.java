package guru.sfg.beer.order.service.sm.actions;

import com.pulkit.sfgBrewery.events.ValidateBeerOrderRequest;
import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.sm.BeerOrderStateMachineConfig;
import guru.sfg.beer.order.service.web.mappers.BeerOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class ValidateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {
  private final BeerOrderMapper beerOrderMapper;
  private final JmsTemplate jmsTemplate;
  private final BeerOrderRepository beerOrderRepository;


  @Override
  public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {
    Optional.ofNullable(stateContext.getMessage())
        .flatMap(msg -> Optional.ofNullable((UUID) msg.getHeaders()
            .getOrDefault(BeerOrderStateMachineConfig.BEER_ORDER_ID_HEADER, null))).ifPresent(orderId -> {
          BeerOrder beerOrder = beerOrderRepository.findOneById(orderId);
          ValidateBeerOrderRequest request = ValidateBeerOrderRequest
              .builder()
              .beerOrderDto(beerOrderMapper.beerOrderToDto(beerOrder))
              .build();
          jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_QUEUE, request);
          log.debug("[validate order msg id ={}] message sent for validate order", orderId);
//          jmsTemplate.send(JmsConfig.VALIDATE_ORDER_QUEUE, new MessageCreator() {
//            @Override
//            public javax.jms.Message createMessage(Session session) throws JMSException {
//              Message message = null;
//              try {
//                message = session.createTextMessage(objectMapper.writeValueAsString(request));
//                message.setStringProperty("_type", "com.pulkit.sfgBrewery.events.ValidateBeerOrderRequest");
//                log.debug("[validate order msg id ={}] message sent for validate order", orderId);
//                return  message;
//              } catch (JsonProcessingException e) {
//                throw new JMSException(e.getMessage());
//              }
//            }
//          });
        });

  }
}
