package com.devcourse.springbootboardjpahi.docs;

import static com.devcourse.springbootboardjpahi.message.PostExceptionMessage.NO_SUCH_POST;
import static com.devcourse.springbootboardjpahi.message.PostExceptionMessage.NO_SUCH_USER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.devcourse.springbootboardjpahi.controller.PostController;
import com.devcourse.springbootboardjpahi.domain.User;
import com.devcourse.springbootboardjpahi.dto.CreatePostRequest;
import com.devcourse.springbootboardjpahi.dto.PageResponse;
import com.devcourse.springbootboardjpahi.dto.PostDetailResponse;
import com.devcourse.springbootboardjpahi.dto.PostResponse;
import com.devcourse.springbootboardjpahi.dto.UpdatePostRequest;
import com.devcourse.springbootboardjpahi.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@AutoConfigureRestDocs
@WebMvcTest(PostController.class)
public class PostControllerRestdocsTest {

    static final Faker faker = new Faker();

    @MockBean
    PostService postService;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @DisplayName("[POST] 포스트를 생성 API 테스트")
    @Test
    void testCreatePostsAPI() throws Exception {
        // given
        User author = generateAuthor();
        CreatePostRequest createPostRequest = generateCreateRequest(author.getId());
        long id = generateId();
        PostResponse postResponse = PostResponse.builder()
                .id(id)
                .title(createPostRequest.title())
                .content(createPostRequest.content())
                .authorName(author.getName())
                .createdAt(LocalDateTime.now())
                .build();

        given(postService.create(createPostRequest))
                .willReturn(postResponse);

        // when
        ResultActions actions = mockMvc.perform(post("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPostRequest)));

        // then
        actions.andDo(document("post-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                field("title", JsonFieldType.STRING, "Title"),
                                field("content", JsonFieldType.STRING, "Content"),
                                field("userId", JsonFieldType.NUMBER, "User ID")),
                        responseFields(
                                field("id", JsonFieldType.NUMBER, "ID"),
                                field("title", JsonFieldType.STRING, "Title"),
                                field("content", JsonFieldType.STRING, "Content"),
                                field("authorName", JsonFieldType.STRING, "Author Name"),
                                field("createdAt", JsonFieldType.STRING, "Creation Datetime"))))
                .andDo(print());
    }

    @DisplayName("[POST] 존재하지 않는 유저의 포스트 생성 API")
    @Test
    void testCreateNotExistAPI() throws Exception {
        // given
        long id = generateId();
        CreatePostRequest createPostRequest = generateCreateRequest(id);

        given(postService.create(createPostRequest))
                .willThrow(new NoSuchElementException(NO_SUCH_USER));

        // when
        ResultActions actions = mockMvc.perform(post("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPostRequest)));

        // then
        actions.andDo(document("post-create-not-exist-user",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                field("title", JsonFieldType.STRING, "Title"),
                                field("content", JsonFieldType.STRING, "Content"),
                                field("userId", JsonFieldType.NUMBER, "User ID")),
                        responseFields(
                                field("message", JsonFieldType.STRING, "Error Message"))))
                .andDo(print());
    }

    @DisplayName("[GET] 포스트를 상세 조회 API")
    @Test
    void testFindByIdAPI() throws Exception {
        // given
        long id = generateId();
        String title = faker.book().title();
        String content = faker.shakespeare().kingRichardIIIQuote();
        String authorName = faker.name().firstName();
        PostDetailResponse postDetailResponse = PostDetailResponse.builder()
                .id(id)
                .title(title)
                .content(content)
                .authorName(authorName)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(postService.findById(id))
                .willReturn(postDetailResponse);

        // when
        MockHttpServletRequestBuilder docsGetRequest = RestDocumentationRequestBuilders.get("/api/v1/posts/{id}", id);
        ResultActions actions = mockMvc.perform(docsGetRequest);

        // then
        actions.andDo(document("post-find-single",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("ID")),
                        responseFields(
                                field("id", JsonFieldType.NUMBER, "ID"),
                                field("title", JsonFieldType.STRING, "Title"),
                                field("content", JsonFieldType.STRING, "Content"),
                                field("authorName", JsonFieldType.STRING, "Author Name"),
                                field("createdAt", JsonFieldType.STRING, "Creation Datetime"),
                                field("updatedAt", JsonFieldType.STRING, "Last Update Datetime"))))
                .andDo(print());
    }

    @DisplayName("[GET] 존재하지 않는 포스트 상세 조회 API")
    @Test
    void testFindByIdNotExistAPI() throws Exception {
        // given
        long id = generateId();

        given(postService.findById(id))
                .willThrow(new NoSuchElementException(NO_SUCH_POST));

        // when
        MockHttpServletRequestBuilder docsGetRequest = RestDocumentationRequestBuilders.get("/api/v1/posts/{id}", id);
        ResultActions actions = mockMvc.perform(docsGetRequest);

        // then
        actions.andDo(document("post-find-single-not-exist",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("ID")),
                        responseFields(
                                field("message", JsonFieldType.STRING, "Error Message"))))
                .andDo(print());
    }

