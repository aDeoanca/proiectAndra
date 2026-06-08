package com.pomanagement.web;

import com.pomanagement.domain.entity.User;
import com.pomanagement.service.PurchaseOrderService;
import com.pomanagement.web.dto.CreatePoRequest;
import com.pomanagement.web.dto.PoDetailResponse;
import com.pomanagement.domain.enums.Status;
import com.pomanagement.web.dto.PoSummaryDto;
import com.pomanagement.web.dto.RejectRequest;
import com.pomanagement.web.dto.UpdatePoRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pos")
public class PurchaseOrderController {

    private final PurchaseOrderService service;

    public PurchaseOrderController(PurchaseOrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<PoSummaryDto> list(@RequestParam(required = false) String queue,
                                   @RequestParam(required = false) String creator,
                                   @RequestParam(required = false) Status status,
                                   @CurrentUser User currentUser) {
        return service.list(queue, creator, status, currentUser);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PoDetailResponse create(@RequestBody @Valid CreatePoRequest req,
                                   @CurrentUser User currentUser) {
        return service.create(req, currentUser);
    }

    @GetMapping("/{id}")
    public PoDetailResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PatchMapping("/{id}")
    public PoDetailResponse update(@PathVariable Long id,
                                   @RequestBody @Valid UpdatePoRequest req,
                                   @CurrentUser User currentUser) {
        return service.update(id, req, currentUser);
    }

    @PostMapping("/{id}/approve")
    public PoDetailResponse approve(@PathVariable Long id,
                                    @CurrentUser User currentUser) {
        return service.approve(id, currentUser);
    }

    @PostMapping("/{id}/reject")
    public PoDetailResponse reject(@PathVariable Long id,
                                   @RequestBody RejectRequest req,
                                   @CurrentUser User currentUser) {
        return service.reject(id, req.comment(), currentUser);
    }

    @PostMapping("/{id}/resubmit")
    public PoDetailResponse resubmit(@PathVariable Long id,
                                     @CurrentUser User currentUser) {
        return service.resubmit(id, currentUser);
    }
}
