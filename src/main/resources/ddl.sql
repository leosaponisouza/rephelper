-- Criação de tabelas base e índices para gerenciamento de repúblicas

create table if not exists public.republics
(
    uuid         uuid                     default gen_random_uuid() not null
        primary key,
    name         varchar(255)                                       not null,
    code         varchar(8)                                         not null
        unique,
    street       varchar(255)                                       not null,
    number       varchar(20)                                        not null,
    complement   varchar(255),
    neighborhood varchar(255)                                       not null,
    city         varchar(255)                                       not null,
    state        varchar(2)                                         not null,
    zip_code     varchar(10)                                        not null,
    created_at   timestamp with time zone default CURRENT_TIMESTAMP,
    updated_at   timestamp with time zone default CURRENT_TIMESTAMP,
    owner_id     uuid                                               not null
);

alter table public.republics
    owner to postgres;

create index if not exists idx_republic_code
    on public.republics (code);

create index if not exists idx_republic_owner_id
    on public.republics (owner_id);

create table if not exists public.users
(
    uuid                uuid                     default gen_random_uuid() not null
        constraint user_pkey
            primary key,
    name                varchar(255)                                       not null,
    email               varchar(255)                                       not null
        constraint user_email_key
            unique,
    phone_number        varchar(20),
    profile_picture_url varchar(255),
    current_republic_id uuid
        constraint fk_user_republic
            references public.republics
            on delete set null,
    status              varchar(255)             default 'active'::user_status,
    created_at          timestamp with time zone default CURRENT_TIMESTAMP,
    last_login          timestamp with time zone,
    provider            varchar(255)                                       not null,
    firebase_uid        varchar(255)                                       not null
        constraint user_firebase_uid_key
            unique,
    is_admin            boolean                  default false,
    entry_date          timestamp with time zone,
    departure_date      timestamp with time zone,
    nickname            varchar(255)
);

alter table public.users
    owner to postgres;

alter table public.republics
    add constraint fk_republic_owner
        foreign key (owner_id) references public.users
            on delete restrict;

create index if not exists idx_user_email
    on public.users (email);

create index if not exists idx_user_firebase_uid
    on public.users (firebase_uid);

create index if not exists idx_user_current_republic_id
    on public.users (current_republic_id);

create table if not exists public.tasks
(
    id           serial
        primary key,
    republic_id  uuid         not null
        constraint fk_task_republic
            references public.republics
            on delete cascade,
    title        varchar(255) not null,
    description  text,
    due_date     timestamp with time zone,
    category     varchar(50),
    status       varchar(50)              default 'pending'::character varying,
    created_at   timestamp with time zone default CURRENT_TIMESTAMP,
    updated_at   timestamp with time zone default CURRENT_TIMESTAMP,
    completed_at timestamp
);

alter table public.tasks
    owner to postgres;

create index if not exists idx_task_republic_id
    on public.tasks (republic_id);

create table if not exists public.user_tasks
(
    user_id uuid    not null
        constraint fk_user_tasks_user
            references public.users
            on delete cascade,
    task_id integer not null
        constraint fk_user_tasks_task
            references public.tasks
            on delete cascade,
    primary key (user_id, task_id)
);

alter table public.user_tasks
    owner to postgres;

create table if not exists public.events
(
    id          serial
        primary key,
    republic_id uuid                     not null
        constraint fk_event_republic
            references public.republics
            on delete cascade,
    title       varchar(255)             not null,
    description text,
    start_date  timestamp with time zone not null,
    end_date    timestamp with time zone not null,
    location    varchar(255),
    created_at  timestamp with time zone default CURRENT_TIMESTAMP
);

alter table public.events
    owner to postgres;

create index if not exists idx_event_republic_id
    on public.events (republic_id);

create table if not exists public.event_invitations
(
    user_id  uuid    not null
        constraint fk_event_invitations_user
            references public.users
            on delete cascade,
    event_id integer not null
        constraint fk_event_invitations_event
            references public.events
            on delete cascade,
    status   event_participant_status default 'invited'::event_participant_status,
    primary key (user_id, event_id)
);

alter table public.event_invitations
    owner to postgres;

-- Criação do tipo enum para status de despesas
DO $$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'expense_status') THEN
            CREATE TYPE expense_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'REIMBURSED');
        END IF;
    END
$$;

-- Atualização da tabela expenses para o sistema de gerenciamento financeiro
create table if not exists public.expenses
(
    id                 serial
        primary key,
    republic_id        uuid           not null
        constraint fk_expenses_republic
            references public.republics
            on delete cascade,
    creator_id         uuid
        constraint fk_expenses_creator
            references public.users
            on delete cascade,
    description        text           not null,
    amount             numeric(10, 2) not null,
    expense_date       date           not null,
    category           varchar(100),
    receipt_url        varchar(255),
    status             expense_status default 'PENDING'::expense_status,
    approval_date      timestamp with time zone,
    reimbursement_date timestamp with time zone,
    rejection_reason   text,
    created_at         timestamp with time zone default CURRENT_TIMESTAMP
);

alter table public.expenses
    owner to postgres;

create index if not exists idx_expenses_republic_id
    on public.expenses (republic_id);

create index if not exists idx_expenses_creator_id
    on public.expenses (creator_id);

create index if not exists idx_expenses_status
    on public.expenses (status);

