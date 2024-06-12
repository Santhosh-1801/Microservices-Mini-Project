package com.programmer.product_service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.programmer.product_service.dto.ProductRequest;
import com.programmer.product_service.dto.ProductResponse;
import com.programmer.product_service.model.Product;
import com.programmer.product_service.repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class ProductServiceApplicationTests {
	@Container
	static MongoDBContainer mongoDBContainer=new MongoDBContainer("mongo:4.4.2");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProductRepository productRepository;

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry){
		dynamicPropertyRegistry.add("spring.data.mongodb.uri",mongoDBContainer::getReplicaSetUrl);
	}

	@Test
	void shouldCreateProduct() throws Exception {
		ProductRequest productRequest=getProductRequest();
		String productRequestString=objectMapper.writeValueAsString(productRequest);
		mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
				.contentType(MediaType.APPLICATION_JSON)
				.content(productRequestString))
				.andExpect(status().isCreated());

		Assertions.assertEquals(1,productRepository.findAll().size());


	}
	@Test
	void shouldGetAllProducts() throws Exception {
		// Arrange: Create and save some test products
		Product product1 = Product.builder()
				.name("OnePlus A1")
				.description("this is international product")
				.price(BigDecimal.valueOf(19000))
				.build();
		Product product2 = Product.builder()
				.name("IPhone 13 Plus")
				.description("this is apple product")
				.price(BigDecimal.valueOf(100000))
				.build();

		productRepository.save(product1);
		productRepository.save(product2);

		// Act: Perform the GET request
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/product")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		// Assert: Check the response
		String jsonResponse = mvcResult.getResponse().getContentAsString();
		List<ProductResponse> productResponses = objectMapper.readValue(jsonResponse, new TypeReference<List<ProductResponse>>() {});

		Assertions.assertEquals(3, productResponses.size());
		Assertions.assertTrue(productResponses.stream().anyMatch(p -> p.getName().equals("OnePlus A1")));
		Assertions.assertTrue(productResponses.stream().anyMatch(p -> p.getName().equals("IPhone 13 Plus")));
	}

	private ProductRequest getProductRequest() {
		return ProductRequest.builder().name("Samsung Galaxy A6").description("It is a Samsung product").price(BigDecimal.valueOf(18000)).build();
	}

}
