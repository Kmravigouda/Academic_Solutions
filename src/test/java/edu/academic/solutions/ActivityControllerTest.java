package edu.academic.solutions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ActivityControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ActivityRecordRepository repository;

    @BeforeEach
    void clearRecords() {
        repository.deleteAll();
    }

    @Test
    void exposesAllTenModulesAndDashboard() throws Exception {
        mockMvc.perform(get("/api/modules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)));

        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Explore your tools")));
    }

    @Test
    void calculatesCreditWeightedCgpaAndSemesterAverage() throws Exception {
        postRecord("""
                {"moduleKey":"academic-score","studentId":"S-1","studentName":"A Student",
                 "title":"Algorithms","period":"Semester 1","metric":8.5,"maximumMetric":10,
                 "weight":4,"status":"Completed"}
                """);
        postRecord("""
                {"moduleKey":"academic-score","studentId":"S-1","studentName":"A Student",
                 "title":"Databases","period":"Semester 1","metric":7.5,"maximumMetric":10,
                 "weight":2,"status":"Completed"}
                """);
        postRecord("""
                {"moduleKey":"semester-progress","studentId":"S-1","studentName":"A Student",
                 "title":"Midterm","period":"Semester 1","metric":80,"maximumMetric":100,
                 "weight":1,"status":"Completed"}
                """);

        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cgpa").value(8.17))
                .andExpect(jsonPath("$.semesterAverage").value(80.0))
                .andExpect(jsonPath("$.recordCount").value(3));
    }

    @Test
    void rejectsScoresAboveTheirMaximumAndDeletesRecords() throws Exception {
        mockMvc.perform(post("/api/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"moduleKey":"mock-tests","studentId":"S-2","studentName":"Another Student",
                                 "title":"Java basics","metric":11,"maximumMetric":10,"weight":1,"status":"Planned"}
                                """))
                .andExpect(status().isBadRequest());

        String response = mockMvc.perform(post("/api/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"moduleKey":"mock-tests","studentId":"S-2","studentName":"Another Student",
                                 "title":"Java basics","metric":8,"maximumMetric":10,"weight":1,"status":"Planned"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long id = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/records/{id}", id))
                .andExpect(status().isNoContent());
    }

    private void postRecord(String json) throws Exception {
        mockMvc.perform(post("/api/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }
}
