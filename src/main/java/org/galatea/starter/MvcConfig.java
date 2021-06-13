package org.galatea.starter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.galatea.starter.utils.FuseHttpTraceRepository;
import org.galatea.starter.utils.http.converter.ApiErrorConverter;
import org.galatea.starter.utils.http.converter.SettlementMissionCsvConverter;
import org.galatea.starter.utils.http.converter.SettlementMissionXlsxConverter;
import org.galatea.starter.utils.rest.FuseHttpTraceFilter;
import org.springframework.boot.actuate.trace.http.HttpExchangeTracer;
import org.springframework.boot.actuate.trace.http.Include;
import org.springframework.boot.actuate.web.trace.servlet.HttpTraceFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

  public static final MediaType TEXT_CSV = new MediaType("text", "csv");
  public static final String TEXT_CSV_VALUE = "text/csv";

  public static final MediaType APPLICATION_EXCEL = new MediaType("application", "vnd.ms-excel");
  public static final String APPLICATION_EXCEL_VALUE = "application/vnd.ms-excel";

  /**
   * This is used to trace web requests and store that trace info.
   *
   * @return the trace filter
   */
  @Bean
  public HttpTraceFilter httpTraceFilter() {
    return new FuseHttpTraceFilter(fuseHttpTraceRepository(), httpExchangeTracer(),
        path -> path.startsWith("/trace"));
  }

  /**
   * Repository for storing trace info.
   */
  @Bean
  public FuseHttpTraceRepository fuseHttpTraceRepository() {
    return new FuseHttpTraceRepository(new ObjectMapper());
  }

  /**
   * Object that performs the actual tracing of an HTTP exchange.
   *
   * @return the exchange tracer
   */
  @Bean
  public HttpExchangeTracer httpExchangeTracer() {
    // Trace everything!
    return new HttpExchangeTracer(Sets.newHashSet(Include.values()));
  }

  @Override
  public void configureContentNegotiation(final ContentNegotiationConfigurer configurer) {
    configurer.favorParameter(true) // give precedence to url request parameters
        .ignoreAcceptHeader(false) // enable use of the Accept header for content negotiation
        .useRegisteredExtensionsOnly(true) // let's not fallback on the Java Activation Framework
        .defaultContentType(MediaType.APPLICATION_JSON);
    // Allow the request to have a "?format=*" query parameter as an alternative to setting the
    // Accept header
    // e.g. "https://myurl?format=json" is the same as "Accept: application/json"
    configurer.parameterName("format");
    // Set the mappings between possible format parameter values and their associated MIME type
    configurer.mediaType("json", MediaType.APPLICATION_JSON);
    configurer.mediaType("xml", MediaType.APPLICATION_XML);
    configurer.mediaType("csv", TEXT_CSV);
    configurer.mediaType("xlsx", APPLICATION_EXCEL);

  }

  @Override
  public void configureMessageConverters(final List<HttpMessageConverter<?>> converters) {
    // The Protobuf converter MUST be added first, otherwise Jackson will try and handle our
    // protobuf to JSON conversion (and will of course, fail).
    converters.add(new ProtobufHttpMessageConverter()); // Protobuf, XML & JSON supported
    converters.add(new MappingJackson2HttpMessageConverter()); // JSON
    converters.add(new Jaxb2RootElementHttpMessageConverter()); // XML
    converters.add(new SettlementMissionCsvConverter()); // SettlementMissionList -> CSV
    converters.add(new SettlementMissionXlsxConverter()); // SettlementMissionList -> XLSX
    // ApiError -> JSON when requested format is XML, CSV, or XLSX
    converters.add(new ApiErrorConverter());
  }

}
