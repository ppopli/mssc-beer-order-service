package com.pulkit.sfgBrewery.events;

import com.pulkit.sfgBrewery.model.BeerDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BeerEvent implements Serializable {

  static final long serialVersionUID = 498880619606414429L;
  private BeerDto beerDto;
}
