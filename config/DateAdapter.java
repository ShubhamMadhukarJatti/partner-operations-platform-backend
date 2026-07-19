package com.sharkdom.config;

import com.google.gson.*;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"; // ISO 8601 format
    private final SimpleDateFormat formatter;

    public DateAdapter(TimeZone timeZone) {
        this.formatter = new SimpleDateFormat(DATE_FORMAT);
        this.formatter.setTimeZone(timeZone); // Use the provided timezone
    }

    @Override
    public JsonElement serialize(Date src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return JsonNull.INSTANCE;
        }
        return new JsonPrimitive(formatter.format(src)); // Format the date
    }

    @Override
    public Date deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (json == null || json.isJsonNull()) {
            return null;
        }
        try {
            return formatter.parse(json.getAsString()); // Parse the date
        }catch (JsonParseException e){
            throw e;
        } catch (Exception e) {
            throw new ServiceException(ErrorMessages.SH133, json.getAsString(), e);
        }
    }
}

