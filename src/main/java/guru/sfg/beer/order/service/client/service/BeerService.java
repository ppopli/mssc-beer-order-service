package guru.sfg.beer.order.service.client.service;

import guru.sfg.beer.order.service.client.model.BeerDto;

import java.util.Optional;

public interface BeerService {
  Optional<BeerDto> getBeerByUpc(String upc);
}
