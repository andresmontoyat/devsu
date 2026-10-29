-- BaseDatos.sql: esquema y datos de ejemplo de devsu-bank (cliente_db y cuenta_db).
--
-- ARCHIVO GENERADO desde los changelogs de Liquibase, que son la fuente de verdad del esquema.
-- No editar a mano: ./gradlew generateBaseDatos lo regenera y BaseDatosSqlIT falla si difiere.
--
-- Uso:       psql -U postgres -f BaseDatos.sql   (crea ambas bases; requiere psql por \connect)
-- Servicios: no lo usan; cada uno aplica sus migraciones con Liquibase al arrancar.

-- =============================================================================
-- cliente_db (cliente-service)
-- =============================================================================
CREATE DATABASE cliente_db;
\connect cliente_db
--
-- PostgreSQL database dump
--


-- Dumped from database version 17.11
-- Dumped by pg_dump version 17.11

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: cliente; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cliente (
    persona_id bigint NOT NULL,
    cliente_id uuid NOT NULL,
    contrasena_hash character varying(100) NOT NULL,
    estado boolean DEFAULT true NOT NULL
);


--
-- Name: outbox_message; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.outbox_message (
    id uuid NOT NULL,
    seq bigint NOT NULL,
    origin character varying(255) NOT NULL,
    routing_key character varying(255),
    topic character varying(255) NOT NULL,
    message_key character varying(255),
    payload text NOT NULL,
    status character varying(16) NOT NULL,
    attempts integer DEFAULT 0 NOT NULL,
    error character varying(512),
    occurred_at timestamp with time zone NOT NULL,
    next_attempt_at timestamp with time zone,
    published_at timestamp with time zone,
    claimed_at timestamp with time zone,
    claimed_by character varying(255),
    send_started_at timestamp with time zone,
    traceparent character varying(55)
);


--
-- Name: outbox_message_seq_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.outbox_message ALTER COLUMN seq ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.outbox_message_seq_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: persona; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.persona (
    id bigint NOT NULL,
    nombre character varying(100) NOT NULL,
    genero character varying(10) NOT NULL,
    edad smallint NOT NULL,
    identificacion character varying(20) NOT NULL,
    direccion character varying(200) NOT NULL,
    telefono character varying(16) NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    CONSTRAINT ck_persona_edad CHECK (((edad >= 0) AND (edad <= 150))),
    CONSTRAINT ck_persona_genero CHECK (((genero)::text = ANY ((ARRAY['MASCULINO'::character varying, 'FEMENINO'::character varying, 'OTRO'::character varying])::text[])))
);


--
-- Name: persona_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.persona ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.persona_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Data for Name: cliente; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.cliente (persona_id, cliente_id, contrasena_hash, estado) VALUES (1, '0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e01', '$2a$10$80eIfsexxNS0oeLvo8TwB.uLqZBBft2TbJaa7j8aLS.k03D1mmZty', true);
INSERT INTO public.cliente (persona_id, cliente_id, contrasena_hash, estado) VALUES (2, '0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e02', '$2a$10$x3QVHmualh0McM5QkNkKM.ldpHsy3Hd2fZu1yiqg5s83WY.ko4UIS', true);
INSERT INTO public.cliente (persona_id, cliente_id, contrasena_hash, estado) VALUES (3, '0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e03', '$2a$10$24JCNVGQgg.eXLOsH907C.M9igtdR7ctkS/HULs51QbcWNPx/BhiK', true);


--
-- Data for Name: outbox_message; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: persona; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.persona (id, nombre, genero, edad, identificacion, direccion, telefono, version) OVERRIDING SYSTEM VALUE VALUES (1, 'Jose Lema', 'MASCULINO', 35, '1712345678', 'Otavalo sn y principal', '098254785', 0);
INSERT INTO public.persona (id, nombre, genero, edad, identificacion, direccion, telefono, version) OVERRIDING SYSTEM VALUE VALUES (2, 'Marianela Montalvo', 'FEMENINO', 30, '1723456789', 'Amazonas y NNUU', '097548965', 0);
INSERT INTO public.persona (id, nombre, genero, edad, identificacion, direccion, telefono, version) OVERRIDING SYSTEM VALUE VALUES (3, 'Juan Osorio', 'MASCULINO', 40, '1734567890', '13 junio y Equinoccial', '098874587', 0);


