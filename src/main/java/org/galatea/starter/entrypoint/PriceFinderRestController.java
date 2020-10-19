package org.galatea.starter.entrypoint;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.aspect4log.Log;
import net.sf.aspect4log.Log.Level;
import org.galatea.starter.domain.AVStock;
import org.galatea.starter.service.PriceFinderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

//sample call: 'http://localhost:8080/pricefinder?text=IBM&quantity=10'

/**
 * REST Controller that listens to http endpoints and allows the caller to send text to be
 * processed.
 * Should process GET requests for data regarding stock ticker
 */
@RequiredArgsConstructor
@Slf4j
@Log(enterLevel = Level.INFO, exitLevel = Level.INFO)
@RestController
public class PriceFinderRestController extends BaseRestController {

  @NonNull
  private PriceFinderService priceFinderService;

/**
  Send the received text to the PriceFinderService to be processed into a GET request for AV.
  @return a String containing AV's response
 */
  @GetMapping(value = "${webservice.priceFinderPath}", produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity priceFinderEndpoint(
      @RequestParam(value = "text") final String text,
      @RequestParam(value = "number-of-days") final int numberOfDays){

    return priceFinderService.getPriceInformation(text, numberOfDays);
  }
}
