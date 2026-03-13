-- User Management Service - Database Init
CREATE SEQUENCE IF NOT EXISTS role_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS public.user_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE role
(
    id   BIGINT      NOT NULL,
    type VARCHAR(20) NOT NULL,
    CONSTRAINT pk_role PRIMARY KEY (id)
);

CREATE TABLE public."user"
(
    id         BIGINT       NOT NULL,
    first_name VARCHAR(50)  NOT NULL,
    last_name  VARCHAR(50)  NOT NULL,
    username   VARCHAR(20)  NOT NULL,
    email      VARCHAR(50)  NOT NULL,
    password   VARCHAR(100) NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id)
);

CREATE TABLE public.user_role
(
    role_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT pk_user_role PRIMARY KEY (role_id, user_id)
);

ALTER TABLE role ADD CONSTRAINT uc_role_type UNIQUE (type);
ALTER TABLE public."user" ADD CONSTRAINT uc_user_email UNIQUE (email);
ALTER TABLE public."user" ADD CONSTRAINT uc_user_username UNIQUE (username);

ALTER TABLE public.user_role
    ADD CONSTRAINT fk_user_role_on_role FOREIGN KEY (role_id) REFERENCES role (id);
ALTER TABLE public.user_role
    ADD CONSTRAINT fk_user_role_on_user FOREIGN KEY (user_id) REFERENCES public."user" (id);
