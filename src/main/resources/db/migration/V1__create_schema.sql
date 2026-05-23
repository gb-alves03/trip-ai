-- ──────────────────────────────────────────────────────────────
-- trip.ai — Schema inicial
-- ──────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS users (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    phone      VARCHAR(30) UNIQUE NOT NULL,
    name       VARCHAR(150),
    status     VARCHAR(30) NOT NULL DEFAULT 'ONBOARDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Estado da conversa (máquina de estados / FSM)
CREATE TABLE IF NOT EXISTS conversation_states (
    user_id    UUID        PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    state      VARCHAR(50) NOT NULL DEFAULT 'IDLE',
    context    TEXT        NOT NULL DEFAULT '{}',   -- JSON serializado
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Preferências de viagem (rotas monitoradas)
CREATE TABLE IF NOT EXISTS travel_preferences (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    origin_iata      VARCHAR(3)   NOT NULL,
    destination_iata VARCHAR(3)   NOT NULL,
    date_from        DATE,
    date_to          DATE,
    flexible_dates   BOOLEAN      NOT NULL DEFAULT TRUE,
    max_budget       DECIMAL(10,2),
    direct_only      BOOLEAN      NOT NULL DEFAULT FALSE,
    passengers       INT          NOT NULL DEFAULT 1,
    active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Histórico de preços capturados pelo scheduler
CREATE TABLE IF NOT EXISTS price_history (
    id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    origin         VARCHAR(3)   NOT NULL,
    destination    VARCHAR(3)   NOT NULL,
    departure_date DATE         NOT NULL,
    return_date    DATE,
    price          DECIMAL(10,2) NOT NULL,
    airline        VARCHAR(150),
    stops          INT          NOT NULL DEFAULT 0,
    source         VARCHAR(50)  NOT NULL DEFAULT 'amadeus',
    affiliate_url  TEXT,
    captured_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Alertas enviados ao usuário
CREATE TABLE IF NOT EXISTS price_alerts (
    id                  UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    preference_id       UUID          NOT NULL REFERENCES travel_preferences(id) ON DELETE CASCADE,
    price_found         DECIMAL(10,2) NOT NULL,
    price_avg_reference DECIMAL(10,2),
    drop_percentage     DECIMAL(5,2),
    airline             VARCHAR(150),
    departure_date      DATE,
    return_date         DATE,
    affiliate_url       TEXT,
    sent_at             TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    user_action         VARCHAR(20)   -- SAVED | IGNORED | null
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_users_phone          ON users(phone);
CREATE INDEX IF NOT EXISTS idx_prefs_active         ON travel_preferences(active, user_id);
CREATE INDEX IF NOT EXISTS idx_price_history_route  ON price_history(origin, destination, captured_at DESC);
CREATE INDEX IF NOT EXISTS idx_alerts_pref          ON price_alerts(preference_id, sent_at DESC);
