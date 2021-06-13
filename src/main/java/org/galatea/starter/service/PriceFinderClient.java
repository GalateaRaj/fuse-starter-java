package org.galatea.starter.service;

import org.galatea.starter.domain.AVStock;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * An interface to retreive stock information from AlphaVantage
 * See https://www.alphavantage.co/documentation/
 */

@FeignClient(name = "AlphaVantage", url = "${spring.rest.priceFinderBasePath}")
public interface PriceFinderClient {


  @GetMapping(path = "/pricefinder")
  @ResponseBody
  ResponseEntity processText(@RequestParam("text") String text,
      @RequestParam("number") int n);


}
