package org.galatea.starter.utils.http.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.galatea.starter.MvcConfig;
import org.galatea.starter.entrypoint.ApiError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;

@Slf4j
public class ApiErrorConverter extends AbstractHttpMessageConverter<ApiError> {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Construct an ApiErrorCsvConverter that handles error response when the requested format is
   * XML, CSV, or XLSX.
   * Instead of returning the requested format, it returns a JSON representation of the error.
   */
  public ApiErrorConverter() {
    super(MediaType.APPLICATION_XML, MvcConfig.TEXT_CSV, MvcConfig.APPLICATION_EXCEL);
  }

  @Override
  protected boolean supports(final Class<?> clazz) {
    return ApiError.class.equals(clazz);
  }

  @Override
  protected ApiError readInternal(
      final Class<? extends ApiError> clazz,
      final HttpInputMessage inputMessage) {
    throw new UnsupportedOperationException("Reading to ApiError is not supported");
  }

  @SneakyThrows(IOException.class)
  @Override
  protected void writeInternal(final ApiError apiError,
      final HttpOutputMessage outputMessage) {
    log.info("Converting ApiError to JSON for HTTP response");
    outputMessage.getBody().write(objectMapper.writeValueAsBytes(apiError));
    log.info("Converted ApiError to JSON");
  }

  /**
   * Overriding is necessary because the returned media type is JSON, rather than the requested
   * format (XML, CSV, or XLSX).
   */
  @SneakyThrows(IOException.class)
  @Override
  protected void addDefaultHeaders(final HttpHeaders headers, final ApiError messages,
      final MediaType contentType) {
    super.addDefaultHeaders(headers, messages, MediaType.APPLICATION_JSON);
  }
}
