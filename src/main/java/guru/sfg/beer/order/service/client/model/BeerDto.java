package guru.sfg.beer.order.service.client.model;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class BeerDto implements Serializable {
  private UUID beerId;
  private String upc;
  private String beerName;
  private String beerStyle;
  private BigDecimal price;
}
