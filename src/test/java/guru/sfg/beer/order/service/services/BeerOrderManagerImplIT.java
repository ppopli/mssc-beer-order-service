package guru.sfg.beer.order.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.pulkit.sfgBrewery.model.BeerDto;
import guru.sfg.beer.order.service.client.service.impl.BeerServiceImpl;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderLine;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.domain.Customer;
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
import org.springframework.test.context.TestPropertySource;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.github.jenspiegsa.wiremockextension.ManagedWireMockServer.with;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
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
      //todo: Allocated Status
      Assertions.assertEquals(BeerOrderStatusEnum.ALLOCATION_PENDING, foundOrder.getOrderStatus());
    });
    BeerOrder savedBeerOrder2 = beerOrderRepository.findById(savedBeerOrder.getId()).get();
    Assertions.assertNotNull(savedBeerOrder2);
    Assertions.assertEquals(BeerOrderStatusEnum.ALLOCATED,  savedBeerOrder2.getOrderStatus());
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
