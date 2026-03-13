INSERT INTO public.role (id, "type") VALUES (3, 'ROLE_ACCOUNTANT');

SELECT setval('role_seq', max(id)) FROM public.role; 