import { describe, it, expect } from 'vitest';
import { createPoSchema } from './schemas';

describe('createPoSchema validation', () => {
  it('rejects blank title', () => {
    const result = createPoSchema.safeParse({
      title: '',
      amount: 50,
      category: 'SERVICES',
    });
    expect(result.success).toBe(false);
    if (!result.success) {
      const issue = result.error.issues.find((i) => i.path[0] === 'title');
      expect(issue?.message).toBe('Title is required');
    }
  });

  it('rejects amount of zero', () => {
    const result = createPoSchema.safeParse({
      title: 'Test PO',
      amount: 0,
      category: 'SERVICES',
    });
    expect(result.success).toBe(false);
    if (!result.success) {
      const issue = result.error.issues.find((i) => i.path[0] === 'amount');
      expect(issue).toBeDefined();
    }
  });

  it('rejects negative amount', () => {
    const result = createPoSchema.safeParse({
      title: 'Test PO',
      amount: -10,
      category: 'SERVICES',
    });
    expect(result.success).toBe(false);
    if (!result.success) {
      const issue = result.error.issues.find((i) => i.path[0] === 'amount');
      expect(issue).toBeDefined();
    }
  });

  it('accepts a valid payload', () => {
    const result = createPoSchema.safeParse({
      title: 'New Laptop',
      amount: 1500,
      category: 'IT_EQUIPMENT',
    });
    expect(result.success).toBe(true);
    if (result.success) {
      expect(result.data.title).toBe('New Laptop');
      expect(result.data.amount).toBe(1500);
      expect(result.data.category).toBe('IT_EQUIPMENT');
    }
  });

  it('accepts optional description', () => {
    const withDesc = createPoSchema.safeParse({
      title: 'Office Chairs',
      description: 'Ergonomic chairs for the new floor',
      amount: 250,
      category: 'OFFICE_SUPPLIES',
    });
    expect(withDesc.success).toBe(true);

    const withoutDesc = createPoSchema.safeParse({
      title: 'Office Chairs',
      amount: 250,
      category: 'OFFICE_SUPPLIES',
    });
    expect(withoutDesc.success).toBe(true);
  });
});
