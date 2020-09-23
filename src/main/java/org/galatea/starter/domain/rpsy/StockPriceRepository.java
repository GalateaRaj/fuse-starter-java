package org.galatea.starter.domain.rpsy;

import java.util.Optional;
import org.galatea.starter.domain.StockData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface StockPriceRepository extends MongoRepository<StockData, String> {

  /**
   * Custom query to find ...stub
   *
   * @param ticker String, stock ticker symbol
   * @param pageable stub
   * @return Slice of the `days` number of stock price documents
   */
  Slice<StockData> findByTicker(String ticker, Pageable pageable);
}
