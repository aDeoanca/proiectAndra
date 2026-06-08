package com.pomanagement.service;

import com.pomanagement.domain.entity.PurchaseOrder;
import com.pomanagement.domain.entity.User;
import com.pomanagement.domain.enums.Role;
import com.pomanagement.domain.enums.Status;
import com.pomanagement.domain.repository.PurchaseOrderRepository;
import com.pomanagement.web.dto.CreatePoRequest;
import com.pomanagement.web.dto.PoDetailResponse;
import com.pomanagement.web.dto.PoHistoryDto;
import com.pomanagement.web.dto.PoSummaryDto;
import com.pomanagement.web.dto.UpdatePoRequest;
import com.pomanagement.workflow.JpaPoHistoryRecorder;
import com.pomanagement.workflow.PurchaseOrderWorkflowService;
import com.pomanagement.workflow.exception.EntityNotFoundException;
import com.pomanagement.workflow.exception.ForbiddenActionException;
import com.pomanagement.workflow.exception.IllegalTransitionException;
import com.pomanagement.workflow.exception.ValidationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PurchaseOrderService {

    private static final int LIST_CAP = 200;
    private static final Sort UPDATED_DESC = Sort.by(Sort.Direction.DESC, "updatedAt");
    private static final Map<Role, Status> ROLE_QUEUE = Map.of(
            Role.MANAGER, Status.PENDING_MANAGER_APPROVAL,
            Role.IT_REP,  Status.PENDING_IT_VALIDATION,
            Role.FINANCE, Status.PENDING_FINANCE_APPROVAL
    );

    private final PurchaseOrderRepository repository;
    private final PurchaseOrderWorkflowService workflowService;
    private final JpaPoHistoryRecorder historyRecorder;

    public PurchaseOrderService(PurchaseOrderRepository repository,
                                PurchaseOrderWorkflowService workflowService,
                                JpaPoHistoryRecorder historyRecorder) {
        this.repository = repository;
        this.workflowService = workflowService;
        this.historyRecorder = historyRecorder;
    }

    public PoDetailResponse create(CreatePoRequest req, User actor) {
        PurchaseOrder po = PurchaseOrder.builder()
                .title(req.title())
                .description(req.description())
                .amount(req.amount())
                .category(req.category())
                .creator(actor)
                .status(Status.PENDING_MANAGER_APPROVAL) // placeholder; submit() overwrites it
                .build();
        repository.save(po);
        workflowService.submit(po, actor);
        return toDetail(po);
    }

    @Transactional(readOnly = true)
    public PoDetailResponse getById(Long id) {
        return toDetail(findOrThrow(id));
    }

    public PoDetailResponse update(Long id, UpdatePoRequest req, User actor) {
        PurchaseOrder po = findOrThrow(id);
        if (po.getStatus() != Status.NEEDS_REWORK) {
            throw new IllegalTransitionException(
                    "PO can only be edited when status is NEEDS_REWORK, was: " + po.getStatus());
        }
        if (!po.getCreator().getId().equals(actor.getId())) {
            throw new ForbiddenActionException("only the creator may edit a PO in NEEDS_REWORK");
        }
        if (req.title() != null) {
            if (req.title().isBlank()) {
                throw new ValidationException("title must not be blank");
            }
            po.setTitle(req.title());
        }
        if (req.description() != null) {
            po.setDescription(req.description());
        }
        if (req.amount() != null) {
            po.setAmount(req.amount());
        }
        if (req.category() != null) {
            po.setCategory(req.category());
        }
        return toDetail(po);
    }

    @Transactional(readOnly = true)
    public List<PoSummaryDto> list(String queue, String creator, Status status, User actor) {
        PageRequest page = PageRequest.of(0, LIST_CAP, UPDATED_DESC);

        if ("me".equals(queue)) {
            Status queueStatus = ROLE_QUEUE.get(actor.getRole());
            if (queueStatus == null) return List.of();
            return repository.findByStatusAndCreatorIdNot(queueStatus, actor.getId(), page)
                    .stream().map(PoSummaryDto::from).toList();
        }
        if ("me".equals(creator)) {
            return repository.findByCreatorId(actor.getId(), page)
                    .stream().map(PoSummaryDto::from).toList();
        }
        if (status != null) {
            return repository.findByStatus(status, page)
                    .stream().map(PoSummaryDto::from).toList();
        }
        return repository.findAll(page).stream()
                .map(PoSummaryDto::from).toList();
    }

    public PoDetailResponse approve(Long id, User actor) {
        PurchaseOrder po = findOrThrow(id);
        workflowService.approve(po, actor);
        return toDetail(po);
    }

    public PoDetailResponse reject(Long id, String comment, User actor) {
        PurchaseOrder po = findOrThrow(id);
        workflowService.reject(po, actor, comment);
        return toDetail(po);
    }

    public PoDetailResponse resubmit(Long id, User actor) {
        PurchaseOrder po = findOrThrow(id);
        workflowService.resubmit(po, actor);
        return toDetail(po);
    }

    private PurchaseOrder findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PurchaseOrder not found: " + id));
    }

    private PoDetailResponse toDetail(PurchaseOrder po) {
        List<PoHistoryDto> history = historyRecorder.historyFor(po.getId()).stream()
                .map(PoHistoryDto::from)
                .toList();
        return PoDetailResponse.from(po, history);
    }
}
