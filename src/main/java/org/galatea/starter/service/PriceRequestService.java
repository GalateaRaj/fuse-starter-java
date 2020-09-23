package org.galatea.starter.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.aspect4log.Log;
import org.galatea.starter.domain.StockData;
import org.galatea.starter.domain.rpsy.StockPriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

/**
 * Front-facing service to request a number of days of share prices for the given stock.
 */
@RequiredArgsConstructor
@Slf4j
@Log
@Service
public class PriceRequestService {
  @Autowired
  private AlphaVantageService alphaVantageService;

  @Autowired
  private StockPriceRepository stockPriceRepository;

  protected List<StockData> externalRequest(final String ticker, final long days,
      final int retrievalNum) {
    return alphaVantageService.access(ticker, days, retrievalNum);
  }

  protected List<StockData> internalRequest(final String ticker, final int days) {
    Slice<StockData> documentSlice = stockPriceRepository
        .findByTicker(ticker,
            PageRequest.of(0, days, Sort.by(Direction.DESC, "date")));
    //checks if the info we're looking for exists in the repo
    if (documentSlice.hasContent()) {
      List<StockData> documents = documentSlice.getContent();
      log.info("Request was serviced internally by MongoDB.");
      return documents;
    } else {
      log.info("Request will be passed to external service.");
      return new ArrayList<>(); //default response that tells caller to poke AlphaVantage for info
    }
  }

  /**
   * Returns a TreeMap of MongoDocument values for each day of share price information
   * mapped to String keys of the corresponding day.
   * Might be less than requested if the information is unavailable,
   * so be sure to check length of the result.
   * @param ticker String, symbol of the stock being queried (case-insensitive)
   * @param days int, number of days to be returned if possible
   * @return TreeMap, date of info as a String key; prices info for that day as MongoDocument values
   */
  public List<StockData> access(final String ticker, final int days) {
    Slice<StockData> mostRecent = stockPriceRepository.findByTicker(ticker,
        PageRequest.of(0, days, Sort.by(Direction.DESC, "date")));
    long daysDifference = days; //default value to grab the entire record, if doesn't exist in db
    if (mostRecent.hasContent()) {
       daysDifference = mostRecent.getContent().get(0).getDate()
           .until(Instant.now(), ChronoUnit.DAYS);
       log.info("Queried Mongo; Most recent data is {} days old.", daysDifference);
    } else {
      log.info("Queried Mongo; No data exists for symbol({})", ticker);
    }

    //check if can get map from internal repo
    if (daysDifference == 0) {
      return internalRequest(ticker, days);
    } else {
      return externalRequest(ticker, daysDifference, days); //query AlphaVantage and serve
    }
  }
}
