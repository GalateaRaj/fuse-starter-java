package org.galatea.starter.utils.http.converter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Slf4j
// See XlsxSerializerTest for example output
public class XlsxSerializer {

  private XlsxSerializer() {}

  /**
   * Serialize the given objects to an XLSX spreadsheet.
   *
   * <p>Doesn't do any special handling of nested complex objects or collections in the given
   * row objects - each field in the row object is basically toString()'ed and put in a cell.
   *
   * @param rows the row data that the spreadsheet should hold
   * @param clazz the class that is the type of the row data
   * @param <T> the type of the row data
   * @return the binary representation of the xlsx spreadsheet
   */
  public static <T> byte[] serializeToXlsx(final Iterable<T> rows, final Class<T> clazz)
      throws IOException {
    // XSSF is used for xlsx-format spreadsheets, HSSF is used for xls-format
    // SXSSF is the streaming version of XSSF, and is useful for working with large spreadsheets
    try (Workbook wb = new XSSFWorkbook()) {
      Sheet sheet = wb.createSheet(clazz.getSimpleName());
      List<Field> fieldsToSerialize = getFieldsToSerialize(clazz);
      populateHeaderRow(sheet, getHeaderValues(fieldsToSerialize));
      populateDataRows(sheet, fieldsToSerialize, rows);
      return writeSpreadsheetToBytes(wb);
    }
  }

  /**
   * Serialize the given objects to an XLSX spreadsheet.
   *
   * <p>Doesn't do any special handling of nested complex objects or collections in the given
   * row objects - each field in the row object is basically toString()'ed and put in a cell.
   *
   * @param row the row data that the spreadsheet should hold
   * @param clazz the class that is the type of the row data
   * @param <T> the type of the row data
   * @return the binary representation of the xlsx spreadsheet
   */
  public static <T> byte[] serializeToXlsx(final T row, final Class<T> clazz)
      throws IOException {
    // XSSF is used for xlsx-format spreadsheets, HSSF is used for xls-format
    // SXSSF is the streaming version of XSSF, and is useful for working with large spreadsheets
    try (Workbook wb = new XSSFWorkbook()) {
      Sheet sheet = wb.createSheet(clazz.getSimpleName());
      List<Field> fieldsToSerialize = getFieldsToSerialize(clazz);
      populateHeaderRow(sheet, getHeaderValues(fieldsToSerialize));
      populateSingleDataRow(sheet, fieldsToSerialize, row);
      return writeSpreadsheetToBytes(wb);
    }
  }

  /*
   * Get a list of all fields in the given class, including inherited and private fields, but
   * excluding any @JsonIgnore'd fields.
   */
  private static List<Field> getFieldsToSerialize(final Class<?> clazz) {
    List<Field> fields = new ArrayList<>();
    // Class#getFields() gets all public fields in a class including inherited fields, while
    // Class#getDeclaredFields() gets all fields in a class excluding inherited fields
    // To get all fields in the class, we do getDeclaredFields() all the way up the class hierarchy
    if (clazz.getSuperclass() != null) {
      fields = getFieldsToSerialize(clazz.getSuperclass());
    }

    Arrays.stream(clazz.getDeclaredFields())
        .filter(XlsxSerializer::shouldSerializeField)
        .forEach(fields::add);
    return fields;
  }

  /*
   * Check whether a field should be serialized.
   *
   * Returns false if the field is synthetic, transient, or @JsonIgnore-d, and true otherwise.
   */
  private static boolean shouldSerializeField(final Field field) {
    // Jacoco adds a synthetic member variable "$jacocoData" to classes under test. Ignore such
    // synthetic fields to avoid inconsistent test behavior.
    // https://github.com/jacoco/jacoco/issues/168
    // http://mylearningdump.blogspot.com/2017/05/java-reflection-synthetic-members-and.html
    if (field.isSynthetic()) {
      return false;
    }
    // The transient keyword indicates that a variable should not be serialized
    if (Modifier.isTransient(field.getModifiers())) {
      return false;
    }
    // Ignore any fields that have a @JsonIgnore annotation
    JsonIgnore jsonIgnore = field.getAnnotation(JsonIgnore.class);
    return jsonIgnore == null || !jsonIgnore.value();
  }

