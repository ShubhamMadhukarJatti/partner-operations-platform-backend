package com.sharkdom.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GoogleSheetService {

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    public List<String> getColumnValues(String sheetUrl, String columnName, String refreshToken) {
        try {
            String spreadsheetId = extractSpreadsheetId(sheetUrl);
            String range = "Sheet1"; // or dynamically read sheet name if needed

            Sheets sheets = getSheetsService(refreshToken);
            ValueRange valueRange = sheets.spreadsheets().values().get(spreadsheetId, range).execute();

            List<List<Object>> rows = valueRange.getValues();
            if (rows == null || rows.isEmpty()) {
                throw new ServiceException(ErrorMessages.SH143);
            }

            List<Object> headers = rows.get(0);
            int colIndex = headers.indexOf(columnName);
            if (colIndex == -1) {
                throw new ServiceException(ErrorMessages.SH142, columnName);
            }

            List<String> result = new ArrayList<>();
            for (int i = 1; i < rows.size(); i++) {
                List<Object> row = rows.get(i);
                if (row.size() > colIndex) {
                    result.add(row.get(colIndex).toString());
                } else {
                    result.add(""); // blank cell
                }
            }
            return result;
        } catch (Exception e) {
            throw new ServiceException(ErrorMessages.SH141, e.getMessage());
        }
    }

    private Sheets getSheetsService(String refreshToken) throws Exception {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        UserCredentials credentials = UserCredentials.newBuilder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(refreshToken)
                .build();

        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        return new Sheets.Builder(httpTransport, jsonFactory, requestInitializer)
                .setApplicationName("Sheet Reader App")
                .build();
    }

    private String extractSpreadsheetId(String sheetUrl) {
        String regex = "/spreadsheets/d/([a-zA-Z0-9-_]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(sheetUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Invalid Google Sheet URL");
    }
}
