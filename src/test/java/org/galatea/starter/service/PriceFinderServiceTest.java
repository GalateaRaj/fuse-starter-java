package org.galatea.starter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import org.galatea.starter.ASpringTest;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.galatea.starter.domain.DateAndPrice;
import org.galatea.starter.domain.PriceFinderResponse;


/**
 * tests for PriceFinderService. These involve real get requests to AV
 * */
public class PriceFinderServiceTest extends ASpringTest {

  RestTemplate restTemplate = new RestTemplate();

  @Test (expected = NullPointerException.class)
  public void testUnsuportedInputStock() {
    String p1 = ""; //bad param 1 value
    int p2 = 1; //valid

    PriceFinderService service = new PriceFinderService(restTemplate);
    ResponseEntity result = service.getPriceInformation(p1, p2);
  }

  @Test (expected = NullPointerException.class)
  public void testUnsuportedInputStock2 () {
    String p1 = "hgvjb fvjhbg"; //bad param 1 value
    int p2 = 1; //valid

    PriceFinderService service = new PriceFinderService(restTemplate);
    ResponseEntity result = service.getPriceInformation(p1, p2);
  }


  @Test
  public void testUnsuportedInputNumberOfDays() {
    String p1 = "IBM"; //valid
    int p2 = -1; //bad param 2 value

    PriceFinderService service = new PriceFinderService(restTemplate);

    ResponseEntity result = service.getPriceInformation(p1, p2);
    assertEquals(null, result);
  }

  @Test
  public void testSupportedInput() {
    String p1 = "TSLA";
    int p2 = 4;

    PriceFinderService service = new PriceFinderService(restTemplate);
    ResponseEntity<PriceFinderResponse> result = service.getPriceInformation(p1, p2);
    assertEquals(4, result.getBody().getStock().getPrices().size());

    DateAndPrice last = new DateAndPrice("3000-1-1", 0);
    for (DateAndPrice dAndP : result.getBody().getStock().getPrices()) {
      assertThat(dAndP.getDate().compareTo(last.getDate()) < 0);
      last = dAndP;
    }
  }
}