INSERT INTO public.role (id, "type") VALUES (4, 'ROLE_CUSTOMER');

SELECT setval('role_seq', max(id)) FROM public.role; 