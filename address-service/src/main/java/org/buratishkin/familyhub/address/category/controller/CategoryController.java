package org.buratishkin.familyhub.address.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.buratishkin.familyhub.shared.response.CreateResp;
import org.buratishkin.familyhub.shared.response.DeleteResp;
import org.buratishkin.familyhub.shared.response.UpdateResp;
import org.buratishkin.familyhub.address.category.dto.CategoryCreateReq;
import org.buratishkin.familyhub.address.category.dto.CategoryCreateResp;
import org.buratishkin.familyhub.address.category.dto.CategoryDeleteReq;
import org.buratishkin.familyhub.address.category.dto.CategoryUpdateReq;
import org.buratishkin.familyhub.address.category.service.CategoryManageService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/category")
public class CategoryController {
    private final CategoryManageService categoryManageService;

    @PostMapping("/")
    public CreateResp<CategoryCreateResp> create(@Valid @RequestBody CategoryCreateReq createReq,
                                                  Authentication authentication){
        return categoryManageService.create(createReq, authentication);
    }

    @PutMapping("/{categoryId}")
    public UpdateResp update(@Valid @RequestBody CategoryUpdateReq updateReq,
                             @PathVariable Long categoryId,
                             Authentication authentication) {
        return categoryManageService.update(updateReq, categoryId, authentication);
    }

    @DeleteMapping("/{categoryId}")
    public DeleteResp delete(@Valid @RequestBody(required = false) CategoryDeleteReq deleteReq,
                             @PathVariable Long categoryId,
                             Authentication authentication) {
        return categoryManageService.delete(deleteReq, categoryId, authentication);
    }
}
