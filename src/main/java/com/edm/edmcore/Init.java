package com.edm.edmcore;

import com.edm.edmcore.client.DispositionClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class Init implements InitializingBean {

    private final DispositionClient dispositionClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println(dispositionClient.getAllDisposition());
        System.out.println(dispositionClient.getAllDispositionByEmployeeCode("Siema",LocalDate.now(), LocalDate.now().plusDays(5)));
    }
}
