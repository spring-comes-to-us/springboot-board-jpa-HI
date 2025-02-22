package com.devcourse.springbootboardjpahi.controller;

import com.devcourse.springbootboardjpahi.dto.CreatePostRequest;
import com.devcourse.springbootboardjpahi.dto.PageResponse;
import com.devcourse.springbootboardjpahi.dto.PostDetailResponse;
import com.devcourse.springbootboardjpahi.dto.PostResponse;
import com.devcourse.springbootboardjpahi.dto.UpdatePostRequest;
import com.devcourse.springbootboardjpahi.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<PageResponse<PostResponse>> find(Pageable pageable) {
        PageResponse<PostResponse> page = postService.getPage(pageable);

        if (page.isEmpty()) {
            return ResponseEntity.noContent()
                    .build();
        }

        return ResponseEntity.ok(page);
    }

    @PostMapping
    public ResponseEntity<PostResponse> create(@Valid @RequestBody CreatePostRequest request) {
        PostResponse post = postService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(post);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDetailResponse> findById(@PathVariable Long id) {
        PostDetailResponse postDetailResponse = postService.findById(id);

        return ResponseEntity.ok(postDetailResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDetailResponse> updateById(@PathVariable Long id,
                                                         @Valid @RequestBody UpdatePostRequest request) {
        PostDetailResponse postDetailResponse = postService.updateById(id, request);

        return ResponseEntity.ok(postDetailResponse);
    }
}
