package org.galatea.starter.entrypoint;

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

//sample call: 'http://localhost:8080/pricefinder?text=IBM&n=10'

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


  @Autowired
  private PriceFinderService priceFinderService;

/**
  Send the received text to the PriceFinderService to be processed into a GET request for AV.
  @return a String containing AV's response
 */
// @GetMapping to link http GET request to this method
// @RequestParam to take a parameter from the url
  @GetMapping(value = "${webservice.priceFinderPath}", produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity priceFinderEndpoint(
      @RequestParam(value = "text") final String text,
      @RequestParam(value = "n") final int n){

    return priceFinderService.processText(text, n);
  }
}
