package com.pomanagement.web;

import com.pomanagement.workflow.exception.EntityNotFoundException;
import com.pomanagement.workflow.exception.ForbiddenActionException;
import com.pomanagement.workflow.exception.IllegalTransitionException;
import com.pomanagement.workflow.exception.ValidationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ThrowingController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // --- ValidationException → 400 ---

    @Test
    void validationException_returns400_validationFailed() throws Exception {
        mockMvc.perform(get("/test/validation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.error.message").value("comment must not be blank"));
    }

    // --- MethodArgumentNotValidException → 400 with per-field details ---

    @Test
    void methodArgumentNotValid_returns400_withFieldDetails() throws Exception {
        mockMvc.perform(post("/test/bean-validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.details.name").exists());
    }

    @Test
    void methodArgumentNotValid_detailsAbsent_forSimpleValidation() throws Exception {
        // ValidationException (not bean-validation) has no details map
        mockMvc.perform(get("/test/validation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.details").doesNotExist());
    }

    // --- ForbiddenActionException → 403 ---

    @Test
    void forbiddenException_returns403_forbidden() throws Exception {
        mockMvc.perform(get("/test/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.error.message").value("creator cannot approve their own PO"));
    }

    // --- EntityNotFoundException → 404 ---

    @Test
    void entityNotFoundException_returns404_notFound() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.error.message").value("PO 99 not found"));
    }

    // --- IllegalTransitionException → 409 ---

    @Test
    void illegalTransitionException_returns409_illegalTransition() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("ILLEGAL_TRANSITION"))
                .andExpect(jsonPath("$.error.message").value("action not allowed in state: INVOICED"));
    }

    // --- Generic Exception → 500, no stack trace ---

    @Test
    void genericException_returns500_noInternalDetail() throws Exception {
        mockMvc.perform(get("/test/server-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.error.message").value("An unexpected error occurred"))
                .andExpect(content().string(not(containsString("NullPointerException"))))
                .andExpect(content().string(not(containsString("com.pomanagement"))))
                .andExpect(content().string(not(containsString("stackTrace"))));
    }

    // --- body shape: error wrapper always present ---

    @Test
    void allErrors_wrapInErrorObject() throws Exception {
        mockMvc.perform(get("/test/forbidden"))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").exists())
                .andExpect(jsonPath("$.error.message").exists());
    }

    // -------------------------------------------------------------------------
    // Minimal controller that throws each exception type on demand
    // -------------------------------------------------------------------------

    @RestController
    static class ThrowingController {

        @GetMapping("/test/validation")
        void throwValidation() {
            throw new ValidationException("comment must not be blank");
        }

        @PostMapping("/test/bean-validation")
        void throwBeanValidation(@Valid @RequestBody NamedDto dto) {
        }

        @GetMapping("/test/forbidden")
        void throwForbidden() {
            throw new ForbiddenActionException("creator cannot approve their own PO");
        }

        @GetMapping("/test/not-found")
        void throwNotFound() {
            throw new EntityNotFoundException("PO 99 not found");
        }

        @GetMapping("/test/conflict")
        void throwConflict() {
            throw new IllegalTransitionException("action not allowed in state: INVOICED");
        }

        @GetMapping("/test/server-error")
        void throwGeneric() {
            throw new RuntimeException("NullPointerException at com.pomanagement.SomeInternal.method");
        }

        record NamedDto(@NotBlank String name) {
        }
    }
}
