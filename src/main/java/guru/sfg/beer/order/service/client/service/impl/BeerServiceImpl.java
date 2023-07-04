package guru.sfg.beer.order.service.client.service.impl;

import guru.sfg.beer.order.service.client.model.BeerDto;
import guru.sfg.beer.order.service.client.service.BeerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@Slf4j
public class BeerServiceImpl implements BeerService {
  public static final String GET_BEER_BY_UPC_URI = "/api/v1/beer/upc/{upc}";
  private final String beerServiceHost ;
  private final RestTemplate restTemplate;

  public BeerServiceImpl(@Value("${sfgBrewery.beer.service.host}") String beerServiceHost,
                         RestTemplateBuilder restTemplateBuilder) {
    this.beerServiceHost = beerServiceHost;
    this.restTemplate = restTemplateBuilder.build();
  }

  @Override
  public Optional<BeerDto> getBeerByUpc(String upc) {
    return Optional.ofNullable(restTemplate.getForObject(beerServiceHost + GET_BEER_BY_UPC_URI, BeerDto.class, upc));
  }
}
