package com.sharkdom.model.trello;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrelloAuthParams {

    private String key;
    private String token;
    private String baseUrl;

}
