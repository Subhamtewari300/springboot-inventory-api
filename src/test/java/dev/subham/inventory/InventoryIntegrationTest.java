package dev.subham.inventory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc
class InventoryIntegrationTest {
 @Autowired MockMvc mvc;
 @Autowired ObjectMapper mapper;

 @Test void reservationIsIdempotentAndCancellationRestoresStock() throws Exception {
  String sku="test-"+java.util.UUID.randomUUID();
  String created=mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
     .content("{\"sku\":\""+sku+"\",\"name\":\"Notebook\",\"stock\":5}"))
     .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
  long id=mapper.readTree(created).get("id").asLong();
  String payload="{\"productId\":"+id+",\"quantity\":3,\"idempotencyKey\":\""+sku+"\"}";
  String one=mvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON).content(payload))
     .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
  String two=mvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON).content(payload))
     .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
  long reservationId=mapper.readTree(one).get("id").asLong();
  assertEquals(reservationId,mapper.readTree(two).get("id").asLong());
  mvc.perform(get("/api/products/"+id)).andExpect(jsonPath("$.stock").value(2));
  mvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON)
     .content("{\"productId\":"+id+",\"quantity\":4,\"idempotencyKey\":\""+sku+"-other\"}"))
     .andExpect(status().isConflict());
  mvc.perform(delete("/api/reservations/"+reservationId)).andExpect(status().isOk());
  mvc.perform(delete("/api/reservations/"+reservationId)).andExpect(status().isOk());
  mvc.perform(get("/api/products/"+id)).andExpect(jsonPath("$.stock").value(5));
 }
}
