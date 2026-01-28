package com.EcommerceApp.service.imp;

import com.EcommerceApp.service.CommonService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CommonServiceImpl implements CommonService {

    @Value("${rupee.sign:₹}")
    private String rupeeSign;

    @Override
    public String rupeeSign() {
        return rupeeSign;
    }
}
