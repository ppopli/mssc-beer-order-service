package com.pulkit.sfgBrewery.events;

import com.pulkit.sfgBrewery.model.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AllocateBeerOrderResult {
  private BeerOrderDto beerOrderDto;
  private Boolean allocationError = false;
  private Boolean pendingInventory = false;
}
