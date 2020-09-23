package org.galatea.starter.entrypoint;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import junitparams.JUnitParamsRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.galatea.starter.ASpringTest;
import org.galatea.starter.MyProps;
import org.galatea.starter.service.AlphaVantageService;
import org.galatea.starter.service.PriceRequestService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Slf4j
@WebMvcTest({StockPriceController.class, PriceRequestService.class, AlphaVantageService.class})
@RunWith(JUnitParamsRunner.class)
public class StockPriceControllerTest extends ASpringTest {

  @Autowired
  private MockMvc mvc;

  @MockBean
  private RestTemplate mockRestTemplate;

  @Autowired
  private PriceRequestService priceRequestService;

  @Autowired
  private AlphaVantageService alphaVantageService;

  @Test
  public void testEndPointTsla() throws Exception {
    ClassLoader classLoader = getClass().getClassLoader();
    InputStream alphaVantageResp = classLoader
        .getResourceAsStream("tsla.json");
    String tslaUrl = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=tsla" +
        "&outputsize=compact&apikey=" + MyProps.apiKey;
    assert alphaVantageResp != null;
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode responseTree = objectMapper.readTree(alphaVantageResp);
    String body = objectMapper.writeValueAsString(responseTree);
    ResponseEntity<String> tslaResponse = new ResponseEntity<>(body, HttpStatus.OK);
    given(mockRestTemplate.getForEntity(tslaUrl, String.class)).willReturn(tslaResponse);


    InputStream resource = classLoader
        .getResourceAsStream("stockOutputTsla.json");
    assert resource != null;
    JsonNode jn = objectMapper.readTree(resource);
    //standardized to remove any potential mistakes in format
    String formattedJson = objectMapper.writeValueAsString(jn);
    log.info("formatted:\n" + formattedJson);
    mvc.perform(get("/prices")
        .param("ticker", "tsla")
        .param("days", "1")
        .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(MockMvcResultMatchers.content().json(formattedJson));
  }

  @Test
  public void testEndPointAmd() throws Exception {
    ClassLoader classLoader = getClass().getClassLoader();
    InputStream alphaVantageResp = classLoader
        .getResourceAsStream("amd.json");
    String amdUrl = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=amd" +
        "&outputsize=compact&apikey=" + MyProps.apiKey;
    assert alphaVantageResp != null;
    String body = new String(alphaVantageResp.readAllBytes());
    ResponseEntity<String> amdResponse = new ResponseEntity<>(body, HttpStatus.OK);
    given(mockRestTemplate.getForEntity(amdUrl, String.class)).willReturn(amdResponse);

    ObjectMapper objectMapper = new ObjectMapper();
    InputStream resource = classLoader
        .getResourceAsStream("stockOutputAmd.json");
    assert resource != null;
    String tree = new String(resource.readAllBytes());
    log.debug(tree);
    JsonNode jn = objectMapper.readTree(tree);
    String formattedJson = objectMapper.writeValueAsString(jn);
    log.info("formatted:\n" + formattedJson);
    mvc.perform(get("/prices")
        .param("ticker", "amd")
        .param("days", "3")
        .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(MockMvcResultMatchers.content().json(formattedJson));
  }

  @Test
  public void testEndPointNvda() throws Exception {
    ClassLoader classLoader = getClass().getClassLoader();
    InputStream alphaVantageResp = classLoader
        .getResourceAsStream("nvda.json");
    String nvdaUrl = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=nvda" +
        "&outputsize=compact&apikey=" + MyProps.apiKey;
    assert alphaVantageResp != null;
    String body = new String(alphaVantageResp.readAllBytes());
    ResponseEntity<String> nvdaResponse = new ResponseEntity<>(body, HttpStatus.OK);
    given(mockRestTemplate.getForEntity(nvdaUrl, String.class)).willReturn(nvdaResponse);

    ObjectMapper objectMapper = new ObjectMapper();
    InputStream resource = classLoader
        .getResourceAsStream("stockOutputNvda.json");
    assert resource != null;
    String tree = new String(resource.readAllBytes());
    log.debug(tree);
    JsonNode jn = objectMapper.readTree(tree);
    String formattedJson = objectMapper.writeValueAsString(jn);
    log.info("formatted:\n" + formattedJson);
    mvc.perform(get("/prices")
        .param("ticker", "nvda")
        .param("days", "5")
        .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(MockMvcResultMatchers.content().json(formattedJson));
  }

  @Test
  public void testEndPointIntc() throws Exception {
    ClassLoader classLoader = getClass().getClassLoader();
    InputStream alphaVantageResp = classLoader
        .getResourceAsStream("intc.json");
    String intcUrl = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=intc" +
        "&outputsize=compact&apikey=" + MyProps.apiKey;
    assert alphaVantageResp != null;
    String body = new String(alphaVantageResp.readAllBytes());
    ResponseEntity<String> intcResponse = new ResponseEntity<>(body, HttpStatus.OK);
    given(mockRestTemplate.getForEntity(intcUrl, String.class)).willReturn(intcResponse);

    ObjectMapper objectMapper = new ObjectMapper();
    InputStream resource = classLoader
        .getResourceAsStream("stockOutputIntc.json");
    assert resource != null;
    String tree = new String(resource.readAllBytes());
    log.debug(tree);
    JsonNode jn = objectMapper.readTree(tree);
    String formattedJson = objectMapper.writeValueAsString(jn);
    log.info("formatted:\n" + formattedJson);
    ResultMatcher resultMatcher = MockMvcResultMatchers.content().json(formattedJson);
    mvc.perform(get("/prices")
        .param("ticker", "intc")
        .param("days", "7")
        .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(resultMatcher);
  }
}
