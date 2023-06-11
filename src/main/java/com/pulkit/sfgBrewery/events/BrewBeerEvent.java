package com.pulkit.sfgBrewery.events;

import com.pulkit.sfgBrewery.model.BeerDto;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BrewBeerEvent extends BeerEvent {
  public BrewBeerEvent(BeerDto beerDto) {
    super(beerDto);
  }
}
