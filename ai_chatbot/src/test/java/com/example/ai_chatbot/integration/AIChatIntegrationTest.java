package com.example.ai_chatbot.integration;

import com.example.ai_chatbot.integration.VO.ConfigurationVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AIChatIntegrationTest {

    private AIChatIntegration integration;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        integration = new AIChatIntegration(restTemplate, "http://localhost:8080");
    }

    @Test
    void retrievesAllRewardConfigurations() {
        server.expect(once(), requestTo("http://localhost:8080/config/searchall"))
                .andRespond(withSuccess("""
                        {
                          "configurationVoList": [
                            {
                              "id": 1,
                              "code": "STAGE_RULE",
                              "rule": "stage = totalDuration < 160 ? 0 : 1",
                              "description": "Listening reward stage rule"
                            }
                          ],
                          "baseVO": {
                            "success": true,
                            "code": 200,
                            "duration": 2,
                            "errorMessage": null
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        List<ConfigurationVO> result = integration.getAllConfigurations();

        assertEquals(1, result.size());
        assertEquals("STAGE_RULE", result.get(0).getCode());
        assertEquals("stage = totalDuration < 160 ? 0 : 1", result.get(0).getRule());
        server.verify();
    }

    @Test
    void retrievesPrizeRecordsByBusinessDate() {
        server.expect(once(), requestTo(
                        "http://localhost:8080/prizerecord/by-user-and-date?userId=3&prizeDate=2026-08-11"))
                .andRespond(withSuccess("""
                        {
                          "prizeRecordVOList": [
                            {
                              "id": 1,
                              "userId": 3,
                              "prizeDate": "2026-08-11",
                              "prizeStage": 2,
                              "prizeAmount": 1
                            }
                          ],
                          "baseVO": {
                            "success": true,
                            "code": 200,
                            "duration": 2,
                            "errorMessage": null
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        assertEquals(1, integration.getByUserAndPrizeDate(3, "2026-08-11").size());
        server.verify();
    }
}
