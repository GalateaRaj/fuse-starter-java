package org.galatea.starter.entrypoint;

import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.galatea.starter.domain.StockData;
import org.galatea.starter.domain.rpsy.StockPriceRepository;
import org.galatea.starter.service.PriceRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RestController
public class StockPriceController extends BaseRestController {
  @Autowired
  PriceRequestService priceRequestService;

  @Autowired
  StockPriceRepository stockPriceRepository;

  /**
   * Pulls 'ticker' and 'days' params from GET request to '/prices' entrypoint and pass along
   * to alphavantge API.
   * @param ticker String, stock symbol user wants to find the prices of
   * @param days int, the number of days of stock price data to return
   * @return String, JSON repr of the data requested
   */
  @GetMapping(value = "${webservice.quotepath}", produces = {MediaType.APPLICATION_JSON_VALUE})
  public Collection<StockData> quoteEndpoint(@RequestParam(value = "ticker") final String ticker,
      @RequestParam(value = "days") final int days) {
    if (days < 1) {
      throw new IllegalArgumentException(
          String.format("'days' parameter should be greater than 1. Was %d", days));
    }

    List<StockData> processed = priceRequestService.access(ticker.toUpperCase(), days);
    log.info("Returned from PriceRequestService with list of StockData.");
    assert processed != null && !processed.isEmpty(); //this might not be assertable in the future
    return processed;
  }

  @GetMapping(value = "/test", produces = {MediaType.APPLICATION_JSON_VALUE})
  public Collection<StockData> quoteEndpoint() {
    List<StockData> result = stockPriceRepository.findAll();

    return result;
  }
}
