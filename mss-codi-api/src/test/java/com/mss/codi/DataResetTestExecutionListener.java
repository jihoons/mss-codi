package com.mss.codi;

import com.mss.codi.config.DevConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

public class DataResetTestExecutionListener implements TestExecutionListener {

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        /*
         * 테스트 테이터가 변경되었을 수 있어서 삭제하고 다시 만들도록 수정
         */
        var devConfiguration = testContext.getApplicationContext().getBean(DevConfiguration.class);
        devConfiguration.reset();
    }
}
