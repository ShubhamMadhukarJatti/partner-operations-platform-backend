package com.sharkdom.model.ppi;

import lombok.Data;

import java.util.List;
@Data
public class ViewDetailsWrapperResponse {
    private ViewDetailsResponse singleResponse;
    private List<ViewDetailsResponse> responseList;
}
