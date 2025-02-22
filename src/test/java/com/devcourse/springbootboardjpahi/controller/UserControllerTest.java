package com.devcourse.springbootboardjpahi.controller;

import static com.devcourse.springbootboardjpahi.message.UserExceptionMessage.BLANK_NAME;
import static com.devcourse.springbootboardjpahi.message.UserExceptionMessage.NEGATIVE_AGE;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devcourse.springbootboardjpahi.dto.CreateUserRequest;
import com.devcourse.springbootboardjpahi.dto.UserResponse;
import com.devcourse.springbootboardjpahi.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(UserController.class)
class UserControllerTest {

    static final Faker faker = new Faker();

    @MockBean
    UserService userService;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @DisplayName("[GET] 사용자 정보를 모두 반환한다.")
    @Test
    void testFindAll() throws Exception {
        // given
        List<UserResponse> mockResponses = List.of(generateUserResponse(), generateUserResponse());

        given(userService.findAll())
                .willReturn(mockResponses);

        // when
        ResultActions actions = mockMvc.perform(get("/api/v1/users"));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(mockResponses.size())));
    }

    @DisplayName("[GET] 등록된 사용자가 없으면 204 상태 코드를 반환한다.")
    @Test
    void testFindAllNoContent() throws Exception {
        // given
        given(userService.findAll())
                .willReturn(Collections.emptyList());

        // when
        ResultActions actions = mockMvc.perform(get("/api/v1/users"));

        // then
        actions.andExpect(status().isNoContent());
    }

    @DisplayName("[POST] 사용자를 추가한다.")
    @Test
    void testCreate() throws Exception {
        // given
        CreateUserRequest createUserRequest = generateCreateUserRequest();
        UserResponse userResponse = UserResponse.builder()
                .name(createUserRequest.name())
                .age(createUserRequest.age())
                .hobby(createUserRequest.hobby())
                .build();

        given(userService.create(createUserRequest))
                .willReturn(userResponse);

        // when
        ResultActions actions = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest)));

        // then
        actions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(createUserRequest.name())))
                .andExpect(jsonPath("$.age", is(createUserRequest.age())))
                .andExpect(jsonPath("$.hobby", is(createUserRequest.hobby())));
    }

    @DisplayName("[POST] 이름에 null과 공백이 들어가면 예외가 발생한다.")
    @ParameterizedTest(name = "이름 입력: {0}")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n", "\r\n"})
    void testCreateInvalidName(String name) throws Exception {
        // given
        int age = faker.number().numberBetween(0, 120);
        String hobby = faker.zelda().game();
        CreateUserRequest createUserRequest = new CreateUserRequest(name, age, hobby);

        // when
        ResultActions actions = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest)));

        // then
        actions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(BLANK_NAME)));
    }

    @DisplayName("[POST] 나이는 음수일 수 없다.")
    @Test
    void testCreateNegativeAge() throws Exception {
        // given
        String name = faker.name().firstName();
        int age = faker.number().numberBetween(-100, -1);
        String hobby = faker.zelda().game();
        CreateUserRequest createUserRequest = new CreateUserRequest(name, age, hobby);

        // when
        ResultActions actions = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest)));

        // then
        actions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(NEGATIVE_AGE)));
    }

    CreateUserRequest generateCreateUserRequest() {
        String name = faker.name().firstName();
        int age = faker.number().numberBetween(0, 120);
        String hobby = faker.esports().game();

        return new CreateUserRequest(name, age, hobby);
    }

    UserResponse generateUserResponse() {
        long id = faker.number().randomNumber();
        String name = faker.name().firstName();
        int age = faker.number().numberBetween(0, 120);
        String hobby = faker.esports().game();
        LocalDateTime createdAt = LocalDateTime.now();

        return UserResponse.builder()
                .id(id)
                .name(name)
                .age(age)
                .hobby(hobby)
                .createdAt(createdAt)
                .build();
    }
}
