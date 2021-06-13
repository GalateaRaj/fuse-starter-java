package org.galatea.starter.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.galatea.starter.domain.AVDay;
import org.galatea.starter.domain.AVStock;
import org.galatea.starter.domain.PriceFinderStock;
import org.galatea.starter.domain.DateAndPrice;
import org.galatea.starter.domain.PriceFinderResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


/**
 * A layer for transformation, aggregation, and business required when retrieving data from AlphaVantage.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PriceFinderService {

  final static String AVQueryURL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&";
  final static String AVApikey = "XNRUM1DDXGFTDL82";

  @NonNull
  private RestTemplate restTemplate;

/**
 * Get all available data regarding requested stock from AlphaVantage and requested N days
 *
 * @return A ResponseEntity with a body of java objects containing N days price information for the
 * requested stock
 */
  public ResponseEntity getPriceInformation(final String stockName, int numberOfDays) {
    String AVGetRequest = (new StringBuilder()).append(AVQueryURL).append("symbol=")
        .append(stockName).append("&apikey=").append(AVApikey)
        .toString();

      try {
        ResponseEntity<AVStock> data = restTemplate.getForEntity(AVGetRequest, AVStock.class);
        List<String> allDatesUnsorted = new ArrayList(data.getBody().getAVTimeSeries().keySet());
        List<String> allDatesSorted = sortDates(allDatesUnsorted);
        List<String> nDatesSorted = getNDates(allDatesSorted, numberOfDays);
        AVStock nData = getNdata(nDatesSorted, data.getBody());
        TreeMap<String, AVDay> nDataSorted = sortNData(nData.getAVTimeSeries());
        ResponseEntity dataDesiredFormat = data.ok(generateResponseFormat("test", nDataSorted));
        return dataDesiredFormat;
      } catch (IllegalArgumentException e) {
        log.debug("failed to gather data");
      }
    return null;
  }

  public List<String> sortDates (List<String> keys) {
    Collections.sort(keys, Collections.reverseOrder());
    return keys;
  }

  public List<String> getNDates(List<String> allDates, int n) {
    if (n < allDates.size()) {
      List<String> nDates = allDates.subList(0, n);
      return nDates;
    }
    return allDates;
  }

  public AVStock getNdata(List<String> nDates, AVStock data) {
    HashMap<String, AVDay> nDaysOfData = new HashMap<String, AVDay>();
    AVStock result = new AVStock();
    HashMap<String, AVDay> dataMap = new HashMap<>(data.getAVTimeSeries());

    for (String date : nDates) {
      nDaysOfData.put(date, dataMap.get(date));
    }
    result.setAVTimeSeries(nDaysOfData);
    return result;
  }

  public TreeMap<String, AVDay> sortNData (HashMap<String, AVDay> data) {
    TreeMap<String, AVDay> sorted = new TreeMap<>(Collections.reverseOrder());
    sorted.putAll(data);
    return sorted;
  }

  public PriceFinderResponse generateResponseFormat (String metadata, TreeMap<String, AVDay> data) {

    List<DateAndPrice> dsAndPs = new ArrayList<>();
    for (String date : data.keySet()) {
      DateAndPrice x = new DateAndPrice(date, data.get(date).getPrice());
      dsAndPs.add(x);
    }
    PriceFinderStock stock = new PriceFinderStock(metadata, dsAndPs);
    PriceFinderResponse result = new PriceFinderResponse(stock);
    return result;
  }

}
