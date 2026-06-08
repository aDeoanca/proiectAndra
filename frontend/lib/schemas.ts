import { z } from 'zod';

export const createPoSchema = z.object({
  title: z.string().min(1, 'Title is required'),
  description: z.string().optional(),
  amount: z
    .number({ error: 'Amount must be a number' })
    .positive('Amount must be greater than 0'),
  category: z.enum(['SERVICES', 'OFFICE_SUPPLIES', 'IT_EQUIPMENT']),
});

export const updatePoSchema = z.object({
  title: z.string().min(1, 'Title must not be blank').optional(),
  description: z.string().optional(),
  amount: z
    .number({ error: 'Amount must be a number' })
    .positive('Amount must be greater than 0')
    .optional(),
  category: z.enum(['SERVICES', 'OFFICE_SUPPLIES', 'IT_EQUIPMENT']).optional(),
});

export type CreatePoFormValues = z.infer<typeof createPoSchema>;
export type UpdatePoFormValues = z.infer<typeof updatePoSchema>;
