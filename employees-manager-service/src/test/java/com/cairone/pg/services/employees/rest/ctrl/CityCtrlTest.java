package com.cairone.pg.services.employees.rest.ctrl;

import com.cairone.pg.services.employees.core.mapper.CityMapper;
import com.cairone.pg.services.employees.core.service.CityService;
import com.cairone.pg.services.employees.data.dao.CityRepository;
import com.cairone.pg.services.employees.data.domain.CityEntity;
import com.cairone.pg.services.employees.rest.ctrl.request.CityRequest;
import com.cairone.pg.services.employees.rest.valid.AppControllerAdvice;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@Transactional
@ActiveProfiles("test")
public class CityCtrlTest extends AbstractCtrlTest {

    private URI baseUri;
    private CityRepository cityRepository;

    @Autowired
    public CityCtrlTest(CityRepository cityRepository, @LocalServerPort int port) {
        this.cityRepository = cityRepository;
        this.baseUri = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost")
                .port(port)
                .path("/api/cities")
                .build()
                .toUri();

    }

    @Test
    void whenFindAll_givenPageZeroSize5_thenHttpOk() throws Exception {

        CityMapper cityMapper = new CityMapper();
        CityService cityService = new CityService(cityRepository, cityMapper);

        PageableHandlerMethodArgumentResolver pageableResolver = new PageableHandlerMethodArgumentResolver();
        pageableResolver.setFallbackPageable(PageRequest.of(0, 10));

        MockMvc mvc = standaloneSetup().setCustomArgumentResolvers(pageableResolver).build();

        URI uri = UriComponentsBuilder.fromUri(baseUri)
                .queryParam("page", 0)
                .queryParam("size",  5)
                .queryParam("sort",  "name")
                .build()
                .toUri();

        mvc.perform(MockMvcRequestBuilders.get(uri))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content",
                        Matchers.hasSize(5)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name",
                        Matchers.equalTo("BEIJING")));
    }

    @Test
    void whenFindById_givenCityId_thenHttpOk() throws Exception {

        CityEntity expected = cityRepository.getById(1L);
        CityMapper cityMapper = new CityMapper();
        CityService cityService = new CityService(cityRepository, cityMapper);

        MockMvc mvc = standaloneSetup().build();

        URI uri = UriComponentsBuilder.fromUri(baseUri)
                .path("/" + expected.getId())
                .build()
                .toUri();

        mvc.perform(MockMvcRequestBuilders.get(uri))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(expected.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(expected.getName()));
    }

    @Test
    void whenCreate_givenNewCity_thenHttpCreated() throws Exception {

        CityRequest request = new CityRequest();
        request.setName("test");

        MockMvc mvc = standaloneSetup().build();

        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(baseUri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();

        Long createdId = findIdInResponse(mvcResult);

        CityEntity expected = new CityEntity();
        expected.setId(createdId);
        expected.setName("TEST");

        CityEntity actual = cityRepository.getById(createdId);
        Assertions.assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void whenCreate_givenNewCityWithAnExistingName_thenHttpBadRequest() throws Exception {

        CityRequest request = new CityRequest();
        request.setName("BEIJING");

        MockMvc mvc = standaloneSetup().setControllerAdvice(new AppControllerAdvice()).build();

        mvc.perform(MockMvcRequestBuilders.post(baseUri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                        Matchers.equalTo("Data integrity violation")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.fieldErrors.name[0]",
                        Matchers.equalTo("City with name BEIJING already exists")));
    }

    @Test
    void whenCreate_givenNewCityWithAnEmptyName_thenHttpBadRequest() throws Exception {

        CityRequest request = new CityRequest();
        request.setName("");

        MockMvc mvc = standaloneSetup().setControllerAdvice(new AppControllerAdvice()).build();

        mvc.perform(MockMvcRequestBuilders.post(baseUri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                        Matchers.equalTo("At least one field in the request is invalid")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.fieldErrors.name[0]",
                        Matchers.equalTo("must not be blank")));
    }

    @Test
    void whenCreate_givenNewCityWithAExtraLargeName_thenHttpBadRequest() throws Exception {

        CityRequest request = new CityRequest();
        request.setName("123456789012345678901234567890123456789012345678901");

        MockMvc mvc = standaloneSetup().setControllerAdvice(new AppControllerAdvice()).build();

        mvc.perform(MockMvcRequestBuilders.post(baseUri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",
                        Matchers.equalTo("At least one field in the request is invalid")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.fieldErrors.name[0]",
                        Matchers.equalTo("size must be between 0 and 50")));
    }

    @Test
    void whenDelete_givenCityById_thenCityEntityRemovedFromDatabase() throws Exception {

        final CityEntity cityEntity = cityRepository.getById(15L);

        MockMvc mvc = standaloneSetup().build();

        URI uri = UriComponentsBuilder.fromUri(baseUri)
                .path("/" + cityEntity.getId())
                .build()
                .toUri();

        mvc.perform(MockMvcRequestBuilders.delete(uri))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        boolean exists = cityRepository.existsById(cityEntity.getId());
        Assertions.assertThat(exists).isFalse();
    }

    @Test
    void whenUpdate_givenCityById_thenCityNameChangedInDatabase() throws Exception {

        final CityEntity before = cityRepository.getById(15L);

        MockMvc mvc = standaloneSetup().build();

        CityRequest request = new CityRequest();
        request.setName("CHANGED");

        URI uri = UriComponentsBuilder.fromUri(baseUri)
                .path("/" + before.getId())
                .build()
                .toUri();

        mvc.perform(MockMvcRequestBuilders.put(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk());

        final CityEntity after = cityRepository.getById(15L);

        Assertions.assertThat(after.getName()).isEqualTo(request.getName());
    }

    private StandaloneMockMvcBuilder standaloneSetup() {
        CityMapper cityMapper = new CityMapper();
        CityService cityService = new CityService(cityRepository, cityMapper);
        return MockMvcBuilders.standaloneSetup(new CityCtrl(cityService));
    }
}