-- Nova tabela para gerenciar as finanças da república (saldo, etc)
CREATE TABLE IF NOT EXISTS public.republic_finances
(
    id              serial PRIMARY KEY,
    republic_id     uuid NOT NULL
        constraint fk_republic_finances_republic
            references public.republics
            on delete cascade,
    current_balance numeric(12, 2) NOT NULL DEFAULT 0,
    last_updated    timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_republic_finances_republic_id UNIQUE (republic_id)
);

alter table public.republic_finances
    owner to postgres;

create index if not exists idx_republic_finances_republic_id
    on public.republic_finances (republic_id);

-- Nova tabela para registrar receitas da república
CREATE TABLE IF NOT EXISTS public.incomes
(
    id             serial PRIMARY KEY,
    republic_id    uuid NOT NULL
        constraint fk_incomes_republic
            references public.republics
            on delete cascade,
    contributor_id uuid
        constraint fk_incomes_contributor
            references public.users
            on delete set null,
    description    text NOT NULL,
    amount         numeric(10, 2) NOT NULL,
    income_date    timestamp with time zone NOT NULL,
    source         varchar(100) NOT NULL,  -- Contribuição, Evento, etc.
    created_at     timestamp with time zone DEFAULT CURRENT_TIMESTAMP
);

alter table public.incomes
    owner to postgres;

create index if not exists idx_incomes_republic_id
    on public.incomes (republic_id);

create index if not exists idx_incomes_contributor_id
    on public.incomes (contributor_id);

-- Nova tabela para planos de orçamento
CREATE TABLE IF NOT EXISTS public.budget_plans
(
    id             serial PRIMARY KEY,
    republic_id    uuid NOT NULL
        constraint fk_budget_plans_republic
            references public.republics
            on delete cascade,
    year           integer NOT NULL,
    month          integer NOT NULL,
    category       varchar(100) NOT NULL,
    planned_amount numeric(10, 2) NOT NULL,
    created_at     timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_budget_plan_month UNIQUE (republic_id, year, month, category)
);

alter table public.budget_plans
    owner to postgres;

create index if not exists idx_budget_plans_republic_id
    on public.budget_plans (republic_id);

-- Tabela expense_splits (mantida caso precise ser usada no futuro)
create table if not exists public.expense_splits
(
    expense_id integer        not null
        constraint fk_expense_splits_expense
            references public.expenses
            on delete cascade,
    user_id    uuid           not null
        constraint fk_expense_splits_user
            references public.users
            on delete cascade,
    amount     numeric(10, 2) not null,
    paid       boolean default false,
    paid_at    timestamp with time zone,
    primary key (expense_id, user_id)
);

alter table public.expense_splits
    owner to postgres;

create index if not exists idx_expense_splits_expense_id
    on public.expense_splits (expense_id);

create index if not exists idx_expense_splits_user_id
    on public.expense_splits (user_id);

create table if not exists public.inventory_items
(
    id          serial
        primary key,
    republic_id uuid         not null
        constraint fk_inventory_items_republic
            references public.republics
            on delete cascade,
    item_name   varchar(255) not null,
    quantity    integer      not null,
    condition   item_condition_type,
    created_at  timestamp with time zone default CURRENT_TIMESTAMP
);

alter table public.inventory_items
    owner to postgres;

create index if not exists idx_inventory_items_republic_id
    on public.inventory_items (republic_id);

create table if not exists public.polls
(
    id          serial
        primary key,
    republic_id uuid                     not null
        constraint fk_polls_republic
            references public.republics
            on delete cascade,
    question    text                     not null,
    start_date  timestamp with time zone not null,
    end_date    timestamp with time zone not null,
    created_by  uuid                     not null
        references public.users
            on delete cascade
        constraint fk_polls_user
            references public.users
            on delete cascade
);

alter table public.polls
    owner to postgres;

create index if not exists idx_polls_republic_id
    on public.polls (republic_id);

create index if not exists idx_polls_created_by
    on public.polls (created_by);

create table if not exists public.poll_options
(
    id          serial
        primary key,
    poll_id     integer      not null
        constraint fk_poll_options_poll
            references public.polls
            on delete cascade,
    option_text varchar(255) not null,
    vote_count  integer default 0
);

alter table public.poll_options
    owner to postgres;

create index if not exists idx_poll_options_poll_id
    on public.poll_options (poll_id);

create table if not exists public.poll_votes
(
    user_id        uuid    not null
        constraint fk_poll_votes_user
            references public.users
            on delete cascade,
    poll_option_id integer not null
        constraint fk_poll_votes_poll_option
            references public.poll_options
            on delete cascade,
    primary key (user_id, poll_option_id)
);

alter table public.poll_votes
    owner to postgres;

create index if not exists idx_poll_votes_user_id
    on public.poll_votes (user_id);

create index if not exists idx_poll_votes_poll_option_id
    on public.poll_votes (poll_option_id);

create table if not exists public.activity_log
(
    id          serial
        primary key,
    user_id     uuid
        constraint fk_activity_log_user
            references public.users
            on delete set null,
    republic_id uuid
        constraint fk_activity_log_republic
            references public.republics
            on delete set null,
    action      varchar(255) not null,
    details     text,
    created_at  timestamp with time zone default CURRENT_TIMESTAMP
);

alter table public.activity_log
    owner to postgres;

create index if not exists idx_activity_log_user_id
    on public.activity_log (user_id);