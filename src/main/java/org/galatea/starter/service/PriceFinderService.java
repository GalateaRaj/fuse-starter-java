package org.galatea.starter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.galatea.starter.domain.AVDay;
import org.galatea.starter.domain.AVStock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


/**
 * A layer for transformation, aggregation, and business required when retrieving data from AlphaVantage.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PriceFinderService implements PriceFinderClient{

  final static String AVQueryURL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&";
  final static String AVApikey = "XNRUM1DDXGFTDL82";

  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private RestTemplate restTemplate;

/**
 * Get all available data regarding requested stock from AlphaVantage and requested N days
 *
 * @return
 */
  public ResponseEntity<AVStock> processText(final String text, int n) {
    String AVGetRequest = (new StringBuilder()).append(AVQueryURL).append("symbol=")
        .append(text).append("&apikey=").append(AVApikey).toString();
    ResponseEntity<AVStock> data = restTemplate.getForEntity(AVGetRequest, AVStock.class);
    data = data.ok(getNDays(data.getBody(), n));
    ResponseEntity dataDesiredFormat = new ResponseEntity(data.getBody().getAVTimeSeries(), data.getStatusCode());

    return dataDesiredFormat;
  }



  public AVStock getNDays (AVStock allData, int n) {
    HashMap<String, AVDay> nDaysOfData = new HashMap();
    for (Map.Entry<String, AVDay> entry : allData.getAVTimeSeries().entrySet()) {
      if (n <= 0) break;
      nDaysOfData.put(entry.getKey(), entry.getValue());
      n -= 1;
    }
    AVStock nStock = new AVStock();
    nStock.setAVTimeSeries(nDaysOfData);
    return nStock;
  }
}
