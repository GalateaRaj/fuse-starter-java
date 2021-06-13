package org.galatea.starter.entrypoint;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.galatea.starter.MvcConfig.APPLICATION_EXCEL;
import static org.galatea.starter.MvcConfig.APPLICATION_EXCEL_VALUE;
import static org.galatea.starter.MvcConfig.TEXT_CSV;
import static org.hamcrest.Matchers.hasXPath;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.common.collect.Sets;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpHeader;
import org.galatea.starter.ASpringTest;
import org.galatea.starter.MessageTranslationConfig;
import org.galatea.starter.MvcConfig;
import org.galatea.starter.domain.SettlementMission;
import org.galatea.starter.entrypoint.exception.EntityNotFoundException;
import org.galatea.starter.service.SettlementService;
import org.galatea.starter.utils.http.converter.ApiErrorConverter;
import org.galatea.starter.utils.http.converter.SettlementMissionCsvConverter;
import org.galatea.starter.utils.http.converter.SettlementMissionXlsxConverter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.accept.ParameterContentNegotiationStrategy;

@Slf4j
@Import({MessageTranslationConfig.class})
@RunWith(JUnitParamsRunner.class)
public class ApiErrorTest extends ASpringTest {

  private static final Long MISSION_ID_1 = 1L;

  private static final Long MISSION_ID_2 = 2L;

  @Value("${mvc.settleMissionPath}")
  private String settleMissionPath;

  @Value("${mvc.getMissionPath}")
  private String getMissionPath;

  @Value("${mvc.getMissionsPath}")
  private String getMissionsPath;

  @Value("${mvc.deleteMissionPath}")
  private String deleteMissionPath;

  @Value("${mvc.updateMissionPath}")
  private String updateMissionPath;

  @MockBean
  private SettlementService mockSettlementService;

  @Autowired
  private SettlementRestController settlementRestController;

  @Before
  public void setup() {
    Map<String, MediaType> mediaTypes = new HashMap<>();
    mediaTypes.put("json", MediaType.APPLICATION_JSON);
    mediaTypes.put("xml", MediaType.APPLICATION_XML);
    mediaTypes.put("csv", MvcConfig.TEXT_CSV);
    mediaTypes.put("xlsx", MvcConfig.APPLICATION_EXCEL);

    ParameterContentNegotiationStrategy parameterContentNegotiationStrategy =
        new ParameterContentNegotiationStrategy(mediaTypes);

    ContentNegotiationManager manager =
        new ContentNegotiationManager(parameterContentNegotiationStrategy);

    RestAssuredMockMvc.standaloneSetup(
        MockMvcBuilders.standaloneSetup(settlementRestController).
            addPlaceholderValue("mvc.settleMissionPath", settleMissionPath).
            addPlaceholderValue("mvc.deleteMissionPath", deleteMissionPath).
            addPlaceholderValue("mvc.updateMissionPath", updateMissionPath).
            addPlaceholderValue("mvc.getMissionsPath", getMissionsPath).
            addPlaceholderValue("mvc.getMissionPath", getMissionPath).
            setContentNegotiationManager(manager).
            setMessageConverters(
                new ApiErrorConverter(),
                new ProtobufHttpMessageConverter(),
                new MappingJackson2HttpMessageConverter(),
                new Jaxb2RootElementHttpMessageConverter(),
                new SettlementMissionCsvConverter(),
                new SettlementMissionXlsxConverter()).
            setControllerAdvice(new RestExceptionHandler()));

  }

  private EntityNotFoundException setupGetMessagesCausesEntityNotFound() {
    List<Long> ids = new ArrayList<>();
    ids.add(MISSION_ID_1);
    ids.add(MISSION_ID_2);

    Sets.SetView<Long> missingMissions = Sets.difference(new HashSet<>(ids),
        Collections.emptySet());

    // cause EntityNotFoundException on GET /settlementEngine/missions?ids={}
    EntityNotFoundException exception =
        new EntityNotFoundException(SettlementMission.class, missingMissions);
    BDDMockito.given(mockSettlementService.findMissions(ids))
        .willThrow(exception);

    return exception;
  }

  @Test
  public void testApiErrorResponse_JSON() {
    EntityNotFoundException exception = setupGetMessagesCausesEntityNotFound();

    given()
        .log().ifValidationFails()
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get("/settlementEngine/missions?ids=1,2&format=json&requestId=1234")
        .then()
        .log().ifValidationFails()
        .statusCode(HttpStatus.NOT_FOUND.value())
        // Content-Type header doesn't match MediaType.APPLICATION_JSON_VALUE
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
        .body("status", is(HttpStatus.NOT_FOUND.name()))
        .body("message", is(exception.toString()));
  }

  @Test
  public void testApiErrorResponse_XML() {
    EntityNotFoundException exception = setupGetMessagesCausesEntityNotFound();

    given()
        .log().ifValidationFails()
        .accept(MediaType.APPLICATION_XML_VALUE)
        .when()
        .get("/settlementEngine/missions?ids=1,2&format=xml&requestId=1234")
        .then()
        .log().ifValidationFails()
        .statusCode(HttpStatus.NOT_FOUND.value())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body("status", is(HttpStatus.NOT_FOUND.name()))
        .body("message", is(exception.toString()));
  }

  @SneakyThrows
  @Test
  public void testApiErrorResponse_CSV() {
    EntityNotFoundException exception = setupGetMessagesCausesEntityNotFound();

    given()
        .log().ifValidationFails()
        .accept(MvcConfig.TEXT_CSV_VALUE)
        .when()
        .get("/settlementEngine/missions?ids=1,2&format=csv&requestId=1234")
        .then()
        .log().ifValidationFails()
        .statusCode(HttpStatus.NOT_FOUND.value())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body("status", is(HttpStatus.NOT_FOUND.name()))
        .body("message", is(exception.toString()));
  }

  @SneakyThrows
  @Test
  public void testApiErrorResponse_XLSX() {
    EntityNotFoundException exception = setupGetMessagesCausesEntityNotFound();

    given()
        .log().ifValidationFails()
        .accept(MvcConfig.APPLICATION_EXCEL_VALUE)
        .when()
        .get("/settlementEngine/missions?ids=1,2&format=xlsx&requestId=1234")
        .then()
        .log().ifValidationFails()
        .statusCode(HttpStatus.NOT_FOUND.value())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body("status", is(HttpStatus.NOT_FOUND.name()))
        .body("message", is(exception.toString()));
  }

  @Configuration
  @Import({SettlementRestController.class})
  @ConditionalOnNotWebApplication
  static class PropertyConfig {

    @Bean
    PropertyPlaceholderConfigurer propertyPlaceholderConfigurer() {
      PropertyPlaceholderConfigurer propertyPlaceholderConfigurer =
          new PropertyPlaceholderConfigurer();
      propertyPlaceholderConfigurer.setLocation(new ClassPathResource("application.properties"));
      return propertyPlaceholderConfigurer;
    }
  }
}
