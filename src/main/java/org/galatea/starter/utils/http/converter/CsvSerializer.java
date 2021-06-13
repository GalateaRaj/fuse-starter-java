package org.galatea.starter.utils.http.converter;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CsvSerializer {

  private CsvSerializer() {}

  /**
   * Serialize the given objects to a CSV document.
   *
   * <p>The CSV document will contain a header row of all the properties in the given Class. For
   * each object in the given Iterable, one data row will be created which will hold comma-separated
   * string representations of the values held in that object.
   *
   * <p>Does not support objects with fields that hold other complex objects.
   *
   * @param rows the collection of objects that should be serialized into the CSV document
   * @param clazz the class that is the type of the row data
   * @param <T> the type of the row data
   * @return a csv document as a string
   */
  public static <T> String serializeToCsv(final Iterable<T> rows, final Class<T> clazz)
      throws IOException {
    // Note that Jackson CSV doesn't work on objects with fields that hold complex objects
    // See https://github.com/FasterXML/jackson-dataformat-csv/issues/9
    CsvMapper mapper = new CsvMapper();
    mapper.disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
    // Jackson CSV uses the property names for the header row by default. To have a custom header
    // for one or more columns, see
    // https://stackoverflow.com/questions/40221223/jackson-dataformat-csv-are-custom-column-names-possible
    CsvSchema schema = mapper.schemaFor(clazz).withHeader();
    return mapper.writer(schema).writeValueAsString(rows);
    // See CsvWriterTest for examples of Jackson CSV behavior
  }

  /**
   * Serialize the given object to a CSV document.
   *
   * <p>The CSV document will contain a header row of all the properties in the given Class.
   * One data row will be created which will hold comma-separated string representations of the
   * values held in the given object.
   *
   * <p>Does not support objects with fields that hold other complex objects.
   *
   * @param value the object that should be serialized into the CSV document
   * @param clazz the class that is the type of the row data
   * @param <T> the type of the row data
   * @return a csv document as a string
   */
  public static <T> String serializeToCsv(final T value, final Class<T> clazz)
      throws IOException {
    // Note that Jackson CSV doesn't work on objects with fields that hold complex objects
    // See https://github.com/FasterXML/jackson-dataformat-csv/issues/9
    CsvMapper mapper = new CsvMapper();
    mapper.disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
    // Jackson CSV uses the property names for the header row by default. To have a custom header
    // for one or more columns, see
    // https://stackoverflow.com/questions/40221223/jackson-dataformat-csv-are-custom-column-names-possible
    CsvSchema schema = mapper.schemaFor(clazz).withHeader();
    return mapper.writer(schema).writeValueAsString(value);
    // See CsvWriterTest for examples of Jackson CSV behavior
  }

}