--
-- Name: outbox_message_seq_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.outbox_message_seq_seq', 1, false);


--
-- Name: persona_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.persona_id_seq', 3, true);


--
-- Name: cliente cliente_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cliente
    ADD CONSTRAINT cliente_pkey PRIMARY KEY (persona_id);


--
-- Name: outbox_message outbox_message_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.outbox_message
    ADD CONSTRAINT outbox_message_pkey PRIMARY KEY (id);


--
-- Name: outbox_message outbox_message_seq_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.outbox_message
    ADD CONSTRAINT outbox_message_seq_key UNIQUE (seq);


--
-- Name: persona persona_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.persona
    ADD CONSTRAINT persona_pkey PRIMARY KEY (id);


--
-- Name: cliente uk_cliente_cliente_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cliente
    ADD CONSTRAINT uk_cliente_cliente_id UNIQUE (cliente_id);


--
-- Name: persona uk_persona_identificacion; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.persona
    ADD CONSTRAINT uk_persona_identificacion UNIQUE (identificacion);


--
-- Name: ix_outbox_message_claim; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_outbox_message_claim ON public.outbox_message USING btree (origin, status, seq);


--
-- Name: ix_outbox_message_key_seq; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_outbox_message_key_seq ON public.outbox_message USING btree (message_key, seq);


