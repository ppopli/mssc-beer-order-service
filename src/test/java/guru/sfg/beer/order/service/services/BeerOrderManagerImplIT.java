package guru.sfg.beer.order.service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.pulkit.sfgBrewery.events.AllocateBeerOrderFailureEvent;
import com.pulkit.sfgBrewery.model.BeerDto;
import guru.sfg.beer.order.service.client.service.impl.BeerServiceImpl;
import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.domain.*;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.repositories.CustomerRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.github.jenspiegsa.wiremockextension.ManagedWireMockServer.with;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;

@ExtendWith(WireMockExtension.class)
@SpringBootTest
@TestPropertySource(
    properties = {
        "sfgBrewery.beer.service.host=http://localhost:8083"
    }
)
public class BeerOrderManagerImplIT {
  @Autowired
  private BeerOrderManager beerOrderManager;

  @Autowired
  private BeerOrderRepository beerOrderRepository;

  @Autowired
  private CustomerRepository customerRepository;

  @Autowired
  private WireMockServer wireMockServer;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private JmsTemplate jmsTemplate;

  private Customer testCustomer;

  UUID beerId = UUID.randomUUID();

  @TestConfiguration
  static class RestTemplateBuilderProvider {
    @Bean(destroyMethod = "stop")
    public WireMockServer wireMockServer() {
      WireMockServer server = with(wireMockConfig().port(8083));
      server.start();
      return server;
    }
  }

  @BeforeEach
  void setUp() {
    testCustomer = Customer.builder().customerName("Test customer").build();
    customerRepository.save(testCustomer);
  }

  @Test
  @SneakyThrows
  void testNewToAllocated() {
    BeerDto beerDto = BeerDto.builder()
        .id(beerId)
        .upc("12345")
        .build();
    wireMockServer.stubFor(get(BeerServiceImpl.GET_BEER_BY_UPC_URI.replace("{upc}", "12345"))
        .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));
    BeerOrder beerOrder = createBeerOrder();
    BeerOrder savedBeerOrder =  beerOrderManager.newBeerOrder(beerOrder);
    await().untilAsserted(() -> {
      BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
      Assertions.assertEquals(BeerOrderStatusEnum.ALLOCATED, foundOrder.getOrderStatus());
    });
    BeerOrder savedBeerOrder2 = beerOrderRepository.findById(savedBeerOrder.getId()).get();
    Assertions.assertNotNull(savedBeerOrder2);
    Assertions.assertEquals(BeerOrderStatusEnum.ALLOCATED,  savedBeerOrder2.getOrderStatus());
  }

  @Test
  @SneakyThrows
  void testNewToPickedUp() {
    BeerDto beerDto = BeerDto.builder()
            .id(beerId)
            .upc("12345")
            .build();
    wireMockServer.stubFor(get(BeerServiceImpl.GET_BEER_BY_UPC_URI.replace("{upc}", "12345"))
            .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));
    BeerOrder beerOrder = createBeerOrder();
    BeerOrder savedBeerOrder =  beerOrderManager.newBeerOrder(beerOrder);
    await().untilAsserted(() -> {
      BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
      Assertions.assertEquals(BeerOrderStatusEnum.ALLOCATED, foundOrder.getOrderStatus());
    });
    beerOrderManager.beerOrderPickedUp(savedBeerOrder.getId());
    await().untilAsserted(() -> {
      BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
      Assertions.assertEquals(BeerOrderStatusEnum.PICKED_UP, foundOrder.getOrderStatus());
    });

    BeerOrder pickedUpOrder = beerOrderRepository.findById(beerOrder.getId()).get();
    Assertions.assertEquals(BeerOrderStatusEnum.PICKED_UP, pickedUpOrder.getOrderStatus());
  }

  @Test
  public void testValidationFailed() throws JsonProcessingException {
    BeerDto beerDto = BeerDto.builder()
            .id(beerId)
            .upc("12345")
            .build();
    wireMockServer.stubFor(get(BeerServiceImpl.GET_BEER_BY_UPC_URI.replace("{upc}", "12345"))
            .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));
    BeerOrder beerOrder = createBeerOrder();
    beerOrder.setCustomerRef("fail-validation");
    beerOrderManager.newBeerOrder(beerOrder);
    await().untilAsserted(() -> {
      BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
      Assertions.assertEquals(BeerOrderStatusEnum.VALIDATION_EXCEPTION, foundOrder.getOrderStatus());
    });
  }

  @Test
  public void testAllocationFailed() throws JsonProcessingException {
    BeerDto beerDto = BeerDto.builder()
            .id(beerId)
            .upc("12345")
            .build();
    wireMockServer.stubFor(get(BeerServiceImpl.GET_BEER_BY_UPC_URI.replace("{upc}", "12345"))
            .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));
    BeerOrder beerOrder = createBeerOrder();
    beerOrder.setCustomerRef("fail-allocation");
    BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);
    await().untilAsserted(() -> {
      BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
      Assertions.assertEquals(BeerOrderStatusEnum.ALLOCATION_EXCEPTION, foundOrder.getOrderStatus());
    });

    AllocateBeerOrderFailureEvent failureEvent =
            (AllocateBeerOrderFailureEvent) jmsTemplate.receiveAndConvert(JmsConfig.ALLOCATE_FAILURE_QUEUE);

    Assertions.assertNotNull(failureEvent);
    assertThat(failureEvent.getOrderId()).isEqualTo(savedBeerOrder.getId());
  }

  @Test
  public void testPartialAllocation() throws JsonProcessingException {
    BeerDto beerDto = BeerDto.builder()
            .id(beerId)
            .upc("12345")
            .build();
    wireMockServer.stubFor(get(BeerServiceImpl.GET_BEER_BY_UPC_URI.replace("{upc}", "12345"))
            .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));
    BeerOrder beerOrder = createBeerOrder();
    beerOrder.setCustomerRef("partial-allocation");
    beerOrderManager.newBeerOrder(beerOrder);
    await().untilAsserted(() -> {
      BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
      Assertions.assertEquals(BeerOrderStatusEnum.PENDING_INVENTORY, foundOrder.getOrderStatus());
    });
  }

  public BeerOrder createBeerOrder() {
    BeerOrder beerOrder = BeerOrder.builder().customer(testCustomer).build();
    Set<BeerOrderLine> lines = new HashSet<>();
    lines.add(BeerOrderLine.builder()
            .beerId(beerId)
            .orderQuantity(1)
            .beerOrder(beerOrder)
            .upc("12345")
        .build());
    beerOrder.setBeerOrderLines(lines);
    return beerOrder;
  }
}
