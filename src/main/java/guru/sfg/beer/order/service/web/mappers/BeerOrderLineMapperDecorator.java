package guru.sfg.beer.order.service.web.mappers;

import guru.sfg.beer.order.service.client.model.BeerDto;
import guru.sfg.beer.order.service.client.service.BeerService;
import guru.sfg.beer.order.service.domain.BeerOrderLine;
import guru.sfg.beer.order.service.web.model.BeerOrderLineDto;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BeerOrderLineMapperDecorator implements BeerOrderLineMapper {
  private BeerService beerService;
  private BeerOrderLineMapper beerOrderLineMapper;

  @Autowired
  public void setBeerService(BeerService beerService) {
    this.beerService = beerService;
  }

  @Autowired
  public void setBeerOrderLineMapper(BeerOrderLineMapper beerOrderLineMapper) {
    this.beerOrderLineMapper = beerOrderLineMapper;
  }

  @Override
  public BeerOrderLineDto beerOrderLineToDto(BeerOrderLine line) {
    BeerOrderLineDto beerOrderLineDto = beerOrderLineMapper.beerOrderLineToDto(line);
    beerService.getBeerByUpc(line.getUpc()).ifPresent(beerDto -> {
      beerOrderLineDto.setBeerName(beerDto.getBeerName());
      beerOrderLineDto.setBeerStyle(beerDto.getBeerStyle());
      beerOrderLineDto.setBeerId(beerDto.getBeerId());
      beerOrderLineDto.setPrice(beerDto.getPrice());
    });
    return beerOrderLineDto;
  }

  @Override
  public BeerOrderLine dtoToBeerOrderLine(BeerOrderLineDto dto) {
    return beerOrderLineMapper.dtoToBeerOrderLine(dto);
  }
}
