package com.sharkdom.service.sales;

import com.sharkdom.entity.sales.Sales;
import com.sharkdom.entity.sales.SalesInfo;
import org.springframework.stereotype.Service;

@Service
public class SalesService {

    public SalesInfo getInfoOfLead(Sales body){
return new SalesInfo();
    }

}
