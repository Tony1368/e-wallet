INSERT INTO public.role (id, type) VALUES (1, 'ROLE_USER');
INSERT INTO public.role (id, type) VALUES (2, 'ROLE_ADMIN');
INSERT INTO public.role (id, type) VALUES (3, 'ROLE_ACCOUNTANT');
INSERT INTO public.role (id, type) VALUES (4, 'ROLE_CUSTOMER');

SELECT setval('role_seq', max(id)) FROM public.role;