  /*
   * Get the column headers that should be used for the given fields. If a @JsonProperty annotation
   * is present on a given field, use that annotation's value as the header, otherwise use the field
   * name.
   */
  private static List<String> getHeaderValues(final List<Field> fields) {
    List<String> headers = new ArrayList<>();
    for (Field field : fields) {
      // Could also use a custom annotation instead of JsonProperty if different configuration is
      // needed for JSON and XLSX serialization
      JsonProperty customHeaderNameAnnotation = field.getAnnotation(JsonProperty.class);
      String header = customHeaderNameAnnotation == null
          ? field.getName()
          : customHeaderNameAnnotation.value();
      headers.add(header);
    }
    return headers;
  }

  /*
   * Populate the first row of the given sheet using the given list of header values.
   */
  private static void populateHeaderRow(final Sheet sheet, final List<String> headers) {
    Row headerRow = sheet.createRow(0);
    for (int col = 0; col < headers.size(); col++) {
      Cell headerCell = headerRow.createCell(col);
      headerCell.setCellValue(headers.get(col));
    }
  }

  /*
   * Populate the rows of the given sheet using the given row data.
   */
  @SneakyThrows(IllegalAccessException.class)
  private static <T> void populateDataRows(final Sheet sheet, final List<Field> fieldsToSerialize,
      final Iterable<T> rows) {
    int rowIndex = 1; // header is row 0
    for (T row : rows) {
      Row dataRow = sheet.createRow(rowIndex);
      rowIndex++;
      for (int col = 0; col < fieldsToSerialize.size(); col++) {
        Cell dataCell = dataRow.createCell(col);
        Object cellObject = FieldUtils.readField(fieldsToSerialize.get(col), row, true);
        dataCell.setCellValue(stringify(cellObject));
      }
    }
  }

  /*
   * Populate the rows of the given sheet using the given row data.
   */
  @SneakyThrows(IllegalAccessException.class)
  private static <T> void populateSingleDataRow(final Sheet sheet,
      final List<Field> fieldsToSerialize, final T row) {
    int rowIndex = 1; // header is row 0
    Row dataRow = sheet.createRow(rowIndex);
    for (int col = 0; col < fieldsToSerialize.size(); col++) {
      Cell dataCell = dataRow.createCell(col);
      Object cellObject = FieldUtils.readField(fieldsToSerialize.get(col), row, true);
      dataCell.setCellValue(stringify(cellObject));
    }
  }

  /*
   * Get a useful string representation of the given object.
   */
  private static String stringify(final Object obj) {
    if (obj == null) {
      return "";
    } else if (obj.getClass().isArray()) {
      // Default toString() of an array isn't useful, as it just gives something like [C@6e1408
      // We don't know whether obj is a primitive array or an object array, and primitive arrays
      // can't be directly cast to Object[], so we copy the possibly-primitive array into
      // an Object[] and go from there
      // https://stackoverflow.com/questions/5606338/cast-primitive-type-array-into-object-array-in-java
      int len = Array.getLength(obj);
      Object[] objectArr = new Object[len];
      for (int i = 0; i < len; i++) {
        objectArr[i] = Array.get(obj, i);
      }
      return Arrays.toString(objectArr);
    } else {
      return obj.toString();
    }
  }

  private static byte[] writeSpreadsheetToBytes(final Workbook wb) throws IOException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      wb.write(baos);
      return baos.toByteArray();
    } catch (IOException e) {
      log.error("Unable to write spreadsheet to bytes", e);
      throw e;
    }
  }
}