    @DisplayName("[PUT] 포스트 수정 API")
    @Test
    void testUpdateByIdAPI() throws Exception {
        // given
        UpdatePostRequest updatePostRequest = generateUpdateRequest();

        long id = generateId();
        String authorName = faker.name().firstName();
        PostDetailResponse postDetailResponse = PostDetailResponse.builder()
                .id(id)
                .title(updatePostRequest.title())
                .content(updatePostRequest.content())
                .authorName(authorName)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(postService.updateById(id, updatePostRequest))
                .willReturn(postDetailResponse);

        // when
        MockHttpServletRequestBuilder docsPutRequest = RestDocumentationRequestBuilders.put("/api/v1/posts/{id}", id);
        ResultActions actions = mockMvc.perform(docsPutRequest
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatePostRequest)));

        // then
        actions.andDo(document("post-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("ID")),
                        requestFields(
                                field("title", JsonFieldType.STRING, "Title"),
                                field("content", JsonFieldType.STRING, "Content")),
                        responseFields(
                                field("id", JsonFieldType.NUMBER, "ID"),
                                field("title", JsonFieldType.STRING, "Title"),
                                field("content", JsonFieldType.STRING, "Content"),
                                field("authorName", JsonFieldType.STRING, "Author Name"),
                                field("createdAt", JsonFieldType.STRING, "Creation Datetime"),
                                field("updatedAt", JsonFieldType.STRING, "Last Update Datetime"))))
                .andDo(print());
    }

    @DisplayName("[PUT] 존재하지 않는 포스트 수정 API")
    @Test
    void testUpdateByIdNotExistAPI() throws Exception {
        // given
        UpdatePostRequest updatePostRequest = generateUpdateRequest();
        long id = generateId();

        given(postService.updateById(id, updatePostRequest))
                .willThrow(new NoSuchElementException(NO_SUCH_POST));

        // when
        MockHttpServletRequestBuilder docsPutRequest = RestDocumentationRequestBuilders.put("/api/v1/posts/{id}", id);
        ResultActions actions = mockMvc.perform(docsPutRequest
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatePostRequest)));

        // then
        actions.andDo(document("post-update-not-exist",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("ID")),
                        requestFields(
                                field("title", JsonFieldType.STRING, "Title"),
                                field("content", JsonFieldType.STRING, "Content")),
                        responseFields(
                                field("message", JsonFieldType.STRING, "Error Message"))))
                .andDo(print());
    }

    @DisplayName("[GET] 포스트 NoContent 조회 API")
    @Test
    void testFindNoContentAPI() throws Exception {
        // given
        PageResponse<PostResponse> page = PageResponse.<PostResponse>builder()
                .isEmpty(true)
                .totalPages(1)
                .totalElements(0L)
                .content(Collections.emptyList())
                .build();

        given(postService.getPage(any()))
                .willReturn(page);

        // when
        ResultActions actions = mockMvc.perform(get("/api/v1/posts"));

        // then
        actions.andDo(document("post-page-no-content"))
                .andDo(print());
    }

    @DisplayName("[GET] 포스트를 페이징 조회 API")
    @Test
    void testFindAPI() throws Exception {
        // given
        long totalCount = 35;
        int defaultPageSize = 10;
        int totalPages = (int) Math.ceil((double) totalCount / defaultPageSize);
        long contentSize = totalCount % defaultPageSize;
        List<PostResponse> postResponses = generatePostResponsesOrderByAsc(contentSize);
        PageResponse<PostResponse> page = PageResponse.<PostResponse>builder()
                .isEmpty(false)
                .totalPages(totalPages)
                .totalElements(totalCount)
                .content(postResponses)
                .build();

        given(postService.getPage(any()))
                .willReturn(page);

        // when
        ResultActions actions = mockMvc.perform(get("/api/v1/posts")
                .param("page", "3")
                .param("size", "10"));

        // then
        actions.andDo(document("post-page",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("page").description("Page"),
                                parameterWithName("size").description("Contents per Page")),
                        responseFields(
                                field("isEmpty", JsonFieldType.BOOLEAN, "True if no post is found"),
                                field("totalPages", JsonFieldType.NUMBER, "Total number of pages"),
                                field("totalElements", JsonFieldType.NUMBER, "Total number of all posts"),
                                field("content[].id", JsonFieldType.NUMBER, "Post Id"),
                                field("content[].title", JsonFieldType.STRING, "Title"),
                                field("content[].content", JsonFieldType.STRING, "Content"),
                                field("content[].authorName", JsonFieldType.STRING, "Author Name"),
                                field("content[].createdAt", JsonFieldType.STRING, "Creation Datetime"))))
                .andDo(print());
    }

    private CreatePostRequest generateCreateRequest(Long userId) {
        String title = faker.book().title();
        String content = faker.shakespeare().hamletQuote();

        return new CreatePostRequest(title, content, userId);
    }

    private UpdatePostRequest generateUpdateRequest() {
        String title = faker.book().title();
        String content = faker.shakespeare().hamletQuote();

        return new UpdatePostRequest(title, content);
    }

    private User generateAuthor() {
        long id = generateId();
        String name = faker.name().firstName();
        int age = faker.number().numberBetween(0, 120);
        String hobby = faker.esports().game();

        return User.builder()
                .id(id)
                .name(name)
                .age(age)
                .hobby(hobby)
                .build();
    }

    private PostResponse generatePostResponse() {
        long id = generateId();
        String title = faker.book().title();
        String content = faker.shakespeare().hamletQuote();
        User author = generateAuthor();

        return PostResponse.builder()
                .id(id)
                .title(title)
                .content(content)
                .authorName(author.getName())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private List<PostResponse> generatePostResponsesOrderByAsc(long count) {
        List<PostResponse> postResponses = new ArrayList<>();

        for (long id = 1; id <= count; id++) {
            String title = faker.book().title();
            String content = faker.shakespeare().hamletQuote();
            User author = generateAuthor();

            PostResponse postResponse = PostResponse.builder()
                    .id(id)
                    .title(title)
                    .content(content)
                    .authorName(author.getName())
                    .createdAt(LocalDateTime.now())
                    .build();

            postResponses.add(postResponse);
        }

        return postResponses;
    }

    private long generateId() {
        return Math.abs(faker.number().randomDigitNotZero());
    }

    private FieldDescriptor field(String name, Object type, String description) {
        return fieldWithPath(name)
                .type(type)
                .description(description);
    }
}
