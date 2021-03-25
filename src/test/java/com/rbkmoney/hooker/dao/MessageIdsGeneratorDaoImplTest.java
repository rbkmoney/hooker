package com.rbkmoney.hooker.dao;

import com.rbkmoney.hooker.AbstractIntegrationTest;
import com.rbkmoney.hooker.dao.impl.MessageIdsGeneratorDaoImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MessageIdsGeneratorDaoImplTest extends AbstractIntegrationTest {

    @Autowired
    private MessageIdsGeneratorDaoImpl idsGeneratorDao;

    @Test
    public void test() {
        List<Long> list = idsGeneratorDao.get(100);
        assertEquals(100, list.size());
    }
}
