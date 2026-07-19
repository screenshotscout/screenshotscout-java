package com.screenshotscout;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class JsonCodec {
  private static final JsonFactory JSON_FACTORY = new JsonFactory();

  private JsonCodec() {}

  static byte[] writeObject(Map<String, Object> value) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    try (JsonGenerator generator = JSON_FACTORY.createGenerator(output)) {
      generator.writeStartObject();
      for (Map.Entry<String, Object> field : value.entrySet()) {
        generator.writeFieldName(field.getKey());
        writeValue(generator, field.getValue());
      }
      generator.writeEndObject();
    }
    return output.toByteArray();
  }

  static Object parse(byte[] body) throws IOException {
    try (JsonParser parser = JSON_FACTORY.createParser(body)) {
      JsonToken first = parser.nextToken();
      if (first == null) {
        throw new JsonParseException(parser, "Expected a JSON value.");
      }
      Object value = readValue(parser, first);
      if (parser.nextToken() != null) {
        throw new JsonParseException(parser, "Unexpected trailing JSON content.");
      }
      return value;
    }
  }

  private static void writeValue(JsonGenerator generator, Object value) throws IOException {
    if (value == null) {
      generator.writeNull();
    } else if (value instanceof String stringValue) {
      generator.writeString(stringValue);
    } else if (value instanceof WireNumber wireNumber) {
      generator.writeNumber(wireNumber.value());
    } else if (value instanceof Integer integerValue) {
      generator.writeNumber(integerValue);
    } else if (value instanceof Long longValue) {
      generator.writeNumber(longValue);
    } else if (value instanceof Double doubleValue) {
      generator.writeNumber(doubleValue);
    } else if (value instanceof Float floatValue) {
      generator.writeNumber(floatValue);
    } else if (value instanceof BigInteger bigIntegerValue) {
      generator.writeNumber(bigIntegerValue);
    } else if (value instanceof BigDecimal bigDecimalValue) {
      generator.writeNumber(bigDecimalValue);
    } else if (value instanceof Boolean booleanValue) {
      generator.writeBoolean(booleanValue);
    } else if (value instanceof List<?> listValue) {
      generator.writeStartArray();
      for (Object item : listValue) {
        writeValue(generator, item);
      }
      generator.writeEndArray();
    } else if (value instanceof Map<?, ?> mapValue) {
      generator.writeStartObject();
      for (Map.Entry<?, ?> field : mapValue.entrySet()) {
        if (!(field.getKey() instanceof String key)) {
          throw new IOException("JSON object keys must be strings.");
        }
        generator.writeFieldName(key);
        writeValue(generator, field.getValue());
      }
      generator.writeEndObject();
    } else {
      throw new IOException("Unsupported JSON value type: " + value.getClass().getName());
    }
  }

  private static Object readValue(JsonParser parser, JsonToken token) throws IOException {
    return switch (token) {
      case START_OBJECT -> readObject(parser);
      case START_ARRAY -> readArray(parser);
      case VALUE_STRING -> parser.getText();
      case VALUE_NUMBER_INT, VALUE_NUMBER_FLOAT -> parser.getNumberValue();
      case VALUE_TRUE -> Boolean.TRUE;
      case VALUE_FALSE -> Boolean.FALSE;
      case VALUE_NULL -> null;
      default -> throw new JsonParseException(parser, "Unexpected JSON token: " + token);
    };
  }

  private static Map<String, Object> readObject(JsonParser parser) throws IOException {
    LinkedHashMap<String, Object> result = new LinkedHashMap<>();
    while (parser.nextToken() != JsonToken.END_OBJECT) {
      String name = parser.currentName();
      JsonToken valueToken = parser.nextToken();
      if (name == null || valueToken == null) {
        throw new JsonParseException(parser, "Incomplete JSON object.");
      }
      result.put(name, readValue(parser, valueToken));
    }
    return Collections.unmodifiableMap(result);
  }

  private static List<Object> readArray(JsonParser parser) throws IOException {
    List<Object> result = new ArrayList<>();
    JsonToken token;
    while ((token = parser.nextToken()) != JsonToken.END_ARRAY) {
      if (token == null) {
        throw new JsonParseException(parser, "Incomplete JSON array.");
      }
      result.add(readValue(parser, token));
    }
    return Collections.unmodifiableList(result);
  }
}
