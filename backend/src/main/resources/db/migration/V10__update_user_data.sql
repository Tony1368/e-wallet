-- Upsert admin user
INSERT INTO public.user (id, first_name, last_name, username, email, "password")
VALUES (1, 'Quản Trị', 'Viên', 'admin', 'admin@hust.edu.vn', '$2a$10$MMOkMuO8zVcXl8YH2GrZSOYf/9zeC/sznGHRVzAq0T8.tzet7QJWq')
ON CONFLICT (id) DO UPDATE SET
  first_name = EXCLUDED.first_name,
  last_name = EXCLUDED.last_name,
  username = EXCLUDED.username,
  email = EXCLUDED.email,
  "password" = EXCLUDED."password";

-- Upsert user 2
INSERT INTO public.user (id, first_name, last_name, username, email, "password")
VALUES (2, 'Lê', 'Thái', 'thailq', 'thailq241017M@hust.edu.vn', '$2a$10$I5hOscIFqw73AU2/my0H0.vHAjI/rxXGcI49PB/jl8krTcM7VqkCy')
ON CONFLICT (id) DO UPDATE SET
  first_name = EXCLUDED.first_name,
  last_name = EXCLUDED.last_name,
  username = EXCLUDED.username,
  email = EXCLUDED.email,
  "password" = EXCLUDED."password";

-- Upsert user 3
INSERT INTO public.user (id, first_name, last_name, username, email, "password")
VALUES (3, 'Thành', 'Công', 'congthanh', 'thanhcong@gmail.com', '$2a$10$0grDMvQ7mSRLDAS6zuGOp.0ycwhgAzyE2FgLHzCV8KaXXP2TtGJ/W')
ON CONFLICT (id) DO UPDATE SET
  first_name = EXCLUDED.first_name,
  last_name = EXCLUDED.last_name,
  username = EXCLUDED.username,
  email = EXCLUDED.email,
  "password" = EXCLUDED."password";

-- Update the user_seq sequence
SELECT setval('user_seq', (SELECT max(id) FROM public.user)); 