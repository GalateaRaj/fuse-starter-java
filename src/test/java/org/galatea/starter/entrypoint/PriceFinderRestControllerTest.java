package org.galatea.starter.entrypoint;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;
import junitparams.JUnitParamsRunner;
import lombok.extern.slf4j.Slf4j;
import org.galatea.starter.ASpringTest;
import org.galatea.starter.domain.AVStock;
import org.galatea.starter.service.PriceFinderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

@Slf4j
@WebMvcTest(PriceFinderRestController.class)
@Import(PriceFinderService.class)

/**All tests for controller use a mock response entity for 3 days of data for IBM, 2020-10-21 -> 2020-10-23
 * Tests are assuming that the user would be requesting data for the valid IBM symbol
 * Tests do not asses responses based on invalid symbol input other than no symbol
 * */
@RunWith(JUnitParamsRunner.class)
public class PriceFinderRestControllerTest extends ASpringTest {

  @Autowired
  private MockMvc mvc;

  @MockBean
  private RestTemplate restTemplate;

  @Autowired
  private ObjectMapper objectMapper;


  @Test
  public void testPriceFinderEndpoint() throws Exception {
    final AVStock mockResponseBody = objectMapper.readValue(mockData, AVStock.class);

    when(restTemplate.getForEntity("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=IBM&apikey=XNRUM1DDXGFTDL82",
        AVStock.class))
        .thenReturn(new ResponseEntity(mockResponseBody, HttpStatus.OK));

    this.mvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .get("/pricefinder?text=IBM&number-of-days=1")
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andReturn();
  }

  @Test
  public void testZeroDays() throws Exception {
    final AVStock mockResponseBody = objectMapper.readValue(mockData, AVStock.class);

    when(restTemplate.getForEntity("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=IBM&apikey=XNRUM1DDXGFTDL82",
        AVStock.class))
        .thenReturn(new ResponseEntity<AVStock>(mockResponseBody, HttpStatus.OK));

    this.mvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .get("/pricefinder?text=IBM&number-of-days=0")
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest())
        .andReturn();
  }


  @Test
  public void testOneDay() throws Exception {
    final AVStock mockResponseBody = objectMapper.readValue(mockData, AVStock.class);

    when(restTemplate.getForEntity("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=IBM&apikey=XNRUM1DDXGFTDL82",
        AVStock.class))
      .thenReturn(new ResponseEntity<AVStock>(mockResponseBody, HttpStatus.OK));

    this.mvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .get("/pricefinder?text=IBM&number-of-days=1")
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.stock.prices[0].date", is("2020-10-23")))
        .andExpect(jsonPath("$.stock.prices[0].price", is(116.0000)))
        .andReturn();
  }

  @Test
  public void testAllDaysPrice() throws Exception {
    final AVStock mockResponseBody = objectMapper.readValue(mockData, AVStock.class);
    when(restTemplate.getForEntity("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=IBM&apikey=XNRUM1DDXGFTDL82",
        AVStock.class))
        .thenReturn(new ResponseEntity<AVStock>(mockResponseBody, HttpStatus.OK));

    this.mvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .get("/pricefinder?text=IBM&number-of-days=3")
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.stock.prices[0].price", is(116.0000)))
        .andExpect(jsonPath("$.stock.prices[1].price", is(115.7600)))
        .andExpect(jsonPath("$.stock.prices[2].price", is(115.0600)))
        .andReturn();
  }

  @Test
  public void testNoSymbol() throws Exception {
    when(restTemplate.getForEntity("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=&apikey=XNRUM1DDXGFTDL82",
        AVStock.class))
        .thenReturn(new ResponseEntity<AVStock>(new AVStock(), HttpStatus.BAD_REQUEST));

    this.mvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .get("/pricefinder?text=&number-of-days=3")
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest())
        .andReturn();
  }


  final String mockData = "{\n"
      + "    \"Meta Data\": {\n"
      + "        \"1. Information\": \"Daily Prices (open, high, low, close) and Volumes\",\n"
      + "        \"2. Symbol\": \"IBM\",\n"
      + "        \"3. Last Refreshed\": \"2020-10-23\",\n"
      + "        \"4. Output Size\": \"Compact\",\n"
      + "        \"5. Time Zone\": \"US/Eastern\"\n"
      + "    },\n"
      + "    \"Time Series (Daily)\": {\n"
      + "        \"2020-10-23\": {\n"
      + "            \"1. open\": \"116.5000\",\n"
      + "            \"2. high\": \"116.6200\",\n"
      + "            \"3. low\": \"115.5300\",\n"
      + "            \"4. close\": \"116.0000\",\n"
      + "            \"5. volume\": \"3893362\"\n"
      + "        },\n"
      + "        \"2020-10-22\": {\n"
      + "            \"1. open\": \"115.0000\",\n"
      + "            \"2. high\": \"116.0600\",\n"
      + "            \"3. low\": \"112.9800\",\n"
      + "            \"4. close\": \"115.7600\",\n"
      + "            \"5. volume\": \"7858158\"\n"
      + "        },\n"
      + "        \"2020-10-21\": {\n"
      + "            \"1. open\": \"116.6600\",\n"
      + "            \"2. high\": \"117.6899\",\n"
      + "            \"3. low\": \"114.7900\",\n"
      + "            \"4. close\": \"115.0600\",\n"
      + "            \"5. volume\": \"9755308\"\n"
      + "        }}}";


}