--
-- Name: cliente fk_cliente_persona; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cliente
    ADD CONSTRAINT fk_cliente_persona FOREIGN KEY (persona_id) REFERENCES public.persona(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--



-- =============================================================================
-- cuenta_db (cuenta-service)
-- =============================================================================
CREATE DATABASE cuenta_db;
\connect cuenta_db
--
-- PostgreSQL database dump
--


-- Dumped from database version 17.11
-- Dumped by pg_dump version 17.11

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: cliente_proyeccion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cliente_proyeccion (
    cliente_id uuid NOT NULL,
    nombre character varying(100) NOT NULL,
    activo boolean NOT NULL,
    eliminado boolean DEFAULT false NOT NULL,
    version bigint NOT NULL
);


--
-- Name: consumer_processed_event; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.consumer_processed_event (
    message_id uuid NOT NULL,
    processed_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: cuenta; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cuenta (
    id bigint NOT NULL,
    numero_cuenta character varying(20) NOT NULL,
    tipo_cuenta character varying(10) NOT NULL,
    saldo_inicial numeric(15,2) NOT NULL,
    saldo_disponible numeric(15,2) NOT NULL,
    estado boolean DEFAULT true NOT NULL,
    cliente_id uuid NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    CONSTRAINT ck_cuenta_saldo_disponible CHECK ((saldo_disponible >= (0)::numeric)),
    CONSTRAINT ck_cuenta_saldo_inicial CHECK ((saldo_inicial >= (0)::numeric)),
    CONSTRAINT ck_cuenta_tipo CHECK (((tipo_cuenta)::text = ANY ((ARRAY['AHORROS'::character varying, 'CORRIENTE'::character varying])::text[])))
);


--
-- Name: cuenta_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.cuenta ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.cuenta_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: movimiento; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.movimiento (
    id bigint NOT NULL,
    movimiento_id uuid NOT NULL,
    numero_cuenta character varying(20) NOT NULL,
    fecha timestamp with time zone NOT NULL,
    tipo_movimiento character varying(10) NOT NULL,
    valor numeric(15,2) NOT NULL,
    saldo_anterior numeric(15,2) NOT NULL,
    saldo numeric(15,2) NOT NULL,
    CONSTRAINT ck_movimiento_saldo CHECK (((saldo >= (0)::numeric) AND (saldo = (saldo_anterior + valor)))),
    CONSTRAINT ck_movimiento_tipo CHECK (((tipo_movimiento)::text = ANY ((ARRAY['DEPOSITO'::character varying, 'RETIRO'::character varying])::text[]))),
    CONSTRAINT ck_movimiento_valor CHECK ((valor <> (0)::numeric))
);


--
-- Name: movimiento_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.movimiento ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.movimiento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Data for Name: cliente_proyeccion; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.cliente_proyeccion (cliente_id, nombre, activo, eliminado, version) VALUES ('0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e01', 'Jose Lema', true, false, 0);
INSERT INTO public.cliente_proyeccion (cliente_id, nombre, activo, eliminado, version) VALUES ('0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e02', 'Marianela Montalvo', true, false, 0);
INSERT INTO public.cliente_proyeccion (cliente_id, nombre, activo, eliminado, version) VALUES ('0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e03', 'Juan Osorio', true, false, 0);


--
-- Data for Name: consumer_processed_event; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: cuenta; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.cuenta (id, numero_cuenta, tipo_cuenta, saldo_inicial, saldo_disponible, estado, cliente_id, version) OVERRIDING SYSTEM VALUE VALUES (1, '478758', 'AHORROS', 2000.00, 2000.00, true, '0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e01', 0);
INSERT INTO public.cuenta (id, numero_cuenta, tipo_cuenta, saldo_inicial, saldo_disponible, estado, cliente_id, version) OVERRIDING SYSTEM VALUE VALUES (2, '225487', 'CORRIENTE', 100.00, 100.00, true, '0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e02', 0);
INSERT INTO public.cuenta (id, numero_cuenta, tipo_cuenta, saldo_inicial, saldo_disponible, estado, cliente_id, version) OVERRIDING SYSTEM VALUE VALUES (3, '495878', 'AHORROS', 0.00, 0.00, true, '0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e03', 0);
INSERT INTO public.cuenta (id, numero_cuenta, tipo_cuenta, saldo_inicial, saldo_disponible, estado, cliente_id, version) OVERRIDING SYSTEM VALUE VALUES (4, '496825', 'AHORROS', 540.00, 540.00, true, '0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e02', 0);


--
-- Data for Name: movimiento; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: cuenta_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.cuenta_id_seq', 4, true);


--
-- Name: movimiento_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.movimiento_id_seq', 1, false);


--
-- Name: cliente_proyeccion cliente_proyeccion_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cliente_proyeccion
    ADD CONSTRAINT cliente_proyeccion_pkey PRIMARY KEY (cliente_id);


--
-- Name: consumer_processed_event consumer_processed_event_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.consumer_processed_event
    ADD CONSTRAINT consumer_processed_event_pkey PRIMARY KEY (message_id);


--
-- Name: cuenta cuenta_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cuenta
    ADD CONSTRAINT cuenta_pkey PRIMARY KEY (id);


--
-- Name: movimiento movimiento_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.movimiento
    ADD CONSTRAINT movimiento_pkey PRIMARY KEY (id);


--
-- Name: cuenta uk_cuenta_numero; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cuenta
    ADD CONSTRAINT uk_cuenta_numero UNIQUE (numero_cuenta);


--
-- Name: movimiento uk_movimiento_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.movimiento
    ADD CONSTRAINT uk_movimiento_id UNIQUE (movimiento_id);


--
-- Name: ix_consumer_processed_event_processed_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_consumer_processed_event_processed_at ON public.consumer_processed_event USING btree (processed_at);


--
-- Name: ix_cuenta_cliente; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_cuenta_cliente ON public.cuenta USING btree (cliente_id);


--
-- Name: ix_movimiento_cuenta_fecha; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_movimiento_cuenta_fecha ON public.movimiento USING btree (numero_cuenta, fecha);


--
-- Name: ix_movimiento_cuenta_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ix_movimiento_cuenta_id ON public.movimiento USING btree (numero_cuenta, id);


--
-- Name: cuenta fk_cuenta_cliente; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cuenta
    ADD CONSTRAINT fk_cuenta_cliente FOREIGN KEY (cliente_id) REFERENCES public.cliente_proyeccion(cliente_id);


--
-- Name: movimiento fk_movimiento_cuenta; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.movimiento
    ADD CONSTRAINT fk_movimiento_cuenta FOREIGN KEY (numero_cuenta) REFERENCES public.cuenta(numero_cuenta);


--
-- PostgreSQL database dump complete
--


