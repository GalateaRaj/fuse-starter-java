package org.galatea.starter.utils.http.converter;

import java.io.IOException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.Charsets;
import org.galatea.starter.MvcConfig;
import org.galatea.starter.domain.SettlementMission;
import org.galatea.starter.entrypoint.ApiError;
import org.galatea.starter.entrypoint.messagecontracts.SettlementMissionList;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;

/**
 * Custom HttpMessageConverter implementation to automatically convert a SettlementMissionList to an
 * CSV document when returning the SettlementMissionList in an HTTP response.
 */
@Slf4j
public class SettlementMissionCsvConverter
    extends AbstractHttpMessageConverter<SettlementMissionList> {

  /**
   * Construct a SettlementMissionCsvConverter that supports a CSV MediaType.
   */
  public SettlementMissionCsvConverter() {
    // The media type supported by this converter
    // Incoming media type for an HTTP request is described in the Content Type header
    // Media types that an HTTP request is willing to accept in the response are described in the
    // Accept header
    // Media types supported by a particular @RestController endpoint are described in the
    // @RequestMapping consumes and produces parameters
    super(MvcConfig.TEXT_CSV);
  }

  @Override
  protected boolean supports(final Class<?> clazz) {
    // Use .isAssignableFrom() instead of .equals() if this converter can support subclasses
    // Be careful using .isAssignableFrom(), because new subclasses may be added in the future that
    // aren't supported by the conversion logic in this converter
    return SettlementMissionList.class.equals(clazz);
  }

  @Override
  protected SettlementMissionList readInternal(
      final Class<? extends SettlementMissionList> clazz,
      final HttpInputMessage inputMessage) throws IOException {
    throw new UnsupportedOperationException(
        "Reading CSV to SettlementMissionList is not supported");
  }

  @SneakyThrows(IOException.class)
  @Override
  protected void writeInternal(final SettlementMissionList settlementMissionList,
      final HttpOutputMessage outputMessage) {
    log.info("Converting SettlementMissionList to CSV for HTTP response");
    outputMessage.getBody().write(CsvSerializer.serializeToCsv(
        settlementMissionList.getSettlementMissions(), SettlementMission.class)
        .getBytes(Charsets.UTF_8));
    log.info("Converted SettlementMissionList to CSV");
  }

  /**
   * Add additional headers to the outgoing response when this converter is used.
   */
  // Has a default implementation in AbstractHttpMessageConverter, so overriding this method is only
  // needed when modifying headers
  @Override
  protected void addDefaultHeaders(final HttpHeaders headers,
      final SettlementMissionList messages,
      final MediaType contentType) throws IOException {
    super.addDefaultHeaders(headers, messages, contentType);
    // Adding this header tells the browser to automatically download the response body
    // You may or may not want this depending on what your front end or other consumer wants
    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=SettlementMissions.csv");
  }
}
