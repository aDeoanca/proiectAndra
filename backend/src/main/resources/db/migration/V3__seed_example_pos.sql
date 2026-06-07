-- sub-$100, non-IT → PENDING_FINANCE_APPROVAL (manager + IT skipped)
INSERT INTO purchase_orders (title, description, amount, currency, category, status, creator_id, created_at, updated_at)
SELECT 'Office supplies order', 'Pens and notebooks for Q1', 50.00, 'USD', 'OFFICE_SUPPLIES', 'PENDING_FINANCE_APPROVAL',
       id, NOW(), NOW()
FROM users WHERE email = 'alice@example.com';

INSERT INTO po_history (po_id, actor_id, action, from_status, to_status, created_at)
SELECT po.id, u.id, 'SUBMIT', NULL, 'PENDING_FINANCE_APPROVAL', NOW()
FROM purchase_orders po, users u
WHERE po.title = 'Office supplies order' AND u.email = 'alice@example.com';

-- sub-$100, IT_EQUIPMENT → PENDING_IT_VALIDATION (manager skipped)
INSERT INTO purchase_orders (title, description, amount, currency, category, status, creator_id, created_at, updated_at)
SELECT 'USB-C cables', 'Replacement cables for dev team', 45.00, 'USD', 'IT_EQUIPMENT', 'PENDING_IT_VALIDATION',
       id, NOW(), NOW()
FROM users WHERE email = 'alice@example.com';

INSERT INTO po_history (po_id, actor_id, action, from_status, to_status, created_at)
SELECT po.id, u.id, 'SUBMIT', NULL, 'PENDING_IT_VALIDATION', NOW()
FROM purchase_orders po, users u
WHERE po.title = 'USB-C cables' AND u.email = 'alice@example.com';

-- >=100, non-IT → PENDING_MANAGER_APPROVAL
INSERT INTO purchase_orders (title, description, amount, currency, category, status, creator_id, created_at, updated_at)
SELECT 'Conference room chairs', 'Ergonomic chairs for the new office wing', 500.00, 'USD', 'OFFICE_SUPPLIES', 'PENDING_MANAGER_APPROVAL',
       id, NOW(), NOW()
FROM users WHERE email = 'alice@example.com';

INSERT INTO po_history (po_id, actor_id, action, from_status, to_status, created_at)
SELECT po.id, u.id, 'SUBMIT', NULL, 'PENDING_MANAGER_APPROVAL', NOW()
FROM purchase_orders po, users u
WHERE po.title = 'Conference room chairs' AND u.email = 'alice@example.com';

-- >=100, IT_EQUIPMENT → PENDING_MANAGER_APPROVAL (all stages apply)
INSERT INTO purchase_orders (title, description, amount, currency, category, status, creator_id, created_at, updated_at)
SELECT 'Development laptops', 'MacBook Pro 14" for new hires', 2500.00, 'USD', 'IT_EQUIPMENT', 'PENDING_MANAGER_APPROVAL',
       id, NOW(), NOW()
FROM users WHERE email = 'alice@example.com';

INSERT INTO po_history (po_id, actor_id, action, from_status, to_status, created_at)
SELECT po.id, u.id, 'SUBMIT', NULL, 'PENDING_MANAGER_APPROVAL', NOW()
FROM purchase_orders po, users u
WHERE po.title = 'Development laptops' AND u.email = 'alice@example.com';
